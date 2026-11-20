package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DesktopScrollMode
import com.example.model.LauncherPreferences
import com.example.model.TransitionSpeed
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
    val WALLPAPER_STYLE = stringPreferencesKey("wallpaper_style")
    val BLUR_ICONS = booleanPreferencesKey("blur_icons_on_transition")
    val BLUR_WALLPAPER = booleanPreferencesKey("blur_wallpaper_on_transition")
    val TRANSITION_SPEED = stringPreferencesKey("transition_speed")
    val SCROLL_MODE = stringPreferencesKey("scroll_mode")
    val SHOW_LABELS = booleanPreferencesKey("show_labels")
    val GRID_COLUMNS = intPreferencesKey("grid_columns")
  }

  val preferencesFlow: Flow<LauncherPreferences> =
    context.launcherDataStore.data.map { prefs ->
      LauncherPreferences(
        dockKeys = decodeList(prefs[Keys.DOCK_KEYS]),
        hasInitializedDock = prefs[Keys.HAS_INITIALIZED_DOCK] ?: false,
        desktopOrderKeys = decodeList(prefs[Keys.DESKTOP_ORDER_KEYS]),
        wallpaperStyle =
          prefs[Keys.WALLPAPER_STYLE]?.let { name ->
            WallpaperStyle.entries.firstOrNull { it.name == name }
          } ?: WallpaperStyle.SUNSET_AURA,
        blurIconsOnTransition = prefs[Keys.BLUR_ICONS] ?: true,
        blurWallpaperOnTransition = prefs[Keys.BLUR_WALLPAPER] ?: true,
        transitionSpeed =
          prefs[Keys.TRANSITION_SPEED]?.let { name ->
            TransitionSpeed.entries.firstOrNull { it.name == name }
          } ?: TransitionSpeed.SMOOTH,
        scrollMode =
          prefs[Keys.SCROLL_MODE]?.let { name ->
            DesktopScrollMode.entries.firstOrNull { it.name == name }
          } ?: DesktopScrollMode.PAGED,
        showLabels = prefs[Keys.SHOW_LABELS] ?: true,
        gridColumns = (prefs[Keys.GRID_COLUMNS] ?: 4).coerceIn(4, 5),
      )
    }

  suspend fun saveDockKeys(keys: List<String>, markInitialized: Boolean = true) {
    context.launcherDataStore.edit { prefs ->
      prefs[Keys.DOCK_KEYS] = encodeList(keys)
      if (markInitialized) {
        prefs[Keys.HAS_INITIALIZED_DOCK] = true
      }
    }
  }

  suspend fun saveDesktopOrderKeys(keys: List<String>) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.DESKTOP_ORDER_KEYS] = encodeList(keys) }
  }

  suspend fun setWallpaperStyle(style: WallpaperStyle) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.WALLPAPER_STYLE] = style.name }
  }

  suspend fun setBlurIconsOnTransition(enabled: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.BLUR_ICONS] = enabled }
  }

  suspend fun setBlurWallpaperOnTransition(enabled: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.BLUR_WALLPAPER] = enabled }
  }

  suspend fun setTransitionSpeed(speed: TransitionSpeed) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.TRANSITION_SPEED] = speed.name }
  }

  suspend fun setScrollMode(mode: DesktopScrollMode) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.SCROLL_MODE] = mode.name }
  }

  suspend fun setShowLabels(show: Boolean) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.SHOW_LABELS] = show }
  }

  suspend fun setGridColumns(columns: Int) {
    context.launcherDataStore.edit { prefs -> prefs[Keys.GRID_COLUMNS] = columns.coerceIn(4, 5) }
  }

  private fun encodeList(items: List<String>): String = items.joinToString(separator = "|")

  private fun decodeList(raw: String?): List<String> =
    if (raw.isNullOrBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
}
