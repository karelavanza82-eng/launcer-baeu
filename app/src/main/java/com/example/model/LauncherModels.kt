package com.example.model

import android.content.ComponentName
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

data class AppItem(
  val componentName: ComponentName,
  val packageName: String,
  val className: String,
  val label: String,
  val icon: ImageBitmap?,
  val dominantColor: Color,
  val isSystemApp: Boolean,
) {
  val key: String
    get() = "$packageName/$className"
}

enum class WallpaperStyle(val title: String, val previewColors: List<Color>) {
  SUNSET_AURA(
    title = "Sunset Aura",
    previewColors = listOf(Color(0xFF141738), Color(0xFF56328A), Color(0xFFE57042)),
  ),
  OCEAN_MIST(
    title = "Ocean Mist",
    previewColors = listOf(Color(0xFF0A192F), Color(0xFF145374), Color(0xFF38B6A9)),
  ),
  EMERALD_DUSK(
    title = "Emerald Dusk",
    previewColors = listOf(Color(0xFF0E1D1B), Color(0xFF1B5E4F), Color(0xFF7FD18B)),
  ),
  OBSIDIAN_VELVET(
    title = "Obsidian Velvet",
    previewColors = listOf(Color(0xFF0D0E14), Color(0xFF282B42), Color(0xFF5E689B)),
  ),
  ROSE_QUARTZ(
    title = "Rose Quartz",
    previewColors = listOf(Color(0xFF2B122A), Color(0xFF833267), Color(0xFFF39C82)),
  ),
  SYSTEM_WALLPAPER(
    title = "System Wallpaper",
    previewColors = listOf(Color(0xFF222531), Color(0xFF3A4159), Color(0xFF5D6785)),
  ),
}

enum class TransitionSpeed(val label: String, val dampingRatio: Float, val stiffness: Float) {
  SNAPPY("Snappy", dampingRatio = 0.86f, stiffness = 540f),
  SMOOTH("Smooth", dampingRatio = 0.82f, stiffness = 360f),
  RELAXED("Relaxed", dampingRatio = 0.80f, stiffness = 240f),
}

enum class DesktopScrollMode(val label: String) {
  PAGED("Paged Desktop"),
  VERTICAL("Vertical Scroll"),
}

data class LauncherPreferences(
  val dockKeys: List<String> = emptyList(),
  val hasInitializedDock: Boolean = false,
  val desktopOrderKeys: List<String> = emptyList(),
  val wallpaperStyle: WallpaperStyle = WallpaperStyle.SUNSET_AURA,
  val blurIconsOnTransition: Boolean = true,
  val blurWallpaperOnTransition: Boolean = true,
  val transitionSpeed: TransitionSpeed = TransitionSpeed.SMOOTH,
  val scrollMode: DesktopScrollMode = DesktopScrollMode.PAGED,
  val showLabels: Boolean = true,
  val gridColumns: Int = 4,
)

data class ActiveAppTransition(
  val app: AppItem,
  val sourceBounds: Rect,
  val isOpening: Boolean,
  val triggerId: Long = System.nanoTime(),
)

data class LauncherUiState(
  val isLoading: Boolean = true,
  val desktopApps: List<AppItem> = emptyList(),
  val dockApps: List<AppItem> = emptyList(),
  val preferences: LauncherPreferences = LauncherPreferences(),
  val isEditMode: Boolean = false,
  val activeTransition: ActiveAppTransition? = null,
  val contextMenuApp: AppItem? = null,
  val showSettings: Boolean = false,
  val showWallpaperPicker: Boolean = false,
  val isDefaultLauncher: Boolean = false,
)
