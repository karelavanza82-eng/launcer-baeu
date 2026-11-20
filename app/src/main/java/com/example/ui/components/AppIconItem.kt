package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
  app: AppItem,
  showLabel: Boolean,
  isEditMode: Boolean,
  isHiddenForTransition: Boolean,
  iconSize: Dp = 58.dp,
  onClick: (Rect) -> Unit,
  onLongClick: () -> Unit,
  onBoundsMeasured: (String, Rect) -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressScale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.88f else if (isEditMode) 0.94f else 1f,
      animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
      label = "iconPressScale",
    )

  val jiggleTransition = rememberInfiniteTransition(label = "editJiggle")
  val jiggleRotation by
    jiggleTransition.animateFloat(
      initialValue = -1.6f,
      targetValue = 1.6f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 150),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "jiggleRotation",
    )

  var latestBounds = remember(app.key) { Rect.Zero }
  val cdText = stringResource(R.string.cd_app_icon, app.label)

  Column(
    modifier =
      modifier
        .widthIn(min = 56.dp)
        .clip(RoundedCornerShape(18.dp))
        .combinedClickable(
          interactionSource = interactionSource,
          indication = ripple(bounded = false, radius = 42.dp),
          onClick = {
            if (isEditMode) {
              onLongClick()
            } else {
              onClick(latestBounds)
            }
          },
          onLongClick = onLongClick,
        )
        .padding(horizontal = 4.dp, vertical = 6.dp)
        .graphicsLayer {
          scaleX = pressScale
          scaleY = pressScale
          rotationZ = if (isEditMode) jiggleRotation else 0f
          alpha = if (isHiddenForTransition) 0f else 1f
        }
        .semantics { contentDescription = cdText }
        .testTag("app_icon_${app.packageName}"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(contentAlignment = Alignment.TopEnd) {
      Box(
        modifier =
          Modifier.size(iconSize)
            .onGloballyPositioned { coords ->
              val rect = coords.boundsInRoot()
              latestBounds = rect
              onBoundsMeasured(app.key, rect)
            }
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(17.dp),
              ambientColor = Color.Black.copy(alpha = 0.35f),
              spotColor = Color.Black.copy(alpha = 0.35f),
            )
            .clip(RoundedCornerShape(17.dp))
            .background(app.dominantColor.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center,
      ) {
        if (app.icon != null) {
          Image(
            bitmap = app.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(iconSize),
          )
        } else {
          Icon(
            imageVector = Icons.Rounded.Apps,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize * 0.56f),
          )
        }
      }

      if (isEditMode) {
        Box(
          modifier =
            Modifier.size(20.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.92f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.MoreHoriz,
            contentDescription = null,
            tint = Color(0xFF1A1C2A),
            modifier = Modifier.size(14.dp),
          )
        }
      }
    }

    if (showLabel) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = app.label,
        color = Color.White,
        style =
          MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            shadow =
              Shadow(
                color = Color.Black.copy(alpha = 0.65f),
                offset = Offset(0f, 1.5f),
                blurRadius = 6f,
              ),
          ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}
