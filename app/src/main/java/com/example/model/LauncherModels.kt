package com.example.model

import android.content.ComponentName
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
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

enum class BuiltInIconPack(
  val id: String,
  val title: String,
  val subtitle: String,
  val previewColors: List<Color>,
) {
  SYSTEM("builtin:SYSTEM", "System Default", "Original app icons", listOf(Color(0xFF4A6CF7), Color(0xFF7B52AB))),
  SQUIRCLE_GLASS("builtin:SQUIRCLE_GLASS", "Aura Glass", "Soft specular sheen", listOf(Color(0xFF6B85FF), Color(0xFFE46F44))),
  OBSIDIAN_DARK("builtin:OBSIDIAN_DARK", "Obsidian Dark", "Deep slate tile backdrop", listOf(Color(0xFF171A29), Color(0xFF394164))),
  PASTEL_BLOOM("builtin:PASTEL_BLOOM", "Pastel Bloom", "Warm tinted glow", listOf(Color(0xFFF3B05A), Color(0xFFDF687A))),
  NEON_EDGE("builtin:NEON_EDGE", "Neon Edge", "Luminous rim accent", listOf(Color(0xFF12162A), Color(0xFF29D4C5))),
  MONOCHROME_INK("builtin:MONOCHROME_INK", "Mono Ink", "Minimal desaturated ink", listOf(Color(0xFF2E3240), Color(0xFF9BA3B8)));

  companion object {
    fun fromId(id: String): BuiltInIconPack? = entries.firstOrNull { it.id == id }
  }
}

@Immutable
data class ExternalIconPackInfo(
  val packageName: String,
  val label: String,
  val previewIcon: ImageBitmap?,
) {
  val id: String
    get() = "pkg:$packageName"
}

enum class IconShapeStyle(val label: String, val cornerFraction: Float) {
  SQUIRCLE("Squircle", 0.28f),
  CIRCLE("Circle", 0.50f),
  ROUNDED("Rounded", 0.18f),
  TEARDROP("Drop", 0.36f),
}

enum class WallpaperStyle(
  val title: String,
  val previewColors: List<Color>,
  val orbs: List<Color> = emptyList(),
) {
  SUNSET_AURA(
    title = "Sunset Aura",
    previewColors = listOf(Color(0xFF121536), Color(0xFF65359C), Color(0xFFE87240)),
    orbs = listOf(Color(0xFF0E1028), Color(0xFF1A1434), Color(0xFF7140B5), Color(0xFFE7723F), Color(0xFFF3B05A), Color(0xFF28469E)),
  ),
  MIDNIGHT_CORAL(
    title = "Midnight Coral",
    previewColors = listOf(Color(0xFF0B1124), Color(0xFF2E2A6E), Color(0xFFFF6E61)),
    orbs = listOf(Color(0xFF090D1C), Color(0xFF191632), Color(0xFF3E358E), Color(0xFFFF6E61), Color(0xFFFFB27D), Color(0xFF1D4F91)),
  ),
  OCEAN_MIST(
    title = "Ocean Mist",
    previewColors = listOf(Color(0xFF071324), Color(0xFF145374), Color(0xFF38B6A9)),
    orbs = listOf(Color(0xFF071324), Color(0xFF0A2540), Color(0xFF165B88), Color(0xFF29A8A8), Color(0xFF72DFC9), Color(0xFF1D3E78)),
  ),
  EMERALD_DUSK(
    title = "Emerald Dusk",
    previewColors = listOf(Color(0xFF091614), Color(0xFF1B5E4F), Color(0xFF7FD18B)),
    orbs = listOf(Color(0xFF091614), Color(0xFF102822), Color(0xFF1A5E4B), Color(0xFF3DA374), Color(0xFFA2DF8C), Color(0xFF14424A)),
  ),
  OBSIDIAN_VELVET(
    title = "Obsidian Velvet",
    previewColors = listOf(Color(0xFF090A10), Color(0xFF282B42), Color(0xFF6A77B8)),
    orbs = listOf(Color(0xFF090A10), Color(0xFF121420), Color(0xFF2E3556), Color(0xFF535D94), Color(0xFF8C97CE), Color(0xFF1F233A)),
  ),
  ROSE_QUARTZ(
    title = "Rose Quartz",
    previewColors = listOf(Color(0xFF220E24), Color(0xFF8C316A), Color(0xFFF39C82)),
    orbs = listOf(Color(0xFF1D0D20), Color(0xFF2B122A), Color(0xFF8C316A), Color(0xFFDF687A), Color(0xFFF6B092), Color(0xFF53256B)),
  ),
  CYBER_NEON(
    title = "Cyber Neon",
    previewColors = listOf(Color(0xFF0C0922), Color(0xFF7D2AE8), Color(0xFF00E5FF)),
    orbs = listOf(Color(0xFF0B071E), Color(0xFF170E3A), Color(0xFF7D2AE8), Color(0xFF00D2FF), Color(0xFFFF3CAC), Color(0xFF2B35AF)),
  ),
  GOLDEN_HOUR(
    title = "Golden Hour",
    previewColors = listOf(Color(0xFF261214), Color(0xFFB54B2A), Color(0xFFF9C86C)),
    orbs = listOf(Color(0xFF1E0F12), Color(0xFF331817), Color(0xFFB54B2A), Color(0xFFE88A3E), Color(0xFFF9C86C), Color(0xFF6B283C)),
  ),
  ARCTIC_AURORA(
    title = "Arctic Aurora",
    previewColors = listOf(Color(0xFF071426), Color(0xFF146B6E), Color(0xFF76F0B9)),
    orbs = listOf(Color(0xFF06101E), Color(0xFF0D2038), Color(0xFF146B6E), Color(0xFF42C9A2), Color(0xFF84F2D6), Color(0xFF29468E)),
  ),
  CRIMSON_ECLIPSE(
    title = "Crimson Eclipse",
    previewColors = listOf(Color(0xFF180912), Color(0xFF8F1D3D), Color(0xFFEB6B4B)),
    orbs = listOf(Color(0xFF14070F), Color(0xFF290C1C), Color(0xFF8F1D3D), Color(0xFFD64550), Color(0xFFF28B66), Color(0xFF4A1845)),
  ),
  LAVENDER_HAZE(
    title = "Lavender Haze",
    previewColors = listOf(Color(0xFF1B163A), Color(0xFF6E62C7), Color(0xFFD2A8FF)),
    orbs = listOf(Color(0xFF161230), Color(0xFF241D4D), Color(0xFF6E62C7), Color(0xFF9F84F2), Color(0xFFD9B8FF), Color(0xFF44489B)),
  ),
  COBALT_PULSE(
    title = "Cobalt Pulse",
    previewColors = listOf(Color(0xFF071333), Color(0xFF1E4FBD), Color(0xFF60AFFF)),
    orbs = listOf(Color(0xFF061029), Color(0xFF0B1F4D), Color(0xFF1E4FBD), Color(0xFF3B82F6), Color(0xFF7CC6FF), Color(0xFF312E81)),
  ),
  FOREST_MOSS(
    title = "Forest Moss",
    previewColors = listOf(Color(0xFF0C1710), Color(0xFF2C5E3B), Color(0xFF9BC96B)),
    orbs = listOf(Color(0xFF09120C), Color(0xFF14261B), Color(0xFF2C5E3B), Color(0xFF568D50), Color(0xFFA7D477), Color(0xFF1E4239)),
  ),
  WARM_TERRACOTTA(
    title = "Terracotta",
    previewColors = listOf(Color(0xFF231311), Color(0xFF964234), Color(0xFFE09F7D)),
    orbs = listOf(Color(0xFF1C0F0D), Color(0xFF2E1916), Color(0xFF964234), Color(0xFFC4644B), Color(0xFFE5A989), Color(0xFF5E2F3A)),
  ),
  CUSTOM_PHOTO(
    title = "Gallery Photo",
    previewColors = listOf(Color(0xFF2B314A), Color(0xFF4F5D8C), Color(0xFFE46F44)),
  ),
  SYSTEM_WALLPAPER(
    title = "System Wallpaper",
    previewColors = listOf(Color(0xFF222531), Color(0xFF3A4159), Color(0xFF5D6785)),
  );

  companion object {
    val proceduralThemes: List<WallpaperStyle>
      get() = entries.filter { it.orbs.size == 6 }
  }
}

enum class PageTransitionEffect(val label: String) {
  SLIDE("Slide"),
  DEPTH_STACK("Depth"),
  PARALLAX_FADE("Fade"),
  CUBE("Cube"),
  SCALE_ZOOM("Zoom"),
}

enum class AnimationCurveStyle(val label: String) {
  SPRING_BOUNCE("Spring Bounce"),
  FLUID_SMOOTH("Fluid Smooth"),
  CRISP_FAST("Crisp Linear"),
}

enum class LaunchAnimationMode(val label: String) {
  SCALE_UP_ICON("Window Scale"),
  CLIP_REVEAL_ICON("Clip Reveal"),
  MORPH_CARD("Morph Card"),
}

enum class DesktopScrollMode(val label: String) {
  PAGED("Paged Desktop"),
  VERTICAL("Vertical Scroll"),
}

@Immutable
data class LauncherPreferences(
  val dockKeys: List<String> = emptyList(),
  val hasInitializedDock: Boolean = false,
  val desktopOrderKeys: List<String> = emptyList(),
  val hiddenKeys: Set<String> = emptySet(),
  val wallpaperStyle: WallpaperStyle = WallpaperStyle.SUNSET_AURA,
  val customWallpaperPath: String? = null,
  val iconPackId: String = BuiltInIconPack.SYSTEM.id,
  val iconShape: IconShapeStyle = IconShapeStyle.SQUIRCLE,
  val iconSizeDp: Int = 58,
  val blurIconsOnTransition: Boolean = true,
  val blurWallpaperOnTransition: Boolean = true,
  val blurRadiusDp: Int = 22,
  val launchAnimationMode: LaunchAnimationMode = LaunchAnimationMode.SCALE_UP_ICON,
  val animationCurve: AnimationCurveStyle = AnimationCurveStyle.SPRING_BOUNCE,
  val animationDurationMs: Int = 290,
  val springDampingRatio: Float = 0.82f,
  val backgroundScaleMin: Float = 0.94f,
  val pageEffect: PageTransitionEffect = PageTransitionEffect.DEPTH_STACK,
  val scrollMode: DesktopScrollMode = DesktopScrollMode.PAGED,
  val showLabels: Boolean = true,
  val showDockBackground: Boolean = true,
  val showQuickFilterPill: Boolean = true,
  val lockLayout: Boolean = false,
  val gridColumns: Int = 4,
)

@Immutable
data class ActiveAppTransition(
  val app: AppItem,
  val sourceBounds: Rect,
  val isOpening: Boolean,
  val triggerId: Long = System.nanoTime(),
)

@Immutable
data class LauncherUiState(
  val isLoading: Boolean = true,
  val desktopApps: List<AppItem> = emptyList(),
  val dockApps: List<AppItem> = emptyList(),
  val hiddenApps: List<AppItem> = emptyList(),
  val externalIconPacks: List<ExternalIconPackInfo> = emptyList(),
  val preferences: LauncherPreferences = LauncherPreferences(),
  val isEditMode: Boolean = false,
  val quickFilterQuery: String = "",
  val isQuickFilterActive: Boolean = false,
  val activeTransition: ActiveAppTransition? = null,
  val contextMenuApp: AppItem? = null,
  val showSettings: Boolean = false,
  val showWallpaperPicker: Boolean = false,
  val showIconPackPicker: Boolean = false,
  val isDefaultLauncher: Boolean = false,
)
