package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.model.ActiveAppTransition
import com.example.model.AnimationCurveStyle
import com.example.model.IconShapeStyle
import kotlin.math.roundToInt

@Composable
fun AppTransitionOverlay(
  transition: ActiveAppTransition,
  progressAnim: Animatable<Float, AnimationVector1D>,
  curveStyle: AnimationCurveStyle,
  durationMs: Int,
  springDampingRatio: Float,
  iconShapeStyle: IconShapeStyle,
  onOpenAnimationPeak: () -> Unit,
  onCloseAnimationFinished: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val animSpec: AnimationSpec<Float> =
    remember(curveStyle, durationMs, springDampingRatio) {
      when (curveStyle) {
        AnimationCurveStyle.SPRING_BOUNCE -> {
          // Map durationMs (160..500) to spring stiffness (950..200)
          val normalized = ((500f - durationMs) / 340f).coerceIn(0f, 1f)
          val stiffness = lerp(220f, 880f, normalized)
          spring(dampingRatio = springDampingRatio.coerceIn(0.65f, 1.0f), stiffness = stiffness)
        }
        AnimationCurveStyle.FLUID_SMOOTH ->
          tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
        AnimationCurveStyle.CRISP_FAST ->
          tween(durationMillis = durationMs, easing = LinearOutSlowInEasing)
      }
    }

  LaunchedEffect(transition.triggerId) {
    progressAnim.snapTo(if (transition.isOpening) 0f else 1f)
    if (transition.isOpening) {
      var peakDispatched = false
      progressAnim.animateTo(targetValue = 1f, animationSpec = animSpec) {
        if (!peakDispatched && value >= 0.90f) {
          peakDispatched = true
          onOpenAnimationPeak()
        }
      }
      if (!peakDispatched) {
        onOpenAnimationPeak()
      }
    } else {
      progressAnim.animateTo(targetValue = 0f, animationSpec = animSpec)
      onCloseAnimationFinished()
    }
  }

  val splashTintTop =
    remember(transition.app.key) { lerp(transition.app.dominantColor, Color.White, 0.78f) }
  val splashTintBottom =
    remember(transition.app.key) { lerp(transition.app.dominantColor, Color.White, 0.92f) }

  // Zero-recomposition layout + GPU graphicsLayer interpolation
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .layout { measurable, constraints ->
          val fullW = constraints.maxWidth.toFloat()
          val fullH = constraints.maxHeight.toFloat()
          val p = progressAnim.value.coerceIn(0f, 1f)

          val startRect =
            if (!transition.sourceBounds.isEmpty) {
              transition.sourceBounds
            } else {
              val s = 58.dp.toPx()
              Rect(
                left = (fullW - s) / 2f,
                top = (fullH - s) / 2f,
                right = (fullW + s) / 2f,
                bottom = (fullH + s) / 2f,
              )
            }

          val currentLeft = lerp(startRect.left, 0f, p).roundToInt()
          val currentTop = lerp(startRect.top, 0f, p).roundToInt()
          val currentW = lerp(startRect.width, fullW, p).roundToInt().coerceAtLeast(1)
          val currentH = lerp(startRect.height, fullH, p).roundToInt().coerceAtLeast(1)

          val placeable = measurable.measure(Constraints.fixed(currentW, currentH))
          layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(currentLeft, currentTop)
          }
        }
        .graphicsLayer {
          val p = progressAnim.value.coerceIn(0f, 1f)
          val startCornerPx =
            when (iconShapeStyle) {
              IconShapeStyle.CIRCLE -> size.minDimension * 0.50f
              IconShapeStyle.SQUIRCLE -> size.minDimension * 0.28f
              IconShapeStyle.ROUNDED -> size.minDimension * 0.18f
              IconShapeStyle.TEARDROP -> size.minDimension * 0.36f
            }
          val cornerPx =
            if (p < 0.72f) {
              lerp(startCornerPx, 36.dp.toPx(), p / 0.72f)
            } else {
              lerp(36.dp.toPx(), 0f, (p - 0.72f) / 0.28f)
            }
          shape = RoundedCornerShape(cornerPx)
          clip = true
          shadowElevation = (16.dp.toPx() * (1f - p * 0.5f))
        }
  ) {
    // Expanding app splash card
    Box(
      modifier =
        Modifier.fillMaxSize()
          .graphicsLayer { alpha = (progressAnim.value * 1.45f).coerceIn(0f, 1f) }
          .background(Brush.verticalGradient(colors = listOf(splashTintTop, splashTintBottom))),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
          Modifier.graphicsLayer {
            val p = progressAnim.value.coerceIn(0f, 1f)
            val reveal = ((p - 0.22f) / 0.52f).coerceIn(0f, 1f)
            alpha = reveal
            val s = 0.84f + (0.16f * reveal)
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

    // Source app icon crossfading out seamlessly
    Box(
      modifier =
        Modifier.fillMaxSize().graphicsLayer {
          alpha = (1f - (progressAnim.value * 2.2f)).coerceIn(0f, 1f)
        },
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
