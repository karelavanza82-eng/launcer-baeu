package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DesktopScrollMode
import com.example.model.LauncherPreferences
import com.example.model.TransitionSpeed

@Composable
fun LauncherSettingsSheet(
  preferences: LauncherPreferences,
  isDefaultLauncher: Boolean,
  onToggleBlurIcons: (Boolean) -> Unit,
  onToggleBlurWallpaper: (Boolean) -> Unit,
  onSelectTransitionSpeed: (TransitionSpeed) -> Unit,
  onSelectScrollMode: (DesktopScrollMode) -> Unit,
  onToggleShowLabels: (Boolean) -> Unit,
  onSelectGridColumns: (Int) -> Unit,
  onOpenWallpaperPicker: () -> Unit,
  onRequestDefaultLauncher: () -> Unit,
  onDismiss: () -> Unit,
) {
  BackHandler { onDismiss() }

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
        // Top header bar
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

        // Section 1: App Open & Close Animations (Blur Icons, Blur Wallpaper, Speed)
        SettingsSectionHeader(title = stringResource(R.string.settings_section_animations))
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surfaceContainer,
          tonalElevation = 2.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(vertical = 6.dp)) {
            SettingsToggleRow(
              icon = Icons.Rounded.BlurOn,
              title = stringResource(R.string.settings_blur_icons_title),
              subtitle = stringResource(R.string.settings_blur_icons_subtitle),
              checked = preferences.blurIconsOnTransition,
              onCheckedChange = onToggleBlurIcons,
              testTag = "toggle_blur_icons",
            )
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 20.dp),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
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
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 20.dp),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Rounded.Speed,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                  text = stringResource(R.string.settings_animation_speed_title),
                  style = MaterialTheme.typography.titleMedium,
                )
              }
              Spacer(modifier = Modifier.height(12.dp))
              SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransitionSpeed.entries.forEachIndexed { index, speed ->
                  SegmentedButton(
                    selected = preferences.transitionSpeed == speed,
                    onClick = { onSelectTransitionSpeed(speed) },
                    shape =
                      SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = TransitionSpeed.entries.size,
                      ),
                    modifier = Modifier.testTag("speed_${speed.name.lowercase()}"),
                  ) {
                    Text(speed.label)
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Section 2: Desktop & Dock
        SettingsSectionHeader(title = stringResource(R.string.settings_section_desktop))
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surfaceContainer,
          tonalElevation = 2.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
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
              Spacer(modifier = Modifier.height(12.dp))
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
                    Text(mode.label)
                  }
                }
              }
            }

            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 20.dp),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )

            SettingsToggleRow(
              icon = Icons.Rounded.Label,
              title = stringResource(R.string.settings_show_labels_title),
              subtitle = stringResource(R.string.settings_show_labels_subtitle),
              checked = preferences.showLabels,
              onCheckedChange = onToggleShowLabels,
              testTag = "toggle_show_labels",
            )

            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 20.dp),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
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
              Spacer(modifier = Modifier.height(12.dp))
              val columnChoices = listOf(4, 5)
              SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                columnChoices.forEachIndexed { index, cols ->
                  SegmentedButton(
                    selected = preferences.gridColumns == cols,
                    onClick = { onSelectGridColumns(cols) },
                    shape =
                      SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = columnChoices.size,
                      ),
                    modifier = Modifier.testTag("grid_columns_$cols"),
                  ) {
                    Text("$cols Columns")
                  }
                }
              }
            }

            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 20.dp),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )

            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .heightIn(min = 56.dp)
                  .clickable(onClick = onOpenWallpaperPicker)
                  .padding(horizontal = 20.dp, vertical = 14.dp)
                  .testTag("settings_open_wallpaper"),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.Wallpaper,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = stringResource(R.string.wallpaper_sheet_title),
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = preferences.wallpaperStyle.title,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Section 3: Default Home App
        SettingsSectionHeader(title = stringResource(R.string.settings_section_system))
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surfaceContainer,
          tonalElevation = 2.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
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
                      if (isDefaultLauncher) {
                        R.string.settings_already_default
                      } else {
                        R.string.settings_set_default_subtitle
                      }
                    ),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = onRequestDefaultLauncher,
              modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("set_default_home_button"),
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
