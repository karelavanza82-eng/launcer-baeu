package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InstalledAppsRepository
import com.example.data.LauncherPreferencesRepository
import com.example.model.ActiveAppTransition
import com.example.model.AnimationCurveStyle
import com.example.model.AppItem
import com.example.model.DesktopScrollMode
import com.example.model.ExternalIconPackInfo
import com.example.model.IconShapeStyle
import com.example.model.LauncherUiState
import com.example.model.PageTransitionEffect
import com.example.model.WallpaperStyle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

  companion object {
    const val MAX_DOCK_APPS = 5
    private const val DEFAULT_INITIAL_DOCK_COUNT = 4
  }

  private val appsRepository = InstalledAppsRepository(application.applicationContext)
  private val prefsRepository = LauncherPreferencesRepository(application.applicationContext)

  private val installedAppsFlow = MutableStateFlow<List<AppItem>>(emptyList())
  private val externalPacksFlow = MutableStateFlow<List<ExternalIconPackInfo>>(emptyList())
  private val hasLoadedApps = MutableStateFlow(false)

  private val _uiState = MutableStateFlow(LauncherUiState())
  val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

  private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
  val messages: SharedFlow<String> = _messages.asSharedFlow()

  private var pendingClosingTransition: ActiveAppTransition? = null

  init {
    viewModelScope.launch {
      combine(
          appsRepository.observePackageChanges(),
          prefsRepository.preferencesFlow.map { it.iconPackId }.distinctUntilChanged(),
        ) { _, iconPackId ->
          iconPackId
        }
        .collect { iconPackId ->
          val apps = appsRepository.getInstalledApps(iconPackId)
          val packs = appsRepository.getExternalIconPacks()
          installedAppsFlow.value = apps
          externalPacksFlow.value = packs
          hasLoadedApps.value = true
        }
    }

    viewModelScope.launch {
      combine(
          installedAppsFlow,
          externalPacksFlow,
          prefsRepository.preferencesFlow,
          hasLoadedApps,
        ) { apps, packs, prefs, loaded ->
          Quadruple(apps, packs, prefs, loaded)
        }
        .collect { (apps, packs, prefs, loaded) ->
          if (loaded && apps.isNotEmpty() && !prefs.hasInitializedDock) {
            val initialDock = pickDefaultDockKeys(apps)
            prefsRepository.saveDockKeys(initialDock, markInitialized = true)
            return@collect
          }

          val appsByKey = apps.associateBy { it.key }
          val hiddenSet = prefs.hiddenKeys

          val validDockKeys =
            prefs.dockKeys.filter { appsByKey.containsKey(it) && !hiddenSet.contains(it) }
          val dockSet = validDockKeys.toSet()
          val dockApps = validDockKeys.mapNotNull { appsByKey[it] }

          val hiddenApps =
            apps.filter { hiddenSet.contains(it.key) }.sortedBy { it.label.lowercase() }

          val remainingDesktop =
            apps.filterNot { dockSet.contains(it.key) || hiddenSet.contains(it.key) }
          val orderedDesktop =
            if (prefs.desktopOrderKeys.isEmpty()) {
              remainingDesktop
            } else {
              val remainingMap = remainingDesktop.associateBy { it.key }.toMutableMap()
              val ordered = mutableListOf<AppItem>()
              for (key in prefs.desktopOrderKeys) {
                remainingMap.remove(key)?.let { ordered.add(it) }
              }
              ordered.addAll(remainingMap.values.sortedBy { it.label.lowercase() })
              ordered
            }

          _uiState.update { current ->
            current.copy(
              isLoading = !loaded,
              desktopApps = orderedDesktop,
              dockApps = dockApps,
              hiddenApps = hiddenApps,
              externalIconPacks = packs,
              preferences = prefs,
              isDefaultLauncher = appsRepository.isDefaultLauncher(),
            )
          }
        }
    }
  }

  fun startAppLaunch(app: AppItem, sourceBounds: Rect) {
    if (_uiState.value.activeTransition != null) return
    val opening =
      ActiveAppTransition(
        app = app,
        sourceBounds = sourceBounds,
        isOpening = true,
        triggerId = System.nanoTime(),
      )
    pendingClosingTransition = null
    _uiState.update {
      it.copy(
        isEditMode = false,
        contextMenuApp = null,
        isQuickFilterActive = false,
        quickFilterQuery = "",
        activeTransition = opening,
      )
    }
  }

  fun onOpenAnimationCompleted(activityContext: Context) {
    val transition = _uiState.value.activeTransition ?: return
    if (!transition.isOpening) return

    val launched = appsRepository.launchApp(activityContext, transition.app, transition.sourceBounds)
    if (launched) {
      pendingClosingTransition = transition
    } else {
      pendingClosingTransition = null
      _uiState.update {
        it.copy(activeTransition = transition.copy(isOpening = false, triggerId = System.nanoTime()))
      }
      _messages.tryEmit("Unable to launch ${transition.app.label}")
    }
  }

  fun onCloseAnimationCompleted() {
    _uiState.update { it.copy(activeTransition = null) }
  }

  fun onLauncherResumed() {
    viewModelScope.launch {
      val iconPackId = _uiState.value.preferences.iconPackId
      installedAppsFlow.value = appsRepository.getInstalledApps(iconPackId)
      externalPacksFlow.value = appsRepository.getExternalIconPacks()
      hasLoadedApps.value = true
    }

    val toClose = pendingClosingTransition
    pendingClosingTransition = null
    _uiState.update { current ->
      current.copy(
        isDefaultLauncher = appsRepository.isDefaultLauncher(),
        activeTransition = toClose?.copy(isOpening = false, triggerId = System.nanoTime()),
      )
    }
  }

  fun onHomePressed() {
    _uiState.update {
      it.copy(
        isEditMode = false,
        contextMenuApp = null,
        isQuickFilterActive = false,
        quickFilterQuery = "",
        showSettings = false,
        showWallpaperPicker = false,
        showIconPackPicker = false,
      )
    }
  }

  fun setEditMode(enabled: Boolean) {
    if (enabled && _uiState.value.preferences.lockLayout) {
      _messages.tryEmit("Desktop layout is locked in Home Settings")
      return
    }
    _uiState.update {
      it.copy(
        isEditMode = enabled,
        contextMenuApp = null,
        isQuickFilterActive = false,
        quickFilterQuery = "",
      )
    }
  }

  fun openContextMenu(app: AppItem) {
    _uiState.update { it.copy(contextMenuApp = app) }
  }

  fun dismissContextMenu() {
    _uiState.update { it.copy(contextMenuApp = null) }
  }

  fun setQuickFilterActive(active: Boolean) {
    _uiState.update {
      it.copy(
        isQuickFilterActive = active,
        quickFilterQuery = if (active) it.quickFilterQuery else "",
      )
    }
  }

  fun setQuickFilterQuery(query: String) {
    _uiState.update { it.copy(quickFilterQuery = query) }
  }

  fun setShowSettings(show: Boolean) {
    _uiState.update {
      it.copy(
        showSettings = show,
        isEditMode = false,
        contextMenuApp = null,
        showWallpaperPicker = false,
        showIconPackPicker = false,
      )
    }
  }

  fun setShowWallpaperPicker(show: Boolean) {
    _uiState.update {
      it.copy(
        showWallpaperPicker = show,
        isEditMode = false,
        contextMenuApp = null,
        showIconPackPicker = false,
      )
    }
  }

  fun setShowIconPackPicker(show: Boolean) {
    _uiState.update {
      it.copy(
        showIconPackPicker = show,
        isEditMode = false,
        contextMenuApp = null,
        showWallpaperPicker = false,
      )
    }
  }

  fun togglePinToDock(app: AppItem) {
    if (_uiState.value.preferences.lockLayout) {
      _messages.tryEmit("Desktop layout is locked in Home Settings")
      return
    }
    viewModelScope.launch {
      val currentDock = _uiState.value.dockApps.map { it.key }.toMutableList()
      if (currentDock.contains(app.key)) {
        currentDock.remove(app.key)
        prefsRepository.saveDockKeys(currentDock)
      } else {
        if (currentDock.size >= MAX_DOCK_APPS) {
          _messages.tryEmit(
            "Dock holds up to $MAX_DOCK_APPS favorite apps. Remove one first to pin another."
          )
        } else {
          currentDock.add(app.key)
          prefsRepository.saveDockKeys(currentDock)
        }
      }
      _uiState.update { it.copy(contextMenuApp = null) }
    }
  }

  fun moveApp(app: AppItem, delta: Int) {
    if (_uiState.value.preferences.lockLayout) {
      _messages.tryEmit("Desktop layout is locked in Home Settings")
      return
    }
    viewModelScope.launch {
      val dockKeys = _uiState.value.dockApps.map { it.key }.toMutableList()
      val dockIndex = dockKeys.indexOf(app.key)
      if (dockIndex != -1) {
        val target = (dockIndex + delta).coerceIn(0, dockKeys.lastIndex)
        if (target != dockIndex) {
          dockKeys.removeAt(dockIndex)
          dockKeys.add(target, app.key)
          prefsRepository.saveDockKeys(dockKeys)
        }
        _uiState.update { it.copy(contextMenuApp = null) }
        return@launch
      }

      val desktopKeys = _uiState.value.desktopApps.map { it.key }.toMutableList()
      val desktopIndex = desktopKeys.indexOf(app.key)
      if (desktopIndex != -1) {
        val target = (desktopIndex + delta).coerceIn(0, desktopKeys.lastIndex)
        if (target != desktopIndex) {
          desktopKeys.removeAt(desktopIndex)
          desktopKeys.add(target, app.key)
          prefsRepository.saveDesktopOrderKeys(desktopKeys)
        }
      }
      _uiState.update { it.copy(contextMenuApp = null) }
    }
  }

  fun hideApp(app: AppItem) {
    viewModelScope.launch {
      val updated = _uiState.value.preferences.hiddenKeys + app.key
      prefsRepository.saveHiddenKeys(updated)
      _uiState.update { it.copy(contextMenuApp = null) }
    }
  }

  fun unhideApp(app: AppItem) {
    viewModelScope.launch {
      val updated = _uiState.value.preferences.hiddenKeys - app.key
      prefsRepository.saveHiddenKeys(updated)
    }
  }

  fun openAppInfo(activityContext: Context, app: AppItem) {
    _uiState.update { it.copy(contextMenuApp = null) }
    appsRepository.openAppInfo(activityContext, app.packageName)
  }

  fun uninstallApp(activityContext: Context, app: AppItem) {
    _uiState.update { it.copy(contextMenuApp = null) }
    appsRepository.requestUninstall(activityContext, app.packageName)
  }

  fun createDefaultHomeIntent() = appsRepository.createDefaultHomeIntent()

  fun setWallpaperStyle(style: WallpaperStyle) {
    viewModelScope.launch { prefsRepository.setWallpaperStyle(style) }
  }

  fun applyGalleryWallpaper(uri: Uri) {
    viewModelScope.launch {
      val savedPath = appsRepository.saveCustomWallpaper(uri)
      if (savedPath != null) {
        prefsRepository.setCustomWallpaperPath(savedPath)
      }
    }
  }

  fun setIconPackId(packId: String) {
    viewModelScope.launch { prefsRepository.setIconPackId(packId) }
  }

  fun setIconShape(shape: IconShapeStyle) {
    viewModelScope.launch { prefsRepository.setIconShape(shape) }
  }

  fun setIconSizeDp(sizeDp: Int) {
    viewModelScope.launch { prefsRepository.setIconSizeDp(sizeDp) }
  }

  fun setBlurIconsOnTransition(enabled: Boolean) {
    viewModelScope.launch { prefsRepository.setBlurIconsOnTransition(enabled) }
  }

  fun setBlurWallpaperOnTransition(enabled: Boolean) {
    viewModelScope.launch { prefsRepository.setBlurWallpaperOnTransition(enabled) }
  }

  fun setBlurRadiusDp(radiusDp: Int) {
    viewModelScope.launch { prefsRepository.setBlurRadiusDp(radiusDp) }
  }

  fun setAnimationCurve(curve: AnimationCurveStyle) {
    viewModelScope.launch { prefsRepository.setAnimationCurve(curve) }
  }

  fun setAnimationDurationMs(durationMs: Int) {
    viewModelScope.launch { prefsRepository.setAnimationDurationMs(durationMs) }
  }

  fun setSpringDampingRatio(damping: Float) {
    viewModelScope.launch { prefsRepository.setSpringDampingRatio(damping) }
  }

  fun setBackgroundScaleMin(scaleMin: Float) {
    viewModelScope.launch { prefsRepository.setBackgroundScaleMin(scaleMin) }
  }

  fun setPageEffect(effect: PageTransitionEffect) {
    viewModelScope.launch { prefsRepository.setPageEffect(effect) }
  }

  fun setScrollMode(mode: DesktopScrollMode) {
    viewModelScope.launch { prefsRepository.setScrollMode(mode) }
  }

  fun setShowLabels(show: Boolean) {
    viewModelScope.launch { prefsRepository.setShowLabels(show) }
  }

  fun setShowDockBackground(show: Boolean) {
    viewModelScope.launch { prefsRepository.setShowDockBackground(show) }
  }

  fun setShowQuickFilterPill(show: Boolean) {
    viewModelScope.launch { prefsRepository.setShowQuickFilterPill(show) }
  }

  fun setLockLayout(locked: Boolean) {
    viewModelScope.launch {
      prefsRepository.setLockLayout(locked)
      if (locked) _uiState.update { it.copy(isEditMode = false) }
    }
  }

  fun setGridColumns(columns: Int) {
    viewModelScope.launch { prefsRepository.setGridColumns(columns) }
  }

  private fun pickDefaultDockKeys(apps: List<AppItem>): List<String> {
    val selected = LinkedHashSet<String>()
    val priorityMatchers: List<(AppItem) -> Boolean> =
      listOf(
        {
          val p = it.packageName.lowercase()
          val l = it.label.lowercase()
          p.contains("dialer") || p.contains("telecom") || l == "phone"
        },
        {
          val p = it.packageName.lowercase()
          val l = it.label.lowercase()
          p.contains("messaging") || p.contains("mms") || l.contains("messages")
        },
        {
          val p = it.packageName.lowercase()
          val l = it.label.lowercase()
          p.contains("chrome") || p.contains("browser") || l.contains("browser")
        },
        {
          val p = it.packageName.lowercase()
          val l = it.label.lowercase()
          p.contains("camera") || l == "camera"
        },
      )

    for (matcher in priorityMatchers) {
      apps.firstOrNull { matcher(it) && !selected.contains(it.key) }?.let { selected.add(it.key) }
    }
    for (app in apps) {
      if (selected.size >= DEFAULT_INITIAL_DOCK_COUNT) break
      selected.add(app.key)
    }
    return selected.toList()
  }

  private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
