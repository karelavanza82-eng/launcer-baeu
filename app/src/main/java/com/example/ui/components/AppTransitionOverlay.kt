package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.model.ActiveAppTransition
import com.example.model.TransitionSpeed
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppTransitionOverlay(
  transition: ActiveAppTransition,
  speed: TransitionSpeed,
  onProgressChanged: (Float) -> Unit,
  onOpenAnimationPeak: () -> Unit,
  onCloseAnimationFinished: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val progressAnim =
    remember(transition.triggerId) {
      Animatable(if (transition.isOpening) 0f else 1f)
    }

  LaunchedEffect(transition.triggerId) {
    snapshotFlow { progressAnim.value }.collectLatest { value ->
      onProgressChanged(value.coerceIn(0f, 1f))
    }
  }

  LaunchedEffect(transition.triggerId) {
    if (transition.isOpening) {
      var peakDispatched = false
      progressAnim.animateTo(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = speed.dampingRatio, stiffness = speed.stiffness),
      ) {
        if (!peakDispatched && value >= 0.90f) {
          peakDispatched = true
          onOpenAnimationPeak()
        }
      }
      if (!peakDispatched) {
        onOpenAnimationPeak()
      }
    } else {
      progressAnim.animateTo(
        targetValue = 0f,
        animationSpec = spring(dampingRatio = speed.dampingRatio, stiffness = speed.stiffness),
      )
      onCloseAnimationFinished()
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val fullW = with(density) { maxWidth.toPx() }
    val fullH = with(density) { maxHeight.toPx() }

    val startRect =
      if (!transition.sourceBounds.isEmpty) {
        transition.sourceBounds
      } else {
        val fallbackSize = with(density) { 58.dp.toPx() }
        Rect(
          left = (fullW - fallbackSize) / 2f,
          top = (fullH - fallbackSize) / 2f,
          right = (fullW + fallbackSize) / 2f,
          bottom = (fullH + fallbackSize) / 2f,
        )
      }

    val p = progressAnim.value.coerceIn(0f, 1f)
    val currentLeft = lerp(startRect.left, 0f, p)
    val currentTop = lerp(startRect.top, 0f, p)
    val currentWidth = lerp(startRect.width, fullW, p)
    val currentHeight = lerp(startRect.height, fullH, p)

    val cornerDp =
      if (p < 0.7f) {
        lerp(17f, 34f, p / 0.7f).dp
      } else {
        lerp(34f, 0f, (p - 0.7f) / 0.3f).dp
      }

    val splashTintTop = lerp(transition.app.dominantColor, Color.White, 0.78f)
    val splashTintBottom = lerp(transition.app.dominantColor, Color.White, 0.92f)
    val cardAlpha = (p * 1.45f).coerceIn(0f, 1f)
    val iconCrossFade = (1f - (p * 2.2f)).coerceIn(0f, 1f)
    val emblemReveal = ((p - 0.20f) / 0.55f).coerceIn(0f, 1f)

    val widthDp = with(density) { currentWidth.toDp() }
    val heightDp = with(density) { currentHeight.toDp() }

    Box(
      modifier =
        Modifier.offset { IntOffset(currentLeft.roundToInt(), currentTop.roundToInt()) }
          .size(width = widthDp, height = heightDp)
          .shadow(elevation = (18f * (1f - p * 0.5f)).dp, shape = RoundedCornerShape(cornerDp))
          .clip(RoundedCornerShape(cornerDp))
          .background(Color.Transparent),
      contentAlignment = Alignment.Center,
    ) {
      // Expanding app card surface (matching the tinted smooth splash expansion in the video)
      Box(
        modifier =
          Modifier.fillMaxSize()
            .graphicsLayer { alpha = cardAlpha }
            .background(
              Brush.verticalGradient(colors = listOf(splashTintTop, splashTintBottom))
            ),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier =
            Modifier.graphicsLayer {
              alpha = emblemReveal
              val s = 0.84f + (0.16f * emblemReveal)
              scaleX = s
              scaleY = s
            },
        ) {
          Box(
            modifier =
              Modifier.size(84.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(transition.app.dominantColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
          ) {
            if (transition.app.icon != null) {
              Image(
                bitmap = transition.app.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
              )
            } else {
              Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = null,
                tint = transition.app.dominantColor,
                modifier = Modifier.size(48.dp),
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = transition.app.label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF151724),
          )
        }
      }

      // Source icon that seamlessly expands out at the start of the zoom (or settles in on close)
      if (iconCrossFade > 0.01f) {
        Box(
          modifier =
            Modifier.fillMaxSize().graphicsLayer { alpha = iconCrossFade },
          contentAlignment = Alignment.Center,
        ) {
          if (transition.app.icon != null) {
            Image(
              bitmap = transition.app.icon,
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            Icon(
              imageVector = Icons.Rounded.Apps,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(36.dp),
            )
          }
        }
      }
    }
  }
}
