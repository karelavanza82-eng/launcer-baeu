package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.WallpaperStyle

@Composable
fun WallpaperPickerSheet(
  selectedStyle: WallpaperStyle,
  onSelectStyle: (WallpaperStyle) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler { onDismiss() }

  Surface(
    modifier = modifier.fillMaxWidth().testTag("wallpaper_picker_sheet"),
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f),
    tonalElevation = 10.dp,
    shadowElevation = 24.dp,
  ) {
    Column(modifier = Modifier.navigationBarsPadding().padding(vertical = 18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.wallpaper_sheet_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          Text(
            text = stringResource(R.string.wallpaper_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(48.dp).testTag("wallpaper_close_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.cd_close),
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        items(WallpaperStyle.entries) { style ->
          val isSelected = style == selectedStyle
          Column(
            modifier =
              Modifier.size(width = 96.dp, height = 164.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onSelectStyle(style) }
                .testTag("wallpaper_option_${style.name.lowercase()}"),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .weight(1f)
                  .aspectRatio(0.65f)
                  .clip(RoundedCornerShape(20.dp))
                  .background(Brush.linearGradient(colors = style.previewColors))
                  .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color =
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(20.dp),
                  ),
              contentAlignment = Alignment.Center,
            ) {
              if (style == WallpaperStyle.SYSTEM_WALLPAPER) {
                Icon(
                  imageVector = Icons.Rounded.Wallpaper,
                  contentDescription = null,
                  tint = Color.White.copy(alpha = 0.85f),
                  modifier = Modifier.size(28.dp),
                )
              }
              if (isSelected) {
                Box(
                  modifier =
                    Modifier.align(Alignment.BottomEnd)
                      .padding(8.dp)
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primary),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = style.title,
              style = MaterialTheme.typography.labelSmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    }
  }
}
