package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BuiltInIconPack
import com.example.model.ExternalIconPackInfo
import com.example.model.IconShapeStyle
import com.example.ui.components.iconShapeFor
import kotlin.math.roundToInt

@Composable
fun IconPackPickerSheet(
  selectedPackId: String,
  selectedShape: IconShapeStyle,
  iconSizeDp: Int,
  externalPacks: List<ExternalIconPackInfo>,
  onSelectPack: (String) -> Unit,
  onSelectShape: (IconShapeStyle) -> Unit,
  onChangeIconSize: (Int) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler { onDismiss() }

  Surface(
    modifier = modifier.fillMaxWidth().testTag("icon_pack_picker_sheet"),
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.97f),
    tonalElevation = 10.dp,
    shadowElevation = 24.dp,
  ) {
    Column(
      modifier =
        Modifier.navigationBarsPadding()
          .heightIn(max = 520.dp)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.icon_pack_sheet_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          Text(
            text = stringResource(R.string.icon_pack_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(48.dp).testTag("icon_pack_close_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.cd_close),
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Built-in icon styles
      Text(
        text = stringResource(R.string.icon_pack_builtin_section),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
      )

      LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(BuiltInIconPack.entries, key = { it.id }) { pack ->
          val selected = selectedPackId == pack.id
          Column(
            modifier =
              Modifier.width(86.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onSelectPack(pack.id) }
                .padding(vertical = 6.dp)
                .testTag("icon_pack_${pack.name.lowercase()}"),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Box(
              modifier =
                Modifier.size(58.dp)
                  .clip(iconShapeFor(selectedShape, 58.dp))
                  .background(Brush.linearGradient(pack.previewColors))
                  .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color =
                      if (selected) MaterialTheme.colorScheme.primary
                      else Color.White.copy(alpha = 0.28f),
                    shape = iconShapeFor(selectedShape, 58.dp),
                  ),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
              )
              if (selected) {
                Box(
                  modifier =
                    Modifier.align(Alignment.BottomEnd)
                      .padding(3.dp)
                      .size(18.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primary),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp),
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = pack.title,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.Center,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Installed third-party Android icon packs
      Text(
        text = stringResource(R.string.icon_pack_installed_section),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
      )

      if (externalPacks.isEmpty()) {
        Text(
          text = stringResource(R.string.icon_pack_none_installed),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        )
      } else {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(externalPacks, key = { it.packageName }) { pack ->
            val selected = selectedPackId == pack.id
            Column(
              modifier =
                Modifier.width(86.dp)
                  .clip(RoundedCornerShape(18.dp))
                  .clickable { onSelectPack(pack.id) }
                  .padding(vertical = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Box(
                modifier =
                  Modifier.size(58.dp)
                    .clip(iconShapeFor(selectedShape, 58.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .border(
                      width = if (selected) 2.5.dp else 1.dp,
                      color =
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                      shape = iconShapeFor(selectedShape, 58.dp),
                    ),
                contentAlignment = Alignment.Center,
              ) {
                if (pack.previewIcon != null) {
                  Image(
                    bitmap = pack.previewIcon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(58.dp),
                  )
                } else {
                  Icon(imageVector = Icons.Rounded.Apps, contentDescription = null)
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = pack.label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Icon Shape
      Text(
        text = stringResource(R.string.settings_icon_shape_title),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
      )

      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
      ) {
        IconShapeStyle.entries.forEachIndexed { index, shape ->
          SegmentedButton(
            selected = selectedShape == shape,
            onClick = { onSelectShape(shape) },
            shape =
              SegmentedButtonDefaults.itemShape(
                index = index,
                count = IconShapeStyle.entries.size,
              ),
            modifier = Modifier.testTag("icon_shape_${shape.name.lowercase()}"),
          ) {
            Text(shape.label, maxLines = 1)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Icon Size Dimension Slider
      Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
          text = stringResource(R.string.settings_icon_size_title, iconSizeDp),
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.primary,
        )
        Slider(
          value = iconSizeDp.toFloat(),
          onValueChange = { onChangeIconSize(it.roundToInt()) },
          valueRange = 48f..68f,
          steps = 9,
        )
      }
    }
  }
}
