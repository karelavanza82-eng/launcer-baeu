package com.example.ui.components

import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.model.WallpaperStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AuraWallpaper(
  style: WallpaperStyle,
  customPhotoPath: String?,
  blurEnabled: Boolean,
  blurRadiusDp: Int,
  transitionProgressProvider: () -> Float,
  parallaxProvider: () -> Float,
  modifier: Modifier = Modifier,
) {
  val customBitmap by
    produceState<ImageBitmap?>(initialValue = null, style, customPhotoPath) {
      value =
        if (style == WallpaperStyle.CUSTOM_PHOTO && !customPhotoPath.isNullOrBlank()) {
          withContext(Dispatchers.IO) {
            try {
              BitmapFactory.decodeFile(customPhotoPath)?.asImageBitmap()
            } catch (_: Exception) {
              null
            }
          }
        } else {
          null
        }
    }

  val hasComposeContent = style != WallpaperStyle.SYSTEM_WALLPAPER

  Box(modifier = modifier.fillMaxSize()) {
    if (hasComposeContent) {
      // Base crisp layer (moves only in GPU graphicsLayer: 0 recompositions)
      WallpaperContentLayer(
        style = style,
        customBitmap = customBitmap,
        modifier =
          Modifier.fillMaxSize().graphicsLayer {
            val p = transitionProgressProvider().coerceIn(0f, 1f)
            val zoom = 1.04f + (0.08f * p)
            scaleX = zoom
            scaleY = zoom
            translationX = -parallaxProvider() * size.width * 0.04f
          },
      )

      // Pre-configured hardware blur overlay faded in smoothly via GPU alpha (avoids per-frame blur kernel recompilation!)
      if (blurEnabled && blurRadiusDp > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        WallpaperContentLayer(
          style = style,
          customBitmap = customBitmap,
          modifier =
            Modifier.fillMaxSize()
              .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                val p = transitionProgressProvider().coerceIn(0f, 1f)
                val zoom = 1.04f + (0.08f * p)
                scaleX = zoom
                scaleY = zoom
                translationX = -parallaxProvider() * size.width * 0.04f
                alpha = p
              }
              .blur(blurRadiusDp.dp),
        )
      }
    }

    // Subtle atmospheric scrim during transition
    Box(
      modifier =
        Modifier.fillMaxSize()
          .graphicsLayer {
            alpha =
              if (blurEnabled) (transitionProgressProvider().coerceIn(0f, 1f) * 0.24f) else 0f
          }
          .background(Color.Black)
    )
  }
}

@Composable
private fun WallpaperContentLayer(
  style: WallpaperStyle,
  customBitmap: ImageBitmap?,
  modifier: Modifier = Modifier,
) {
  if (style == WallpaperStyle.CUSTOM_PHOTO && customBitmap != null) {
    Image(
      bitmap = customBitmap,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = modifier,
    )
  } else {
    val activeStyle =
      if (style == WallpaperStyle.CUSTOM_PHOTO || style == WallpaperStyle.SYSTEM_WALLPAPER) {
        WallpaperStyle.SUNSET_AURA
      } else {
        style
      }
    AuraMeshCanvas(style = activeStyle, modifier = modifier)
  }
}

@Composable
private fun AuraMeshCanvas(style: WallpaperStyle, modifier: Modifier = Modifier) {
  val orbs = style.orbs
  if (orbs.size < 6) return

  Box(
    modifier =
      modifier.drawWithCache {
        val w = size.width
        val h = size.height
        val bgBrush = Brush.verticalGradient(colors = listOf(orbs[0], orbs[1]))
        val orb4Brush =
          Brush.radialGradient(
            colors = listOf(orbs[5].copy(alpha = 0.86f), Color.Transparent),
            center = Offset(w * 0.16f, h * 0.18f),
            radius = w * 0.88f,
          )
        val orb1Brush =
          Brush.radialGradient(
            colors = listOf(orbs[2].copy(alpha = 0.90f), Color.Transparent),
            center = Offset(w * 0.82f, h * 0.36f),
            radius = w * 0.90f,
          )
        val orb2Brush =
          Brush.radialGradient(
            colors = listOf(orbs[3].copy(alpha = 0.92f), Color.Transparent),
            center = Offset(w * 0.22f, h * 0.62f),
            radius = w * 0.82f,
          )
        val orb3Brush =
          Brush.radialGradient(
            colors = listOf(orbs[4].copy(alpha = 0.68f), Color.Transparent),
            center = Offset(w * 0.84f, h * 0.84f),
            radius = w * 0.74f,
          )

        onDrawBehind {
          drawRect(brush = bgBrush)
          drawCircle(brush = orb4Brush, center = Offset(w * 0.16f, h * 0.18f), radius = w * 0.88f)
          drawCircle(brush = orb1Brush, center = Offset(w * 0.82f, h * 0.36f), radius = w * 0.90f)
          drawCircle(brush = orb2Brush, center = Offset(w * 0.22f, h * 0.62f), radius = w * 0.82f)
          drawCircle(brush = orb3Brush, center = Offset(w * 0.84f, h * 0.84f), radius = w * 0.74f)
        }
      }
  )
}
