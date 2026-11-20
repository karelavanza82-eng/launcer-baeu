package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AnimationCurveStyle
import com.example.model.AppItem
import com.example.model.BuiltInIconPack
import com.example.model.DesktopScrollMode
import com.example.model.ExternalIconPackInfo
import com.example.model.IconShapeStyle
import com.example.model.LauncherPreferences
import com.example.model.PageTransitionEffect
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LauncherSettingsSheet(
  preferences: LauncherPreferences,
  hiddenApps: List<AppItem>,
  externalIconPacks: List<ExternalIconPackInfo>,
  isDefaultLauncher: Boolean,
  onToggleBlurIcons: (Boolean) -> Unit,
  onToggleBlurWallpaper: (Boolean) -> Unit,
  onChangeBlurRadius: (Int) -> Unit,
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
  onSelectIconShape: (IconShapeStyle) -> Unit,
  onChangeIconSize: (Int) -> Unit,
  onUnhideApp: (AppItem) -> Unit,
  onOpenIconPackPicker: () -> Unit,
  onOpenWallpaperPicker: () -> Unit,
  onRequestDefaultLauncher: () -> Unit,
  onDismiss: () -> Unit,
) {
  BackHandler { onDismiss() }

  val currentPackLabel =
    BuiltInIconPack.fromId(preferences.iconPackId)?.title
      ?: externalIconPacks
        .firstOrNull { it.id == preferences.iconPackId }
        ?.label
      ?: BuiltInIconPack.SYSTEM.title

  Surface(
    modifier = Modifier.fillMaxSize().testTag("launcher_settings_screen"),
    color = MaterialTheme.colorScheme.surface,
  ) {
    Box(
      modifier = Modifier.fillMaxSize().safeDrawingPadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 600.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onDismiss,
            modifier =
              Modifier.size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .testTag("settings_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = stringResource(R.string.cd_back),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          )
        }

        // Section 1: Animations & Physics Dimensions
        SettingsSectionHeader(title = stringResource(R.string.settings_section_animations))
        SettingsCard {
          SettingsToggleRow(
            icon = Icons.Rounded.BlurOn,
            title = stringResource(R.string.settings_blur_icons_title),
            subtitle = stringResource(R.string.settings_blur_icons_subtitle),
            checked = preferences.blurIconsOnTransition,
            onCheckedChange = onToggleBlurIcons,
            testTag = "toggle_blur_icons",
          )
          SettingsDivider()
          SettingsToggleRow(
            icon = Icons.Rounded.Wallpaper,
            title = stringResource(R.string.settings_blur_wallpaper_title),
            subtitle = stringResource(R.string.settings_blur_wallpaper_subtitle),
            checked = preferences.blurWallpaperOnTransition,
            onCheckedChange = onToggleBlurWallpaper,
            testTag = "toggle_blur_wallpaper",
          )
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Text(
              text = stringResource(R.string.blur_api_note),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
          }
          SettingsDivider()
          SettingsSliderRow(
            title = stringResource(R.string.settings_blur_intensity_title, preferences.blurRadiusDp),
            value = preferences.blurRadiusDp.toFloat(),
            valueRange = 0f..32f,
            onValueChange = { onChangeBlurRadius(it.roundToInt()) },
          )
          SettingsDivider()
          Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.Animation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = stringResource(R.string.settings_animation_curve_title),
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
              AnimationCurveStyle.entries.forEachIndexed { index, curve ->
                SegmentedButton(
                  selected = preferences.animationCurve == curve,
                  onClick = { onSelectAnimationCurve(curve) },
                  shape =
                    SegmentedButtonDefaults.itemShape(
                      index = index,
                      count = AnimationCurveStyle.entries.size,
                    ),
                ) {
                  Text(curve.label, maxLines = 1)
                }
              }
            }
          }
          SettingsDivider()
          SettingsSliderRow(
            title =
              stringResource(
                R.string.settings_animation_duration_title,
                preferences.animationDurationMs,
              ),
            value = preferences.animationDurationMs.toFloat(),
            valueRange = 160f..500f,
            onValueChange = { onChangeAnimationDuration((it / 10f).roundToInt() * 10) },
          )
          if (preferences.animationCurve == AnimationCurveStyle.SPRING_BOUNCE) {
            val bouncePercent = (((1.0f - preferences.springDampingRatio) / 0.35f) * 100f).roundToInt().coerceIn(0, 100)
            SettingsSliderRow(
              title = stringResource(R.string.settings_spring_bounce_title, bouncePercent),
              value = 1.0f - preferences.springDampingRatio,
              valueRange = 0f..0.35f,
              onValueChange = { onChangeSpringDamping(1.0f - it) },
            )
          }
          SettingsSliderRow(
            title =
              stringResource(
                R.string.settings_background_scale_title,
                (preferences.backgroundScaleMin * 100f).roundToInt(),
              ),
            value = preferences.backgroundScaleMin,
            valueRange = 0.86f..1.0f,
            onValueChange = onChangeBackgroundScale,
          )
          SettingsDivider()
          Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
              text = stringResource(R.string.settings_page_effect_title),
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              PageTransitionEffect.entries.forEach { effect ->
                FilterChip(
                  selected = preferences.pageEffect == effect,
                  onClick = { onSelectPageEffect(effect) },
                  label = { Text(effect.label) },
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Icons, Shapes & Wallpapers
        SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))
        SettingsCard {
          SettingsActionRow(
            icon = Icons.Rounded.Palette,
            title = stringResource(R.string.settings_icon_pack_title),
            subtitle = currentPackLabel,
            onClick = onOpenIconPackPicker,
            testTag = "settings_open_icon_pack",
          )
          SettingsDivider()
          SettingsActionRow(
            icon = Icons.Rounded.Wallpaper,
            title = stringResource(R.string.wallpaper_sheet_title),
            subtitle = preferences.wallpaperStyle.title,
            onClick = onOpenWallpaperPicker,
            testTag = "settings_open_wallpaper",
          )
          SettingsDivider()
          Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
              text = stringResource(R.string.settings_icon_shape_title),
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
              IconShapeStyle.entries.forEachIndexed { index, shape ->
                SegmentedButton(
                  selected = preferences.iconShape == shape,
                  onClick = { onSelectIconShape(shape) },
                  shape =
                    SegmentedButtonDefaults.itemShape(
                      index = index,
                      count = IconShapeStyle.entries.size,
                    ),
                ) {
                  Text(shape.label, maxLines = 1)
                }
              }
            }
          }
          SettingsSliderRow(
            title = stringResource(R.string.settings_icon_size_title, preferences.iconSizeDp),
            value = preferences.iconSizeDp.toFloat(),
            valueRange = 48f..68f,
            onValueChange = { onChangeIconSize(it.roundToInt()) },
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Desktop & Dock
        SettingsSectionHeader(title = stringResource(R.string.settings_section_desktop))
        SettingsCard {
          Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.ViewCarousel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = stringResource(R.string.settings_scroll_mode_title),
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
              DesktopScrollMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                  selected = preferences.scrollMode == mode,
                  onClick = { onSelectScrollMode(mode) },
                  shape =
                    SegmentedButtonDefaults.itemShape(
                      index = index,
                      count = DesktopScrollMode.entries.size,
                    ),
                  modifier = Modifier.testTag("scroll_mode_${mode.name.lowercase()}"),
                ) {
                  Text(mode.label, maxLines = 1)
                }
              }
            }
          }
          SettingsDivider()
          Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.GridView,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = stringResource(R.string.settings_grid_columns_title),
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            val columnChoices = listOf(4, 5)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
              columnChoices.forEachIndexed { index, cols ->
                SegmentedButton(
                  selected = preferences.gridColumns == cols,
                  onClick = { onSelectGridColumns(cols) },
                  shape =
                    SegmentedButtonDefaults.itemShape(index = index, count = columnChoices.size),
                  modifier = Modifier.testTag("grid_columns_$cols"),
                ) {
                  Text("$cols Columns")
                }
              }
            }
          }
          SettingsDivider()
          SettingsToggleRow(
            icon = Icons.AutoMirrored.Rounded.Label,
            title = stringResource(R.string.settings_show_labels_title),
            subtitle = stringResource(R.string.settings_show_labels_subtitle),
            checked = preferences.showLabels,
            onCheckedChange = onToggleShowLabels,
            testTag = "toggle_show_labels",
          )
          SettingsDivider()
          SettingsToggleRow(
            icon = Icons.Rounded.Dock,
            title = stringResource(R.string.settings_dock_glass_title),
            subtitle = stringResource(R.string.settings_dock_glass_subtitle),
            checked = preferences.showDockBackground,
            onCheckedChange = onToggleDockBackground,
            testTag = "toggle_dock_glass",
          )
          SettingsDivider()
          SettingsToggleRow(
            icon = Icons.Rounded.Search,
            title = stringResource(R.string.settings_quick_filter_title),
            subtitle = stringResource(R.string.settings_quick_filter_subtitle),
            checked = preferences.showQuickFilterPill,
            onCheckedChange = onToggleQuickFilterPill,
            testTag = "toggle_quick_filter",
          )
          SettingsDivider()
          SettingsToggleRow(
            icon = Icons.Rounded.Lock,
            title = stringResource(R.string.settings_lock_layout_title),
            subtitle = stringResource(R.string.settings_lock_layout_subtitle),
            checked = preferences.lockLayout,
            onCheckedChange = onToggleLockLayout,
            testTag = "toggle_lock_layout",
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: Hidden Apps Manager
        SettingsSectionHeader(
          title = stringResource(R.string.settings_hidden_apps_title, hiddenApps.size)
        )
        SettingsCard {
          if (hiddenApps.isEmpty()) {
            Text(
              text = stringResource(R.string.settings_hidden_apps_empty),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(20.dp),
            )
          } else {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = stringResource(R.string.settings_hidden_apps_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.height(10.dp))
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                hiddenApps.forEach { app ->
                  AssistChip(
                    onClick = { onUnhideApp(app) },
                    label = { Text(app.label) },
                    leadingIcon = {
                      if (app.icon != null) {
                        Image(
                          bitmap = app.icon,
                          contentDescription = null,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.size(18.dp).clip(CircleShape),
                        )
                      } else {
                        Icon(
                          imageVector = Icons.Rounded.Apps,
                          contentDescription = null,
                          modifier = Modifier.size(18.dp),
                        )
                      }
                    },
                    trailingIcon = {
                      Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                      )
                    },
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 5: Default Home App
        SettingsSectionHeader(title = stringResource(R.string.settings_section_system))
        SettingsCard {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (isDefaultLauncher) Icons.Rounded.CheckCircle else Icons.Rounded.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = stringResource(R.string.settings_set_default_title),
                  style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text =
                    stringResource(
                      if (isDefaultLauncher) R.string.settings_already_default
                      else R.string.settings_set_default_subtitle
                    ),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = onRequestDefaultLauncher,
              modifier =
                Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("set_default_home_button"),
              shape = RoundedCornerShape(16.dp),
            ) {
              Text(stringResource(R.string.settings_choose_home_action))
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
  )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation = 2.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
  }
}

@Composable
private fun SettingsDivider() {
  HorizontalDivider(
    modifier = Modifier.padding(horizontal = 20.dp),
    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
  )
}

@Composable
private fun SettingsToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 64.dp)
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(22.dp),
      )
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
private fun SettingsActionRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 58.dp)
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun SettingsSliderRow(
  title: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
) {
  Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    )
    Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
  }
}
