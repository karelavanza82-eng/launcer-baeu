package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppItem
import com.example.model.DesktopScrollMode
import com.example.model.LauncherUiState
import com.example.model.TransitionSpeed
import com.example.model.WallpaperStyle
import com.example.ui.LauncherViewModel
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.AppIconItem
import com.example.ui.components.AppTransitionOverlay
import com.example.ui.components.AuraWallpaper
import com.example.ui.components.FrostedDock
import kotlin.math.ceil
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
  onOpenAppInfo: (AppItem) -> Unit,
  onUninstallApp: (AppItem) -> Unit,
  onOpenSettings: () -> Unit,
  onCloseSettings: () -> Unit,
  onOpenWallpaperPicker: () -> Unit,
  onCloseWallpaperPicker: () -> Unit,
  onSelectWallpaper: (WallpaperStyle) -> Unit,
  onToggleBlurIcons: (Boolean) -> Unit,
  onToggleBlurWallpaper: (Boolean) -> Unit,
  onSelectTransitionSpeed: (TransitionSpeed) -> Unit,
  onSelectScrollMode: (DesktopScrollMode) -> Unit,
  onToggleShowLabels: (Boolean) -> Unit,
  onSelectGridColumns: (Int) -> Unit,
  onRequestDefaultLauncher: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val iconBoundsMap = remember { mutableStateMapOf<String, Rect>() }

  var transitionProgress by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(uiState.activeTransition) {
    if (uiState.activeTransition == null) {
      transitionProgress = 0f
      onBackgroundTransitionProgress(0f)
    }
  }

  val prefs = uiState.preferences
  val isTransitioning = uiState.activeTransition != null
  val iconBlurProgress =
    if (isTransitioning && prefs.blurIconsOnTransition) transitionProgress else 0f
  val wallpaperBlurProgress =
    if (isTransitioning && prefs.blurWallpaperOnTransition) transitionProgress else 0f

  val desktopScale by
    animateFloatAsState(
      targetValue = if (isTransitioning) 1f - (0.06f * transitionProgress) else 1f,
      animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f),
      label = "desktopScale",
    )

  BackHandler(enabled = uiState.isEditMode) { onExitEditMode() }

  val iconBlurModifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && iconBlurProgress > 0.01f) {
      Modifier.blur((18f * iconBlurProgress).dp)
    } else {
      Modifier
    }

  val columns = prefs.gridColumns.coerceIn(4, 5)

  Box(modifier = Modifier.fillMaxSize()) {
    // 1. Wallpaper layer (with parallax + optional API 31+ blur)
    AuraWallpaper(
      style = prefs.wallpaperStyle,
      blurProgress = wallpaperBlurProgress,
      blurEnabled = prefs.blurWallpaperOnTransition,
      parallaxOffset = 0f,
    )

    // 2. Desktop + Dock interactive workspace (no App Drawer: all apps live directly on desktop!)
    Column(
      modifier =
        Modifier.fillMaxSize()
          .statusBarsPadding()
          .pointerInput(uiState.isEditMode) {
            detectTapGestures(
              onTap = {
                if (uiState.isEditMode) onExitEditMode()
              },
              onLongPress = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (uiState.isEditMode) onExitEditMode() else onEnterEditMode()
              },
            )
          }
          .graphicsLayer {
            scaleX = desktopScale
            scaleY = desktopScale
            alpha = if (isTransitioning && prefs.blurIconsOnTransition) {
              1f - (0.28f * transitionProgress)
            } else {
              1f
            }
          }
          .then(iconBlurModifier)
    ) {
      // Top desktop grid area
      BoxWithConstraints(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter,
      ) {
        val availableHeight = maxHeight
        val cellHeightApprox = if (prefs.showLabels) 96.dp else 78.dp
        val rowsPerPage = (availableHeight / cellHeightApprox).toInt().coerceIn(3, 6)
        val appsPerPage = columns * rowsPerPage

        when {
          uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = Color.White)
            }
          }
          uiState.desktopApps.isEmpty() && uiState.dockApps.isEmpty() -> {
            EmptyDesktopView()
          }
          prefs.scrollMode == DesktopScrollMode.PAGED -> {
            val pages =
              remember(uiState.desktopApps, appsPerPage) {
                if (uiState.desktopApps.isEmpty()) {
                  listOf(emptyList())
                } else {
                  uiState.desktopApps.chunked(appsPerPage)
                }
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
              ) { pageIndex ->
                val pageApps = pages[pageIndex]
                val rows = ceil(pageApps.size / columns.toFloat()).toInt().coerceAtLeast(1)

                Column(
                  modifier =
                    Modifier.fillMaxSize()
                      .padding(horizontal = 14.dp, vertical = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp),
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
                              uiState.activeTransition?.app?.key == app.key,
                            iconSize = if (columns >= 5) 52.dp else 58.dp,
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

              // Page indicator dots above dock
              if (pages.size > 1) {
                PageIndicatorDots(
                  pageCount = pages.size,
                  currentPage = pagerState.currentPage,
                  onSelectPage = { target ->
                    scope.launch { pagerState.animateScrollToPage(target) }
                  },
                  modifier = Modifier.padding(vertical = 8.dp),
                )
              }
            }
          }
          else -> {
            // Vertical scroll desktop mode
            LazyVerticalGrid(
              columns = GridCells.Fixed(columns),
              modifier = Modifier.fillMaxSize().testTag("desktop_vertical_grid"),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              items(items = uiState.desktopApps, key = { it.key }) { app ->
                AppIconItem(
                  app = app,
                  showLabel = prefs.showLabels,
                  isEditMode = uiState.isEditMode,
                  isHiddenForTransition = uiState.activeTransition?.app?.key == app.key,
                  iconSize = if (columns >= 5) 52.dp else 58.dp,
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
            hiddenAppKey = uiState.activeTransition?.app?.key,
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
            onSettings = onOpenSettings,
            onDone = onExitEditMode,
          )
        }
      }
    }

    // 4. Icon-to-App Opening & Closing Spring Zoom Overlay
    uiState.activeTransition?.let { transition ->
      val liveBounds = iconBoundsMap[transition.app.key] ?: transition.sourceBounds
      AppTransitionOverlay(
        transition = transition.copy(sourceBounds = liveBounds),
        speed = prefs.transitionSpeed,
        onProgressChanged = { p ->
          transitionProgress = p
          onBackgroundTransitionProgress(
            if (prefs.blurWallpaperOnTransition) p else 0f
          )
        },
        onOpenAnimationPeak = onOpenAnimationPeak,
        onCloseAnimationFinished = onCloseAnimationFinished,
      )
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
        onDismiss = onCloseWallpaperPicker,
      )
    }

    // 7. Home Settings Screen
    AnimatedVisibility(
      visible = uiState.showSettings,
      enter = slideInVertically { it / 4 } + fadeIn(),
      exit = slideOutVertically { it / 4 } + fadeOut(),
    ) {
      LauncherSettingsSheet(
        preferences = prefs,
        isDefaultLauncher = uiState.isDefaultLauncher,
        onToggleBlurIcons = onToggleBlurIcons,
        onToggleBlurWallpaper = onToggleBlurWallpaper,
        onSelectTransitionSpeed = onSelectTransitionSpeed,
        onSelectScrollMode = onSelectScrollMode,
        onToggleShowLabels = onToggleShowLabels,
        onSelectGridColumns = onSelectGridColumns,
        onOpenWallpaperPicker = {
          onCloseSettings()
          onOpenWallpaperPicker()
        },
        onRequestDefaultLauncher = onRequestDefaultLauncher,
        onDismiss = onCloseSettings,
      )
    }

    // 8. Snackbar Host
    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
    )
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
    modifier =
      modifier.fillMaxWidth().semantics { contentDescription = desc },
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
                .background(
                  if (active) Color.White else Color.White.copy(alpha = 0.42f)
                )
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
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xEE1F2336), Color(0xEE141726))
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.22f), barShape)
          .padding(horizontal = 12.dp, vertical = 10.dp)
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
