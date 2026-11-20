package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.model.WallpaperStyle

@Composable
fun AuraWallpaper(
  style: WallpaperStyle,
  blurProgress: Float,
  blurEnabled: Boolean,
  parallaxOffset: Float,
  modifier: Modifier = Modifier,
) {
  val effectiveBlur = if (blurEnabled) blurProgress.coerceIn(0f, 1f) else 0f
  val blurModifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && effectiveBlur > 0.01f) {
      Modifier.blur((28f * effectiveBlur).dp)
    } else {
      Modifier
    }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .graphicsLayer {
          val zoom = 1f + (0.08f * effectiveBlur)
          scaleX = zoom
          scaleY = zoom
        }
        .then(blurModifier)
  ) {
    if (style != WallpaperStyle.SYSTEM_WALLPAPER) {
      AuraMeshCanvas(style = style, parallaxOffset = parallaxOffset)
    }
    if (effectiveBlur > 0.01f) {
      Box(
        modifier =
          Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.22f * effectiveBlur))
      )
    }
  }
}

@Composable
private fun AuraMeshCanvas(style: WallpaperStyle, parallaxOffset: Float) {
  val palette =
    when (style) {
      WallpaperStyle.SUNSET_AURA ->
        MeshPalette(
          baseTop = Color(0xFF0E1028),
          baseBottom = Color(0xFF1A1434),
          orb1 = Color(0xFF7140B5),
          orb2 = Color(0xFFE7723F),
          orb3 = Color(0xFFF3B05A),
          orb4 = Color(0xFF28469E),
        )
      WallpaperStyle.OCEAN_MIST ->
        MeshPalette(
          baseTop = Color(0xFF071324),
          baseBottom = Color(0xFF0A2540),
          orb1 = Color(0xFF165B88),
          orb2 = Color(0xFF29A8A8),
          orb3 = Color(0xFF72DFC9),
          orb4 = Color(0xFF1D3E78),
        )
      WallpaperStyle.EMERALD_DUSK ->
        MeshPalette(
          baseTop = Color(0xFF091614),
          baseBottom = Color(0xFF102822),
          orb1 = Color(0xFF1A5E4B),
          orb2 = Color(0xFF3DA374),
          orb3 = Color(0xFFA2DF8C),
          orb4 = Color(0xFF14424A),
        )
      WallpaperStyle.OBSIDIAN_VELVET ->
        MeshPalette(
          baseTop = Color(0xFF090A10),
          baseBottom = Color(0xFF121420),
          orb1 = Color(0xFF2E3556),
          orb2 = Color(0xFF535D94),
          orb3 = Color(0xFF8C97CE),
          orb4 = Color(0xFF1F233A),
        )
      WallpaperStyle.ROSE_QUARTZ ->
        MeshPalette(
          baseTop = Color(0xFF1D0D20),
          baseBottom = Color(0xFF2B122A),
          orb1 = Color(0xFF8C316A),
          orb2 = Color(0xFFDF687A),
          orb3 = Color(0xFFF6B092),
          orb4 = Color(0xFF53256B),
        )
      WallpaperStyle.SYSTEM_WALLPAPER ->
        MeshPalette(
          baseTop = Color.Transparent,
          baseBottom = Color.Transparent,
          orb1 = Color.Transparent,
          orb2 = Color.Transparent,
          orb3 = Color.Transparent,
          orb4 = Color.Transparent,
        )
    }

  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val shift = parallaxOffset * w * 0.08f

    drawRect(
      brush = Brush.verticalGradient(colors = listOf(palette.baseTop, palette.baseBottom))
    )

    // Upper-left deep ambient glow
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(palette.orb4.copy(alpha = 0.85f), Color.Transparent),
          center = Offset(w * 0.18f - shift, h * 0.20f),
          radius = w * 0.85f,
        ),
      center = Offset(w * 0.18f - shift, h * 0.20f),
      radius = w * 0.85f,
    )

    // Center-right sweeping luminous wave
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(palette.orb1.copy(alpha = 0.90f), Color.Transparent),
          center = Offset(w * 0.78f - shift * 1.2f, h * 0.38f),
          radius = w * 0.88f,
        ),
      center = Offset(w * 0.78f - shift * 1.2f, h * 0.38f),
      radius = w * 0.88f,
    )

    // Warm lower-left coral/accent sweep
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(palette.orb2.copy(alpha = 0.92f), Color.Transparent),
          center = Offset(w * 0.24f + shift, h * 0.62f),
          radius = w * 0.80f,
        ),
      center = Offset(w * 0.24f + shift, h * 0.62f),
      radius = w * 0.80f,
    )

    // Lower-right soft gold highlight
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(palette.orb3.copy(alpha = 0.65f), Color.Transparent),
          center = Offset(w * 0.85f - shift, h * 0.82f),
          radius = w * 0.72f,
        ),
      center = Offset(w * 0.85f - shift, h * 0.82f),
      radius = w * 0.72f,
    )
  }
}

private data class MeshPalette(
  val baseTop: Color,
  val baseBottom: Color,
  val orb1: Color,
  val orb2: Color,
  val orb3: Color,
  val orb4: Color,
)
