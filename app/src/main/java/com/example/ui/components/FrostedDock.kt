package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppItem
import com.example.model.IconShapeStyle

@Composable
fun FrostedDock(
  dockApps: List<AppItem>,
  isEditMode: Boolean,
  hiddenAppKey: String?,
  showDockBackground: Boolean,
  iconSize: Dp,
  iconShapeStyle: IconShapeStyle,
  onAppClick: (AppItem, Rect) -> Unit,
  onAppLongClick: (AppItem) -> Unit,
  onBoundsMeasured: (String, Rect) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (dockApps.isEmpty()) return

  val dockCd = stringResource(R.string.dock_section_description)
  val dockShape = RoundedCornerShape(30.dp)

  val backgroundModifier =
    if (showDockBackground) {
      Modifier.clip(dockShape)
        .background(
          brush =
            Brush.verticalGradient(
              colors = listOf(Color.White.copy(alpha = 0.24f), Color.White.copy(alpha = 0.14f))
            )
        )
        .border(
          width = 1.dp,
          brush =
            Brush.verticalGradient(
              colors = listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.12f))
            ),
          shape = dockShape,
        )
    } else {
      Modifier
    }

  Box(
    modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      modifier =
        Modifier.widthIn(max = 520.dp)
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .then(backgroundModifier)
          .padding(horizontal = 12.dp, vertical = 10.dp)
          .semantics { contentDescription = dockCd }
          .testTag("favorites_dock"),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      dockApps.forEach { app ->
        AppIconItem(
          app = app,
          showLabel = false,
          isEditMode = isEditMode,
          isHiddenForTransition = hiddenAppKey == app.key,
          iconSize = iconSize,
          iconShapeStyle = iconShapeStyle,
          onClick = { bounds -> onAppClick(app, bounds) },
          onLongClick = { onAppLongClick(app) },
          onBoundsMeasured = onBoundsMeasured,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}
