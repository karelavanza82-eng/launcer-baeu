package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WallpaperStyle
import com.example.ui.LauncherViewModel
import com.example.ui.screens.LauncherHomeScreen
import com.example.ui.theme.AuraHomeTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

  private val viewModel: LauncherViewModel by viewModels()
  private var homeResetSignal by mutableIntStateOf(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )

    setContent {
      AuraHomeTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val rootView = LocalView.current

        val defaultHomeLauncher =
          rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
          ) {
            viewModel.onLauncherResumed()
          }

        val galleryWallpaperLauncher =
          rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
          ) { uri ->
            if (uri != null) {
              viewModel.applyGalleryWallpaper(uri)
            }
          }

        LaunchedEffect(Unit) {
          viewModel.messages.collectLatest { msg -> snackbarHostState.showSnackbar(msg) }
        }

        BackHandler(
          enabled =
            !uiState.isEditMode &&
              !uiState.isQuickFilterActive &&
              !uiState.showSettings &&
              !uiState.showWallpaperPicker &&
              !uiState.showIconPackPicker &&
              uiState.contextMenuApp == null
        ) {
          homeResetSignal++
        }

        LauncherHomeScreen(
          uiState = uiState,
          snackbarHostState = snackbarHostState,
          resetToFirstPageSignal = homeResetSignal,
          onLaunchApp = { app, bounds ->
            viewModel.startAppLaunch(this@MainActivity, rootView, app, bounds)
          },
          onOpenAnimationPeak = {
            viewModel.onOpenAnimationCompleted(this@MainActivity, rootView)
          },
          onCloseAnimationFinished = { viewModel.onCloseAnimationCompleted() },
          onBackgroundTransitionProgress = { progress ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              val useWindowBlur =
                uiState.preferences.wallpaperStyle == WallpaperStyle.SYSTEM_WALLPAPER &&
                  uiState.preferences.blurWallpaperOnTransition &&
                  uiState.preferences.blurRadiusDp > 0
              val radiusPx =
                if (useWindowBlur) {
                  (uiState.preferences.blurRadiusDp * 2.2f * progress).toInt()
                } else {
                  0
                }
              try {
                window.setBackgroundBlurRadius(radiusPx)
              } catch (_: Exception) {}
            }
          },
          onEnterEditMode = { viewModel.setEditMode(true) },
          onExitEditMode = { viewModel.setEditMode(false) },
          onOpenContextMenu = { app -> viewModel.openContextMenu(app) },
          onDismissContextMenu = { viewModel.dismissContextMenu() },
          onTogglePinToDock = { app -> viewModel.togglePinToDock(app) },
          onMoveApp = { app, delta -> viewModel.moveApp(app, delta) },
          onHideApp = { app -> viewModel.hideApp(app) },
          onUnhideApp = { app -> viewModel.unhideApp(app) },
          onOpenAppInfo = { app -> viewModel.openAppInfo(this@MainActivity, app) },
          onUninstallApp = { app -> viewModel.uninstallApp(this@MainActivity, app) },
          onQuickFilterActiveChange = { active -> viewModel.setQuickFilterActive(active) },
          onQuickFilterQueryChange = { query -> viewModel.setQuickFilterQuery(query) },
          onOpenSettings = { viewModel.setShowSettings(true) },
          onCloseSettings = { viewModel.setShowSettings(false) },
          onOpenWallpaperPicker = { viewModel.setShowWallpaperPicker(true) },
          onCloseWallpaperPicker = { viewModel.setShowWallpaperPicker(false) },
          onOpenIconPackPicker = {
            viewModel.refreshIconPacks()
            viewModel.setShowIconPackPicker(true)
          },
          onCloseIconPackPicker = { viewModel.setShowIconPackPicker(false) },
          onSelectWallpaper = { style -> viewModel.setWallpaperStyle(style) },
          onPickGalleryWallpaper = {
            galleryWallpaperLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onSelectIconPack = { packId -> viewModel.setIconPackId(packId) },
          onSelectIconShape = { shape -> viewModel.setIconShape(shape) },
          onChangeIconSize = { sizeDp -> viewModel.setIconSizeDp(sizeDp) },
          onToggleBlurIcons = { enabled -> viewModel.setBlurIconsOnTransition(enabled) },
          onToggleBlurWallpaper = { enabled -> viewModel.setBlurWallpaperOnTransition(enabled) },
          onChangeBlurRadius = { radius -> viewModel.setBlurRadiusDp(radius) },
          onSelectLaunchMode = { mode -> viewModel.setLaunchAnimationMode(mode) },
          onSelectAnimationCurve = { curve -> viewModel.setAnimationCurve(curve) },
          onChangeAnimationDuration = { duration -> viewModel.setAnimationDurationMs(duration) },
          onChangeSpringDamping = { damping -> viewModel.setSpringDampingRatio(damping) },
          onChangeBackgroundScale = { scaleMin -> viewModel.setBackgroundScaleMin(scaleMin) },
          onSelectPageEffect = { effect -> viewModel.setPageEffect(effect) },
          onSelectScrollMode = { mode -> viewModel.setScrollMode(mode) },
          onToggleShowLabels = { show -> viewModel.setShowLabels(show) },
          onToggleDockBackground = { show -> viewModel.setShowDockBackground(show) },
          onToggleQuickFilterPill = { show -> viewModel.setShowQuickFilterPill(show) },
          onToggleLockLayout = { locked -> viewModel.setLockLayout(locked) },
          onSelectGridColumns = { cols -> viewModel.setGridColumns(cols) },
          onRefreshIconPacks = { viewModel.refreshIconPacks() },
          onRequestDefaultLauncher = {
            try {
              defaultHomeLauncher.launch(viewModel.createDefaultHomeIntent())
            } catch (_: Exception) {}
          },
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.onLauncherResumed()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.categories?.contains(Intent.CATEGORY_HOME) == true ||
      intent.action == Intent.ACTION_MAIN
    ) {
      viewModel.onHomePressed()
      homeResetSignal++
    }
  }
}
