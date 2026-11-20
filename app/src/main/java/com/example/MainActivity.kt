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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        val defaultHomeLauncher =
          rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
          ) {
            viewModel.onLauncherResumed()
          }

        LaunchedEffect(Unit) {
          viewModel.messages.collectLatest { msg -> snackbarHostState.showSnackbar(msg) }
        }

        // Standard Home launcher behavior: Back on the root desktop stays on Home
        BackHandler(
          enabled =
            !uiState.isEditMode &&
              !uiState.showSettings &&
              !uiState.showWallpaperPicker &&
              uiState.contextMenuApp == null
        ) {
          homeResetSignal++
        }

        LauncherHomeScreen(
          uiState = uiState,
          snackbarHostState = snackbarHostState,
          resetToFirstPageSignal = homeResetSignal,
          onLaunchApp = { app, bounds -> viewModel.startAppLaunch(app, bounds) },
          onOpenAnimationPeak = { viewModel.onOpenAnimationCompleted(this@MainActivity) },
          onCloseAnimationFinished = { viewModel.onCloseAnimationCompleted() },
          onBackgroundTransitionProgress = { progress ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              val useWindowBlur =
                uiState.preferences.wallpaperStyle == WallpaperStyle.SYSTEM_WALLPAPER &&
                  uiState.preferences.blurWallpaperOnTransition
              val radiusPx = if (useWindowBlur) (48f * progress).toInt() else 0
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
          onOpenAppInfo = { app -> viewModel.openAppInfo(this@MainActivity, app) },
          onUninstallApp = { app -> viewModel.uninstallApp(this@MainActivity, app) },
          onOpenSettings = { viewModel.setShowSettings(true) },
          onCloseSettings = { viewModel.setShowSettings(false) },
          onOpenWallpaperPicker = { viewModel.setShowWallpaperPicker(true) },
          onCloseWallpaperPicker = { viewModel.setShowWallpaperPicker(false) },
          onSelectWallpaper = { style -> viewModel.setWallpaperStyle(style) },
          onToggleBlurIcons = { enabled -> viewModel.setBlurIconsOnTransition(enabled) },
          onToggleBlurWallpaper = { enabled -> viewModel.setBlurWallpaperOnTransition(enabled) },
          onSelectTransitionSpeed = { speed -> viewModel.setTransitionSpeed(speed) },
          onSelectScrollMode = { mode -> viewModel.setScrollMode(mode) },
          onToggleShowLabels = { show -> viewModel.setShowLabels(show) },
          onSelectGridColumns = { cols -> viewModel.setGridColumns(cols) },
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
