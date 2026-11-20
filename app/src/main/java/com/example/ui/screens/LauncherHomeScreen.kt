package com.example.ui.screens

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.R
import com.example.model.AnimationCurveStyle
import com.example.model.AppItem
import com.example.model.DesktopScrollMode
import com.example.model.IconShapeStyle
import com.example.model.LaunchAnimationMode
import com.example.model.LauncherUiState
import com.example.model.PageTransitionEffect
import com.example.model.WallpaperStyle
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.AppIconItem
import com.example.ui.components.AppTransitionOverlay
import com.example.ui.components.AuraWallpaper
import com.example.ui.components.FrostedDock
import com.example.ui.components.rememberLauncherAnimationSpec
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LauncherHomeScreen(
  uiState: LauncherUiState,
  snackbarHostState: SnackbarHostState,
  resetToFirstPageSignal: Int,
  onLaunchApp: (AppItem, Rect) -> Unit,
  onOpenAnimationPeak: () -> Unit,
  onCloseAnimationFinished: () -> Unit,
  onBackgroundTransitionProgress: (Float) -> Unit,
  onEnterEditMode: () -> Unit,
  onExitEditMode: () -> Unit,
  onOpenContextMenu: (AppItem) -> Unit,
  onDismissContextMenu: () -> Unit,
  onTogglePinToDock: (AppItem) -> Unit,
  onMoveApp: (AppItem, Int) -> Unit,
  onHideApp: (AppItem) -> Unit,
  onUnhideApp: (AppItem) -> Unit,
  onOpenAppInfo: (AppItem) -> Unit,
  onUninstallApp: (AppItem) -> Unit,
  onQuickFilterActiveChange: (Boolean) -> Unit,
  onQuickFilterQueryChange: (String) -> Unit,
  onOpenSettings: () -> Unit,
  onCloseSettings: () -> Unit,
  onOpenWallpaperPicker: () -> Unit,
  onCloseWallpaperPicker: () -> Unit,
  onOpenIconPackPicker: () -> Unit,
  onCloseIconPackPicker: () -> Unit,
  onSelectWallpaper: (WallpaperStyle) -> Unit,
  onPickGalleryWallpaper: () -> Unit,
  onSelectIconPack: (String) -> Unit,
  onSelectIconShape: (IconShapeStyle) -> Unit,
  onChangeIconSize: (Int) -> Unit,
  onToggleBlurIcons: (Boolean) -> Unit,
  onToggleBlurWallpaper: (Boolean) -> Unit,
  onChangeBlurRadius: (Int) -> Unit,
  onSelectLaunchMode: (LaunchAnimationMode) -> Unit,
  onSelectAnimationCurve: (AnimationCurveStyle) -> Unit,
  onChangeAnimationDuration: (Int) -> Unit,
  onChangeSpringDamping: (Float) -> Unit,
  onChangeBackgroundScale: (Float) -> Unit,
  onSelectPageEffect: (PageTransitionEffect) -> Unit,
  onSelectScrollMode: (DesktopScrollMode) -> Unit,
  onToggleShowLabels: (Boolean) -> Unit,
  onToggleDockBackground: (Boolean) -> Unit,
  onToggleQuickFilterPill: (Boolean) -> Unit,
  onToggleLockLayout: (Boolean) -> Unit,
  onSelectGridColumns: (Int) -> Unit,
  onRefreshIconPacks: () -> Unit,
  onRequestDefaultLauncher: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val density = LocalDensity.current
  val iconBoundsMap = remember { mutableStateMapOf<String, Rect>() }

  // Single Animatable shared cleanly into graphicsLayer & layout lambdas (ZERO recomposition per frame!)
  val transitionProgressAnim = remember { Animatable(0f) }
  val prefs = uiState.preferences
  val isTransitioning = uiState.activeTransition != null
  val nativeAnimSpec =
    rememberLauncherAnimationSpec(
      curveStyle = prefs.animationCurve,
      durationMs = prefs.animationDurationMs,
      springDampingRatio = prefs.springDampingRatio,
    )

  LaunchedEffect(uiState.activeTransition) {
    val transition = uiState.activeTransition
    if (transition == null) {
      transitionProgressAnim.snapTo(0f)
      onBackgroundTransitionProgress(0f)
    } else {
      snapshotFlow { transitionProgressAnim.value }.collectLatest { p ->
        onBackgroundTransitionProgress(p)
      }
    }
  }

  // In SCALE_UP_ICON / CLIP_REVEAL_ICON modes, the OS WindowManager animates the real app window directly.
  // We animate only the launcher desktop depth scale & blur here — zero ghost white splash card!
  LaunchedEffect(uiState.activeTransition?.triggerId, prefs.launchAnimationMode) {
    val transition = uiState.activeTransition ?: return@LaunchedEffect
    if (prefs.launchAnimationMode != LaunchAnimationMode.MORPH_CARD) {
      transitionProgressAnim.snapTo(if (transition.isOpening) 0f else 1f)
      if (transition.isOpening) {
        transitionProgressAnim.animateTo(targetValue = 1f, animationSpec = nativeAnimSpec)
      } else {
        transitionProgressAnim.animateTo(targetValue = 0f, animationSpec = nativeAnimSpec)
        onCloseAnimationFinished()
      }
    }
  }

  // Pre-cache 8 blur RenderEffect steps for icons so 0 allocations happen during 120fps animations
  val maxIconBlurPx = with(density) { (prefs.blurRadiusDp * 0.72f).dp.toPx() }
  val cachedIconBlurEffects: List<RenderEffect?> =
    remember(maxIconBlurPx) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && maxIconBlurPx > 1f) {
        List(9) { step ->
          if (step == 0) {
            null
          } else {
            val r = maxIconBlurPx * (step / 8f)
            AndroidRenderEffect.createBlurEffect(r, r, Shader.TileMode.CLAMP).asComposeRenderEffect()
          }
        }
      } else {
        emptyList()
      }
    }

  val filteredDesktopApps by
    remember(uiState.desktopApps, uiState.isQuickFilterActive, uiState.quickFilterQuery) {
      derivedStateOf {
        val q = uiState.quickFilterQuery.trim()
        if (!uiState.isQuickFilterActive || q.isEmpty()) {
          uiState.desktopApps
        } else {
          uiState.desktopApps.filter { it.label.contains(q, ignoreCase = true) }
        }
      }
    }

  BackHandler(enabled = uiState.isEditMode || uiState.isQuickFilterActive) {
    if (uiState.isQuickFilterActive) {
      onQuickFilterActiveChange(false)
    } else if (uiState.isEditMode) {
      onExitEditMode()
    }
  }

  val columns = prefs.gridColumns.coerceIn(4, 5)
  val iconSizeDp = prefs.iconSizeDp.dp
  val pagerParallax = remember { { 0f } }

  Box(modifier = Modifier.fillMaxSize()) {
    // 1. Zero-recomposition GPU wallpaper layer
    AuraWallpaper(
      style = prefs.wallpaperStyle,
      customPhotoPath = prefs.customWallpaperPath,
      blurEnabled = prefs.blurWallpaperOnTransition,
      blurRadiusDp = prefs.blurRadiusDp,
      transitionProgressProvider = {
        if (isTransitioning) transitionProgressAnim.value else 0f
      },
      parallaxProvider = pagerParallax,
    )

    // 2. Desktop + Dock interactive workspace (no App Drawer: all apps directly on desktop)
    Column(
      modifier =
        Modifier.fillMaxSize()
          .statusBarsPadding()
          .pointerInput(uiState.isEditMode, uiState.isQuickFilterActive) {
            detectTapGestures(
              onTap = {
                if (uiState.isEditMode) onExitEditMode()
                if (uiState.isQuickFilterActive) onQuickFilterActiveChange(false)
              },
              onLongPress = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (uiState.isEditMode) onExitEditMode() else onEnterEditMode()
              },
            )
          }
          .graphicsLayer {
            val p = if (isTransitioning) transitionProgressAnim.value.coerceIn(0f, 1f) else 0f
            val scale = lerp(1f, prefs.backgroundScaleMin, p)
            scaleX = scale
            scaleY = scale
            if (isTransitioning && prefs.blurIconsOnTransition) {
              alpha = 1f - (0.30f * p)
              if (cachedIconBlurEffects.isNotEmpty()) {
                val idx = (p * 8f).roundToInt().coerceIn(0, 8)
                renderEffect = cachedIconBlurEffects[idx]
              }
            } else {
              alpha = 1f
              renderEffect = null
            }
          }
    ) {
      // Optional inline Quick Filter input when tapped
      AnimatedVisibility(visible = uiState.isQuickFilterActive) {
        QuickFilterBar(
          query = uiState.quickFilterQuery,
          onQueryChange = onQuickFilterQueryChange,
          onClose = { onQuickFilterActiveChange(false) },
        )
      }

      // Top desktop grid area
      BoxWithConstraints(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter,
      ) {
        val availableHeight = maxHeight
        val cellHeightApprox =
          (prefs.iconSizeDp + if (prefs.showLabels) 36 else 18).dp
        val rowsPerPage = (availableHeight / cellHeightApprox).toInt().coerceIn(3, 6)
        val appsPerPage = columns * rowsPerPage

        when {
          uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = Color.White)
            }
          }
          filteredDesktopApps.isEmpty() && uiState.dockApps.isEmpty() -> {
            EmptyDesktopView()
          }
          prefs.scrollMode == DesktopScrollMode.PAGED -> {
            val pages =
              remember(filteredDesktopApps, appsPerPage) {
                if (filteredDesktopApps.isEmpty()) listOf(emptyList())
                else filteredDesktopApps.chunked(appsPerPage)
              }
            val pagerState = rememberPagerState(pageCount = { pages.size })

            LaunchedEffect(resetToFirstPageSignal) {
              if (resetToFirstPageSignal > 0 && pagerState.currentPage != 0) {
                pagerState.animateScrollToPage(0)
              }
            }

            Column(modifier = Modifier.fillMaxSize()) {
              HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("desktop_pager"),
                beyondViewportPageCount = 1,
                key = { page -> page },
              ) { pageIndex ->
                val pageApps = pages[pageIndex]
                val rows = ceil(pageApps.size / columns.toFloat()).toInt().coerceAtLeast(1)

                Column(
                  modifier =
                    Modifier.fillMaxSize()
                      .graphicsLayer {
                        val pageOffset =
                          (pagerState.currentPage - pageIndex) +
                            pagerState.currentPageOffsetFraction
                        val absOffset = abs(pageOffset).coerceIn(0f, 1f)
                        when (prefs.pageEffect) {
                          PageTransitionEffect.SLIDE -> Unit
                          PageTransitionEffect.DEPTH_STACK -> {
                            val s = lerp(1f, 0.90f, absOffset)
                            scaleX = s
                            scaleY = s
                            alpha = lerp(1f, 0.65f, absOffset)
                          }
                          PageTransitionEffect.PARALLAX_FADE -> {
                            alpha = lerp(1f, 0.25f, absOffset)
                            translationX = pageOffset * size.width * 0.14f
                          }
                          PageTransitionEffect.CUBE -> {
                            cameraDistance = 14f * density.density
                            rotationY = (-pageOffset * 26f).coerceIn(-30f, 30f)
                            alpha = lerp(1f, 0.72f, absOffset)
                          }
                          PageTransitionEffect.SCALE_ZOOM -> {
                            val s = lerp(1f, 0.84f, absOffset)
                            scaleX = s
                            scaleY = s
                            alpha = lerp(1f, 0.55f, absOffset)
                          }
                        }
                      }
                      .padding(horizontal = 14.dp, vertical = 6.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  for (r in 0 until rows) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceEvenly,
                      verticalAlignment = Alignment.Top,
                    ) {
                      for (c in 0 until columns) {
                        val idx = r * columns + c
                        if (idx < pageApps.size) {
                          val app = pageApps[idx]
                          AppIconItem(
                            app = app,
                            showLabel = prefs.showLabels,
                            isEditMode = uiState.isEditMode,
                            isHiddenForTransition =
                              prefs.launchAnimationMode == LaunchAnimationMode.MORPH_CARD &&
                                uiState.activeTransition?.app?.key == app.key,
                            iconSize = iconSizeDp,
                            iconShapeStyle = prefs.iconShape,
                            onClick = { bounds -> onLaunchApp(app, bounds) },
                            onLongClick = {
                              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                              onOpenContextMenu(app)
                            },
                            onBoundsMeasured = { key, rect -> iconBoundsMap[key] = rect },
                            modifier = Modifier.weight(1f),
                          )
                        } else {
                          Spacer(modifier = Modifier.weight(1f))
                        }
                      }
                    }
                  }
                }
              }

              if (pages.size > 1) {
                PageIndicatorDots(
                  pageCount = pages.size,
                  currentPage = pagerState.currentPage,
                  onSelectPage = { target ->
                    scope.launch { pagerState.animateScrollToPage(target) }
                  },
                  modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                )
              }
            }
          }
          else -> {
            LazyVerticalGrid(
              columns = GridCells.Fixed(columns),
              modifier = Modifier.fillMaxSize().testTag("desktop_vertical_grid"),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              items(items = filteredDesktopApps, key = { it.key }) { app ->
                AppIconItem(
                  app = app,
                  showLabel = prefs.showLabels,
                  isEditMode = uiState.isEditMode,
                  isHiddenForTransition =
                    prefs.launchAnimationMode == LaunchAnimationMode.MORPH_CARD &&
                      uiState.activeTransition?.app?.key == app.key,
                  iconSize = iconSizeDp,
                  iconShapeStyle = prefs.iconShape,
                  onClick = { bounds -> onLaunchApp(app, bounds) },
                  onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenContextMenu(app)
                  },
                  onBoundsMeasured = { key, rect -> iconBoundsMap[key] = rect },
                )
              }
            }
          }
        }
      }

      // Optional translucent Quick Filter pill above dock (like the video's floating pill)
      if (prefs.showQuickFilterPill && !uiState.isEditMode && !uiState.isQuickFilterActive) {
        Box(
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Row(
            modifier =
              Modifier.clip(CircleShape)
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.24f), CircleShape)
                .clickable { onQuickFilterActiveChange(true) }
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("quick_filter_pill"),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.Search,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Filter apps",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = Color.White.copy(alpha = 0.92f),
            )
          }
        }
      }

      // 3. Favorites Dock OR Edit Mode Toolbar
      Box(modifier = Modifier.fillMaxWidth()) {
        this@Column.AnimatedVisibility(
          visible = !uiState.isEditMode,
          enter = slideInVertically { it } + fadeIn(),
          exit = slideOutVertically { it } + fadeOut(),
          modifier = Modifier.align(Alignment.BottomCenter),
        ) {
          FrostedDock(
            dockApps = uiState.dockApps,
            isEditMode = uiState.isEditMode,
            hiddenAppKey =
              if (prefs.launchAnimationMode == LaunchAnimationMode.MORPH_CARD) {
                uiState.activeTransition?.app?.key
              } else {
                null
              },
            showDockBackground = prefs.showDockBackground,
            iconSize = iconSizeDp.coerceAtMost(60.dp),
            iconShapeStyle = prefs.iconShape,
            onAppClick = { app, bounds -> onLaunchApp(app, bounds) },
            onAppLongClick = { app ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onOpenContextMenu(app)
            },
            onBoundsMeasured = { key, rect -> iconBoundsMap[key] = rect },
          )
        }

        this@Column.AnimatedVisibility(
          visible = uiState.isEditMode,
          enter = slideInVertically { it } + fadeIn(),
          exit = slideOutVertically { it } + fadeOut(),
          modifier = Modifier.align(Alignment.BottomCenter),
        ) {
          EditModeToolbar(
            onWallpaper = onOpenWallpaperPicker,
            onIconPack = onOpenIconPackPicker,
            onSettings = onOpenSettings,
            onDone = onExitEditMode,
          )
        }
      }
    }

    // 4. Optional Morph Card Overlay (only rendered when user chooses MORPH_CARD mode)
    if (prefs.launchAnimationMode == LaunchAnimationMode.MORPH_CARD) {
      uiState.activeTransition?.let { transition ->
        val liveBounds = iconBoundsMap[transition.app.key] ?: transition.sourceBounds
        AppTransitionOverlay(
          transition = transition.copy(sourceBounds = liveBounds),
          progressAnim = transitionProgressAnim,
          curveStyle = prefs.animationCurve,
          durationMs = prefs.animationDurationMs,
          springDampingRatio = prefs.springDampingRatio,
          iconShapeStyle = prefs.iconShape,
          onOpenAnimationPeak = onOpenAnimationPeak,
          onCloseAnimationFinished = onCloseAnimationFinished,
        )
      }
    }

    // 5. Long-press App Context Menu
    uiState.contextMenuApp?.let { selectedApp ->
      val isInDock = uiState.dockApps.any { it.key == selectedApp.key }
      val list = if (isInDock) uiState.dockApps else uiState.desktopApps
      val index = list.indexOfFirst { it.key == selectedApp.key }

      AppContextMenuDialog(
        app = selectedApp,
        isInDock = isInDock,
        canMoveLeft = index > 0,
        canMoveRight = index in 0 until list.lastIndex,
        onDismiss = onDismissContextMenu,
        onToggleDock = { onTogglePinToDock(selectedApp) },
        onMoveLeft = { onMoveApp(selectedApp, -1) },
        onMoveRight = { onMoveApp(selectedApp, +1) },
        onHideApp = { onHideApp(selectedApp) },
        onAppInfo = { onOpenAppInfo(selectedApp) },
        onUninstall = { onUninstallApp(selectedApp) },
      )
    }

    // 6. Wallpaper Picker Sheet
    AnimatedVisibility(
      visible = uiState.showWallpaperPicker,
      enter = slideInVertically { it } + fadeIn(),
      exit = slideOutVertically { it } + fadeOut(),
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      WallpaperPickerSheet(
        selectedStyle = prefs.wallpaperStyle,
        onSelectStyle = onSelectWallpaper,
        onPickGalleryPhoto = onPickGalleryWallpaper,
        onDismiss = onCloseWallpaperPicker,
      )
    }

    // 7. Icon Pack & Shape Picker Sheet
    AnimatedVisibility(
      visible = uiState.showIconPackPicker,
      enter = slideInVertically { it } + fadeIn(),
      exit = slideOutVertically { it } + fadeOut(),
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      IconPackPickerSheet(
        selectedPackId = prefs.iconPackId,
        selectedShape = prefs.iconShape,
        iconSizeDp = prefs.iconSizeDp,
        externalPacks = uiState.externalIconPacks,
        onSelectPack = onSelectIconPack,
        onSelectShape = onSelectIconShape,
        onChangeIconSize = onChangeIconSize,
        onRefreshPacks = onRefreshIconPacks,
        onDismiss = onCloseIconPackPicker,
      )
    }

    // 8. Home Settings Screen
    AnimatedVisibility(
      visible = uiState.showSettings,
      enter = slideInVertically { it / 4 } + fadeIn(),
      exit = slideOutVertically { it / 4 } + fadeOut(),
    ) {
      LauncherSettingsSheet(
        preferences = prefs,
        hiddenApps = uiState.hiddenApps,
        externalIconPacks = uiState.externalIconPacks,
        isDefaultLauncher = uiState.isDefaultLauncher,
        onToggleBlurIcons = onToggleBlurIcons,
        onToggleBlurWallpaper = onToggleBlurWallpaper,
        onChangeBlurRadius = onChangeBlurRadius,
        onSelectLaunchMode = onSelectLaunchMode,
        onSelectAnimationCurve = onSelectAnimationCurve,
        onChangeAnimationDuration = onChangeAnimationDuration,
        onChangeSpringDamping = onChangeSpringDamping,
        onChangeBackgroundScale = onChangeBackgroundScale,
        onSelectPageEffect = onSelectPageEffect,
        onSelectScrollMode = onSelectScrollMode,
        onToggleShowLabels = onToggleShowLabels,
        onToggleDockBackground = onToggleDockBackground,
        onToggleQuickFilterPill = onToggleQuickFilterPill,
        onToggleLockLayout = onToggleLockLayout,
        onSelectGridColumns = onSelectGridColumns,
        onSelectIconShape = onSelectIconShape,
        onChangeIconSize = onChangeIconSize,
        onUnhideApp = onUnhideApp,
        onOpenIconPackPicker = {
          onCloseSettings()
          onOpenIconPackPicker()
        },
        onOpenWallpaperPicker = {
          onCloseSettings()
          onOpenWallpaperPicker()
        },
        onRequestDefaultLauncher = onRequestDefaultLauncher,
        onDismiss = onCloseSettings,
      )
    }

    // 9. Snackbar Host
    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
    )
  }
}

@Composable
private fun QuickFilterBar(
  query: String,
  onQueryChange: (String) -> Unit,
  onClose: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(22.dp))
        .background(Color.Black.copy(alpha = 0.42f))
        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
        .padding(horizontal = 14.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Rounded.Search,
      contentDescription = null,
      tint = Color.White.copy(alpha = 0.85f),
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(10.dp))
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
      if (query.isEmpty()) {
        Text(
          text = stringResource(R.string.quick_filter_hint),
          color = Color.White.copy(alpha = 0.65f),
          style = MaterialTheme.typography.bodyMedium,
        )
      }
      BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
        cursorBrush = SolidColor(Color.White),
        modifier = Modifier.fillMaxWidth(),
      )
    }
    IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
      Icon(
        imageVector = Icons.Rounded.Close,
        contentDescription = stringResource(R.string.cd_clear),
        tint = Color.White,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

@Composable
private fun PageIndicatorDots(
  pageCount: Int,
  currentPage: Int,
  onSelectPage: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val desc = stringResource(R.string.page_indicator_description, currentPage + 1, pageCount)
  Row(
    modifier = modifier.fillMaxWidth().semantics { contentDescription = desc },
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier.clip(RoundedCornerShape(50))
          .background(Color.Black.copy(alpha = 0.22f))
          .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        for (i in 0 until pageCount) {
          val active = i == currentPage
          Box(
            modifier =
              Modifier.size(width = if (active) 16.dp else 7.dp, height = 7.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else Color.White.copy(alpha = 0.42f))
                .clickable { onSelectPage(i) }
          )
        }
      }
    }
  }
}

@Composable
private fun EditModeToolbar(
  onWallpaper: () -> Unit,
  onIconPack: () -> Unit,
  onSettings: () -> Unit,
  onDone: () -> Unit,
) {
  val barShape = RoundedCornerShape(28.dp)
  Box(
    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      modifier =
        Modifier.widthIn(max = 520.dp)
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .clip(barShape)
          .background(Brush.verticalGradient(colors = listOf(Color(0xEE1F2336), Color(0xEE141726))))
          .border(1.dp, Color.White.copy(alpha = 0.22f), barShape)
          .padding(horizontal = 10.dp, vertical = 10.dp)
          .testTag("edit_mode_toolbar"),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      EditBarAction(
        icon = Icons.Rounded.Wallpaper,
        label = stringResource(R.string.edit_mode_wallpaper),
        onClick = onWallpaper,
        testTag = "edit_wallpaper_button",
        modifier = Modifier.weight(1f),
      )
      EditBarAction(
        icon = Icons.Rounded.Palette,
        label = stringResource(R.string.edit_mode_icons),
        onClick = onIconPack,
        testTag = "edit_icon_pack_button",
        modifier = Modifier.weight(1f),
      )
      EditBarAction(
        icon = Icons.Rounded.Settings,
        label = stringResource(R.string.edit_mode_settings),
        onClick = onSettings,
        testTag = "edit_settings_button",
        modifier = Modifier.weight(1f),
      )
      EditBarAction(
        icon = Icons.Rounded.Check,
        label = stringResource(R.string.edit_mode_done),
        onClick = onDone,
        testTag = "edit_done_button",
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun EditBarAction(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .heightIn(min = 52.dp)
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .padding(vertical = 6.dp)
        .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
      color = Color.White,
    )
  }
}

@Composable
private fun EmptyDesktopView() {
  Column(
    modifier = Modifier.fillMaxSize().padding(32.dp).testTag("empty_desktop_view"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Rounded.Apps,
      contentDescription = null,
      tint = Color.White.copy(alpha = 0.8f),
      modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = stringResource(R.string.launcher_empty_title),
      style = MaterialTheme.typography.titleMedium,
      color = Color.White,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = stringResource(R.string.launcher_empty_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = Color.White.copy(alpha = 0.78f),
      textAlign = TextAlign.Center,
    )
  }
}
