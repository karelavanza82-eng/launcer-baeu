package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AnimationCurveStyle
import com.example.model.BuiltInIconPack
import com.example.model.DesktopScrollMode
import com.example.model.IconShapeStyle
import com.example.model.LaunchAnimationMode
import com.example.model.LauncherPreferences
import com.example.model.PageTransitionEffect
import com.example.model.WallpaperStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "aura_home_preferences")

class LauncherPreferencesRepository(private val context: Context) {

  private object Keys {
    val DOCK_KEYS = stringPreferencesKey("dock_keys")
    val HAS_INITIALIZED_DOCK = booleanPreferencesKey("has_initialized_dock")
    val DESKTOP_ORDER_KEYS = stringPreferencesKey("desktop_order_keys")
    val HIDDEN_KEYS = stringPreferencesKey("hidden_keys")
    val WALLPAPER_STYLE = stringPreferencesKey("wallpaper_style")
    val CUSTOM_WALLPAPER_PATH = stringPreferencesKey("custom_wallpaper_path")
    val ICON_PACK_ID = stringPreferencesKey("icon_pack_id")
    val ICON_SHAPE = stringPreferencesKey("icon_shape")
    val ICON_SIZE_DP = intPreferencesKey("icon_size_dp")
    val BLUR_ICONS = booleanPreferencesKey("blur_icons_on_transition")
    val BLUR_WALLPAPER = booleanPreferencesKey("blur_wallpaper_on_transition")
    val BLUR_RADIUS_DP = intPreferencesKey("blur_radius_dp")
    val LAUNCH_ANIMATION_MODE = stringPreferencesKey("launch_animation_mode")
    val ANIMATION_CURVE = stringPreferencesKey("animation_curve")
    val ANIMATION_DURATION_MS = intPreferencesKey("animation_duration_ms")
    val SPRING_DAMPING = floatPreferencesKey("spring_damping_ratio")
    val BACKGROUND_SCALE_MIN = floatPreferencesKey("background_scale_min")
    val PAGE_EFFECT = stringPreferencesKey("page_effect")
    val SCROLL_MODE = stringPreferencesKey("scroll_mode")
    val SHOW_LABELS = booleanPreferencesKey("show_labels")
    val SHOW_DOCK_BG = booleanPreferencesKey("show_dock_background")
    val SHOW_QUICK_FILTER = booleanPreferencesKey("show_quick_filter_pill")
    val LOCK_LAYOUT = booleanPreferencesKey("lock_layout")
    val GRID_COLUMNS = intPreferencesKey("grid_columns")
  }

  val preferencesFlow: Flow<LauncherPreferences> =
    context.launcherDataStore.data.map { prefs ->
      LauncherPreferences(
        dockKeys = decodeList(prefs[Keys.DOCK_KEYS]),
        hasInitializedDock = prefs[Keys.HAS_INITIALIZED_DOCK] ?: false,
        desktopOrderKeys = decodeList(prefs[Keys.DESKTOP_ORDER_KEYS]),
        hiddenKeys = decodeList(prefs[Keys.HIDDEN_KEYS]).toSet(),
        wallpaperStyle =
          prefs[Keys.WALLPAPER_STYLE]?.let { name ->
            WallpaperStyle.entries.firstOrNull { it.name == name }
          } ?: WallpaperStyle.SUNSET_AURA,
        customWallpaperPath = prefs[Keys.CUSTOM_WALLPAPER_PATH],
        iconPackId = prefs[Keys.ICON_PACK_ID] ?: BuiltInIconPack.SYSTEM.id,
        iconShape =
          prefs[Keys.ICON_SHAPE]?.let { name ->
            IconShapeStyle.entries.firstOrNull { it.name == name }
          } ?: IconShapeStyle.SQUIRCLE,
        iconSizeDp = (prefs[Keys.ICON_SIZE_DP] ?: 58).coerceIn(48, 68),
        blurIconsOnTransition = prefs[Keys.BLUR_ICONS] ?: true,
        blurWallpaperOnTransition = prefs[Keys.BLUR_WALLPAPER] ?: true,
        blurRadiusDp = (prefs[Keys.BLUR_RADIUS_DP] ?: 22).coerceIn(0, 32),
        launchAnimationMode =
          prefs[Keys.LAUNCH_ANIMATION_MODE]?.let { name ->
            LaunchAnimationMode.entries.firstOrNull { it.name == name }
          } ?: LaunchAnimationMode.SCALE_UP_ICON,
        animationCurve =
          prefs[Keys.ANIMATION_CURVE]?.let { name ->
            AnimationCurveStyle.entries.firstOrNull { it.name == name }
          } ?: AnimationCurveStyle.SPRING_BOUNCE,
        animationDurationMs = (prefs[Keys.ANIMATION_DURATION_MS] ?: 290).coerceIn(160, 500),
        springDampingRatio = (prefs[Keys.SPRING_DAMPING] ?: 0.82f).coerceIn(0.65f, 1.0f),
        backgroundScaleMin = (prefs[Keys.BACKGROUND_SCALE_MIN] ?: 0.94f).coerceIn(0.86f, 1.0f),
        pageEffect =
          prefs[Keys.PAGE_EFFECT]?.let { name ->
            PageTransitionEffect.entries.firstOrNull { it.name == name }
          } ?: PageTransitionEffect.DEPTH_STACK,
        scrollMode =
          prefs[Keys.SCROLL_MODE]?.let { name ->
            DesktopScrollMode.entries.firstOrNull { it.name == name }
          } ?: DesktopScrollMode.PAGED,
        showLabels = prefs[Keys.SHOW_LABELS] ?: true,
        showDockBackground = prefs[Keys.SHOW_DOCK_BG] ?: true,
        showQuickFilterPill = prefs[Keys.SHOW_QUICK_FILTER] ?: true,
        lockLayout = prefs[Keys.LOCK_LAYOUT] ?: false,
        gridColumns = (prefs[Keys.GRID_COLUMNS] ?: 4).coerceIn(4, 5),
      )
    }

  suspend fun saveDockKeys(keys: List<String>, markInitialized: Boolean = true) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.DOCK_KEYS] = encodeList(keys)
      if (markInitialized) prefs[Keys.HAS_INITIALIZED_DOCK] = true
    }
  }

  suspend fun saveDesktopOrderKeys(keys: List<String>) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.DESKTOP_ORDER_KEYS] = encodeList(keys) }
  }

  suspend fun saveHiddenKeys(keys: Set<String>) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.HIDDEN_KEYS] = encodeList(keys.toList()) }
  }

  suspend fun setWallpaperStyle(style: WallpaperStyle) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.WALLPAPER_STYLE] = style.name }
  }

  suspend fun setCustomWallpaperPath(path: String) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.CUSTOM_WALLPAPER_PATH] = path
      prefs[Keys.WALLPAPER_STYLE] = WallpaperStyle.CUSTOM_PHOTO.name
    }
  }

  suspend fun setIconPackId(packId: String) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.ICON_PACK_ID] = packId }
  }

  suspend fun setIconShape(shape: IconShapeStyle) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.ICON_SHAPE] = shape.name }
  }

  suspend fun setIconSizeDp(sizeDp: Int) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.ICON_SIZE_DP] = sizeDp.coerceIn(48, 68) }
  }

  suspend fun setBlurIconsOnTransition(enabled: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.BLUR_ICONS] = enabled }
  }

  suspend fun setBlurWallpaperOnTransition(enabled: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.BLUR_WALLPAPER] = enabled }
  }

  suspend fun setBlurRadiusDp(radiusDp: Int) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.BLUR_RADIUS_DP] = radiusDp.coerceIn(0, 32) }
  }

  suspend fun setLaunchAnimationMode(mode: LaunchAnimationMode) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.LAUNCH_ANIMATION_MODE] = mode.name }
  }

  suspend fun setAnimationCurve(curve: AnimationCurveStyle) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.ANIMATION_CURVE] = curve.name }
  }

  suspend fun setAnimationDurationMs(durationMs: Int) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.ANIMATION_DURATION_MS] = durationMs.coerceIn(160, 500)
    }
  }

  suspend fun setSpringDampingRatio(damping: Float) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.SPRING_DAMPING] = damping.coerceIn(0.65f, 1.0f)
    }
  }

  suspend fun setBackgroundScaleMin(scaleMin: Float) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.BACKGROUND_SCALE_MIN] = scaleMin.coerceIn(0.86f, 1.0f)
    }
  }

  suspend fun setPageEffect(effect: PageTransitionEffect) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.PAGE_EFFECT] = effect.name }
  }

  suspend fun setScrollMode(mode: DesktopScrollMode) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.SCROLL_MODE] = mode.name }
  }

  suspend fun setShowLabels(show: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.SHOW_LABELS] = show }
  }

  suspend fun setShowDockBackground(show: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.SHOW_DOCK_BG] = show }
  }

  suspend fun setShowQuickFilterPill(show: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.SHOW_QUICK_FILTER] = show }
  }

  suspend fun setLockLayout(locked: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.LOCK_LAYOUT] = locked }
  }

  suspend fun setGridColumns(columns: Int) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.GRID_COLUMNS] = columns.coerceIn(4, 5) }
  }

  private fun encodeList(items: List<String>): String = items.joinToString(separator = "|")

  private fun decodeList(raw: String?): List<String> =
    if (raw.isNullOrBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
}
