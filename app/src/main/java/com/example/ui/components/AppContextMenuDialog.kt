package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AppItem

@Composable
fun AppContextMenuDialog(
  app: AppItem,
  isInDock: Boolean,
  canMoveLeft: Boolean,
  canMoveRight: Boolean,
  onDismiss: () -> Unit,
  onToggleDock: () -> Unit,
  onMoveLeft: () -> Unit,
  onMoveRight: () -> Unit,
  onHideApp: () -> Unit,
  onAppInfo: () -> Unit,
  onUninstall: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().testTag("app_context_menu"),
      shape = RoundedCornerShape(26.dp),
      color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f),
      tonalElevation = 8.dp,
      shadowElevation = 18.dp,
    ) {
      Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(app.dominantColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
          ) {
            if (app.icon != null) {
              Image(
                bitmap = app.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(44.dp),
              )
            } else {
              Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = app.label,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = app.packageName,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        HorizontalDivider(
          modifier = Modifier.padding(vertical = 6.dp),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        )

        ContextMenuRow(
          icon = if (isInDock) Icons.Rounded.Dock else Icons.Rounded.PushPin,
          label =
            stringResource(
              if (isInDock) R.string.action_unpin_from_dock else R.string.action_pin_to_dock
            ),
          onClick = onToggleDock,
          testTag = "context_toggle_dock",
        )

        if (canMoveLeft) {
          ContextMenuRow(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            label = stringResource(R.string.action_move_left),
            onClick = onMoveLeft,
            testTag = "context_move_left",
          )
        }

        if (canMoveRight) {
          ContextMenuRow(
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            label = stringResource(R.string.action_move_right),
            onClick = onMoveRight,
            testTag = "context_move_right",
          )
        }

        ContextMenuRow(
          icon = Icons.Rounded.VisibilityOff,
          label = stringResource(R.string.action_hide_app),
          onClick = onHideApp,
          testTag = "context_hide_app",
        )

        ContextMenuRow(
          icon = Icons.Rounded.Info,
          label = stringResource(R.string.action_app_info),
          onClick = onAppInfo,
          testTag = "context_app_info",
        )

        if (!app.isSystemApp) {
          Spacer(modifier = Modifier.height(2.dp))
          ContextMenuRow(
            icon = Icons.Rounded.DeleteOutline,
            label = stringResource(R.string.action_uninstall),
            isDestructive = true,
            onClick = onUninstall,
            testTag = "context_uninstall",
          )
        }
      }
    }
  }
}

@Composable
private fun ContextMenuRow(
  icon: ImageVector,
  label: String,
  isDestructive: Boolean = false,
  onClick: () -> Unit,
  testTag: String,
) {
  val contentColor =
    if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 48.dp)
        .clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Start,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
      modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
      color = contentColor,
    )
  }
}
