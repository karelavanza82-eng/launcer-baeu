package com.example

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppItem
import com.example.model.LauncherPreferences
import com.example.model.LauncherUiState
import com.example.ui.screens.LauncherHomeScreen
import com.example.ui.theme.AuraHomeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `app name matches Aura Home`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aura Home", appName)
  }

  @Test
  fun `manifest declares HOME intent category`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val homeIntent =
      Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        `package` = context.packageName
      }
    val resolved =
      context.packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
    assertTrue("Expected MainActivity to resolve CATEGORY_HOME", resolved.isNotEmpty())
  }

  @Test
  fun `launcher desktop renders pager, dock, icon pack picker, wallpaper picker, and settings`() {
    val desktopApps =
      List(6) { idx ->
        AppItem(
          componentName = ComponentName("com.demo.app$idx", "com.demo.app$idx.MainActivity"),
          packageName = "com.demo.app$idx",
          className = "com.demo.app$idx.MainActivity",
          label = "App $idx",
          icon = null,
          dominantColor = Color(0xFF4A6CF7),
          isSystemApp = false,
        )
      }
    val dockApps =
      List(2) { idx ->
        AppItem(
          componentName = ComponentName("com.dock.app$idx", "com.dock.app$idx.MainActivity"),
          packageName = "com.dock.app$idx",
          className = "com.dock.app$idx.MainActivity",
          label = "Dock $idx",
          icon = null,
          dominantColor = Color(0xFFE46F44),
          isSystemApp = true,
        )
      }

    composeTestRule.setContent {
      AuraHomeTheme {
        var state by remember {
          mutableStateOf(
            LauncherUiState(
              isLoading = false,
              desktopApps = desktopApps,
              dockApps = dockApps,
              preferences = LauncherPreferences(),
              isEditMode = false,
            )
          )
        }
        val snackbarHostState = remember { SnackbarHostState() }

        LauncherHomeScreen(
          uiState = state,
          snackbarHostState = snackbarHostState,
          resetToFirstPageSignal = 0,
          onLaunchApp = { _, _ -> },
          onOpenAnimationPeak = {},
          onCloseAnimationFinished = {},
          onBackgroundTransitionProgress = {},
          onEnterEditMode = { state = state.copy(isEditMode = true) },
          onExitEditMode = { state = state.copy(isEditMode = false) },
          onOpenContextMenu = { app -> state = state.copy(contextMenuApp = app) },
          onDismissContextMenu = { state = state.copy(contextMenuApp = null) },
          onTogglePinToDock = {},
          onMoveApp = { _, _ -> },
          onHideApp = {},
          onUnhideApp = {},
          onOpenAppInfo = {},
          onUninstallApp = {},
          onQuickFilterActiveChange = { active ->
            state = state.copy(isQuickFilterActive = active)
          },
          onQuickFilterQueryChange = { q -> state = state.copy(quickFilterQuery = q) },
          onOpenSettings = { state = state.copy(showSettings = true, isEditMode = false) },
          onCloseSettings = { state = state.copy(showSettings = false) },
          onOpenWallpaperPicker = {
            state = state.copy(showWallpaperPicker = true, isEditMode = false)
          },
          onCloseWallpaperPicker = { state = state.copy(showWallpaperPicker = false) },
          onOpenIconPackPicker = {
            state = state.copy(showIconPackPicker = true, isEditMode = false)
          },
          onCloseIconPackPicker = { state = state.copy(showIconPackPicker = false) },
          onSelectWallpaper = { style ->
            state = state.copy(preferences = state.preferences.copy(wallpaperStyle = style))
          },
          onPickGalleryWallpaper = {},
          onSelectIconPack = { packId ->
            state = state.copy(preferences = state.preferences.copy(iconPackId = packId))
          },
          onSelectIconShape = { shape ->
            state = state.copy(preferences = state.preferences.copy(iconShape = shape))
          },
          onChangeIconSize = { size ->
            state = state.copy(preferences = state.preferences.copy(iconSizeDp = size))
          },
          onToggleBlurIcons = { enabled ->
            state =
              state.copy(preferences = state.preferences.copy(blurIconsOnTransition = enabled))
          },
          onToggleBlurWallpaper = { enabled ->
            state =
              state.copy(
                preferences = state.preferences.copy(blurWallpaperOnTransition = enabled)
              )
          },
          onChangeBlurRadius = { radius ->
            state = state.copy(preferences = state.preferences.copy(blurRadiusDp = radius))
          },
          onSelectLaunchMode = { mode ->
            state = state.copy(preferences = state.preferences.copy(launchAnimationMode = mode))
          },
          onSelectAnimationCurve = { curve ->
            state = state.copy(preferences = state.preferences.copy(animationCurve = curve))
          },
          onChangeAnimationDuration = { duration ->
            state = state.copy(preferences = state.preferences.copy(animationDurationMs = duration))
          },
          onChangeSpringDamping = { damping ->
            state = state.copy(preferences = state.preferences.copy(springDampingRatio = damping))
          },
          onChangeBackgroundScale = { scale ->
            state = state.copy(preferences = state.preferences.copy(backgroundScaleMin = scale))
          },
          onSelectPageEffect = { effect ->
            state = state.copy(preferences = state.preferences.copy(pageEffect = effect))
          },
          onSelectScrollMode = { mode ->
            state = state.copy(preferences = state.preferences.copy(scrollMode = mode))
          },
          onToggleShowLabels = { show ->
            state = state.copy(preferences = state.preferences.copy(showLabels = show))
          },
          onToggleDockBackground = { show ->
            state = state.copy(preferences = state.preferences.copy(showDockBackground = show))
          },
          onToggleQuickFilterPill = { show ->
            state = state.copy(preferences = state.preferences.copy(showQuickFilterPill = show))
          },
          onToggleLockLayout = { locked ->
            state = state.copy(preferences = state.preferences.copy(lockLayout = locked))
          },
          onSelectGridColumns = { cols ->
            state = state.copy(preferences = state.preferences.copy(gridColumns = cols))
          },
          onRefreshIconPacks = {},
          onRequestDefaultLauncher = {},
        )
      }
    }

    composeTestRule.onNodeWithTag("desktop_pager").assertIsDisplayed()
    composeTestRule.onNodeWithTag("app_icon_com.demo.app0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("favorites_dock").assertIsDisplayed()
    composeTestRule.onNodeWithTag("app_icon_com.dock.app0").assertIsDisplayed()

    // Long press empty area on pager to enter edit mode
    composeTestRule.onNodeWithTag("desktop_pager").performTouchInput { longClick() }
    composeTestRule.onNodeWithTag("edit_mode_toolbar").assertIsDisplayed()

    // Open Icon Pack Picker
    composeTestRule.onNodeWithTag("edit_icon_pack_button").performClick()
    composeTestRule.onNodeWithTag("icon_pack_picker_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("icon_pack_close_button").performClick()

    // Re-enter edit mode and open Settings
    composeTestRule.onNodeWithTag("desktop_pager").performTouchInput { longClick() }
    composeTestRule.onNodeWithTag("edit_settings_button").performClick()
    composeTestRule.onNodeWithTag("launcher_settings_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_blur_icons").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_blur_wallpaper").assertIsDisplayed()
  }
}
