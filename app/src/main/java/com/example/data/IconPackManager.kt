package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.example.model.BuiltInIconPack
import com.example.model.ExternalIconPackInfo
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class IconPackManager(private val context: Context) {

  private val packageManager: PackageManager = context.packageManager

  data class LoadedExternalPack(
    val packageName: String,
    val resources: Resources,
    val componentToDrawable: Map<String, String>,
    val packageToDrawable: Map<String, String>,
    val iconBacks: List<String>,
    val iconMask: String?,
    val iconUpon: String?,
    val scaleFactor: Float,
  )

  fun discoverExternalIconPacks(): List<ExternalIconPackInfo> {
    val actions =
      listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.anddoes.launcher.THEME",
        "com.teslacoilsw.launcher.THEME",
      )
    val discovered = LinkedHashMap<String, ExternalIconPackInfo>()
    for (action in actions) {
      val intent = Intent(action)
      val list =
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
          } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
          }
        } catch (_: Exception) {
          emptyList()
        }
      for (info in list) {
        val pkg = info.activityInfo?.packageName ?: continue
        if (pkg == context.packageName || discovered.containsKey(pkg)) continue
        val label =
          try {
            info.loadLabel(packageManager).toString().trim().ifEmpty { pkg }
          } catch (_: Exception) {
            pkg
          }
        val iconBitmap =
          try {
            info.loadIcon(packageManager)?.let { drawableToBitmap(it, 128).asImageBitmap() }
          } catch (_: Exception) {
            null
          }
        discovered[pkg] = ExternalIconPackInfo(packageName = pkg, label = label, previewIcon = iconBitmap)
      }
    }
    return discovered.values.sortedBy { it.label.lowercase() }
  }

  fun loadExternalPack(packageName: String): LoadedExternalPack? {
    return try {
      val res = packageManager.getResourcesForApplication(packageName)
      val componentMap = HashMap<String, String>(512)
      val packageMap = HashMap<String, String>(256)
      val iconBacks = mutableListOf<String>()
      var iconMask: String? = null
      var iconUpon: String? = null
      var scaleFactor = 1f

      val xmlResId = res.getIdentifier("appfilter", "xml", packageName)
      val parser: XmlPullParser? =
        when {
          xmlResId != 0 -> res.getXml(xmlResId)
          else -> {
            try {
              val stream = res.assets.open("appfilter.xml")
              XmlPullParserFactory.newInstance().newPullParser().apply {
                setInput(stream, "UTF-8")
              }
            } catch (_: Exception) {
              null
            }
          }
        }

      if (parser != null) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
          if (eventType == XmlPullParser.START_TAG) {
            when (parser.name) {
              "item" -> {
                val compAttr = parser.getAttributeValue(null, "component")
                val drawableAttr = parser.getAttributeValue(null, "drawable")
                if (!compAttr.isNullOrBlank() && !drawableAttr.isNullOrBlank()) {
                  val normalized =
                    compAttr
                      .removePrefix("ComponentInfo{")
                      .removeSuffix("}")
                  componentMap[normalized] = drawableAttr
                  val pkgOnly = normalized.substringBefore("/")
                  if (pkgOnly.isNotEmpty() && !packageMap.containsKey(pkgOnly)) {
                    packageMap[pkgOnly] = drawableAttr
                  }
                }
              }
              "iconback" -> {
                for (i in 0 until parser.attributeCount) {
                  val v = parser.getAttributeValue(i)
                  if (!v.isNullOrBlank()) iconBacks.add(v)
                }
              }
              "iconmask" -> {
                iconMask = parser.getAttributeValue(null, "img1")
                  ?: parser.getAttributeValue(null, "img")
              }
              "iconupon" -> {
                iconUpon = parser.getAttributeValue(null, "img1")
                  ?: parser.getAttributeValue(null, "img")
              }
              "scale" -> {
                parser.getAttributeValue(null, "factor")?.toFloatOrNull()?.let {
                  scaleFactor = it.coerceIn(0.45f, 1.1f)
                }
              }
            }
          }
          eventType = parser.next()
        }
      }

      LoadedExternalPack(
        packageName = packageName,
        resources = res,
        componentToDrawable = componentMap,
        packageToDrawable = packageMap,
        iconBacks = iconBacks,
        iconMask = iconMask,
        iconUpon = iconUpon,
        scaleFactor = scaleFactor,
      )
    } catch (_: Exception) {
      null
    }
  }

  fun resolveStyledIcon(
    component: ComponentName,
    originalDrawable: Drawable?,
    iconPackId: String,
    externalPack: LoadedExternalPack?,
  ): Pair<Bitmap?, Color> {
    val size = 192
    val rawBitmap = originalDrawable?.let { drawableToBitmap(it, size) }
    val dominant = rawBitmap?.let { extractDominantColor(it) } ?: Color(0xFF4A5588)

    if (iconPackId.startsWith("pkg:") && externalPack != null) {
      val compKey = "${component.packageName}/${component.className}"
      val drawableName =
        externalPack.componentToDrawable[compKey]
          ?: externalPack.packageToDrawable[component.packageName]
      if (!drawableName.isNullOrBlank()) {
        val packDrawable = loadDrawableByName(externalPack.resources, externalPack.packageName, drawableName)
        if (packDrawable != null) {
          val themedBitmap = drawableToBitmap(packDrawable, size)
          return themedBitmap to extractDominantColor(themedBitmap)
        }
      }
      if (rawBitmap != null &&
        (externalPack.iconBacks.isNotEmpty() || externalPack.iconMask != null || externalPack.iconUpon != null)
      ) {
        val composited = compositeWithPackTemplate(rawBitmap, externalPack, size)
        return composited to extractDominantColor(composited)
      }
      return rawBitmap to dominant
    }

    val builtIn = BuiltInIconPack.fromId(iconPackId) ?: BuiltInIconPack.SYSTEM
    if (rawBitmap == null || builtIn == BuiltInIconPack.SYSTEM) {
      return rawBitmap to dominant
    }

    val styled = applyBuiltInStyle(rawBitmap, dominant, builtIn, size)
    return styled to dominant
  }

  private fun applyBuiltInStyle(
    source: Bitmap,
    dominant: Color,
    style: BuiltInIconPack,
    size: Int,
  ): Bitmap {
    val out = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())

    when (style) {
      BuiltInIconPack.SYSTEM -> {
        canvas.drawBitmap(source, 0f, 0f, null)
      }
      BuiltInIconPack.SQUIRCLE_GLASS -> {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.shader =
          LinearGradient(
            0f,
            0f,
            size.toFloat(),
            size.toFloat(),
            dominant.copy(alpha = 0.28f).toArgb(),
            dominant.copy(alpha = 0.55f).toArgb(),
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(rect, bgPaint)
        canvas.drawBitmap(source, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        val sheen = Paint(Paint.ANTI_ALIAS_FLAG)
        sheen.shader =
          LinearGradient(
            0f,
            0f,
            0f,
            size * 0.55f,
            0x44FFFFFF,
            0x00FFFFFF,
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(rect, sheen)
      }
      BuiltInIconPack.OBSIDIAN_DARK -> {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.shader =
          LinearGradient(0f, 0f, size.toFloat(), size.toFloat(), 0xFF1A1D2E.toInt(), 0xFF0E101B.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRect(rect, bgPaint)
        val inset = size * 0.16f
        val dst = RectF(inset, inset, size - inset, size - inset)
        canvas.drawBitmap(source, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
      }
      BuiltInIconPack.PASTEL_BLOOM -> {
        val softColor =
          Color(
            red = (dominant.red + 0.55f).coerceIn(0f, 1f),
            green = (dominant.green + 0.55f).coerceIn(0f, 1f),
            blue = (dominant.blue + 0.55f).coerceIn(0f, 1f),
          )
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.shader =
          RadialGradient(
            size * 0.35f,
            size * 0.30f,
            size * 0.9f,
            softColor.toArgb(),
            dominant.copy(alpha = 0.78f).toArgb(),
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(rect, bgPaint)
        val inset = size * 0.12f
        val dst = RectF(inset, inset, size - inset, size - inset)
        canvas.drawBitmap(source, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
      }
      BuiltInIconPack.NEON_EDGE -> {
        canvas.drawColor(0xFF0D1020.toInt())
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        glowPaint.shader =
          RadialGradient(
            size * 0.5f,
            size * 0.5f,
            size * 0.6f,
            dominant.copy(alpha = 0.5f).toArgb(),
            0x00000000,
            Shader.TileMode.CLAMP,
          )
        canvas.drawRect(rect, glowPaint)
        val inset = size * 0.15f
        val dst = RectF(inset, inset, size - inset, size - inset)
        canvas.drawBitmap(source, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
        val ring =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 6f
            color = dominant.copy(alpha = 0.92f).toArgb()
          }
        canvas.drawRoundRect(RectF(5f, 5f, size - 5f, size - 5f), 42f, 42f, ring)
      }
      BuiltInIconPack.MONOCHROME_INK -> {
        canvas.drawColor(0xFF1F2330.toInt())
        val matrix = ColorMatrix().apply { setSaturation(0.08f) }
        val paint =
          Paint(Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) }
        val inset = size * 0.10f
        val dst = RectF(inset, inset, size - inset, size - inset)
        canvas.drawBitmap(source, null, dst, paint)
      }
    }
    return out
  }

  private fun compositeWithPackTemplate(
    raw: Bitmap,
    pack: LoadedExternalPack,
    size: Int,
  ): Bitmap {
    val result = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val fullRect = RectF(0f, 0f, size.toFloat(), size.toFloat())

    val backName = pack.iconBacks.firstOrNull()
    val backDrawable = backName?.let { loadDrawableByName(pack.resources, pack.packageName, it) }
    backDrawable?.let {
      val backBmp = drawableToBitmap(it, size)
      canvas.drawBitmap(backBmp, null, fullRect, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    val scaledSize = size * pack.scaleFactor
    val offset = (size - scaledSize) / 2f
    val dst = RectF(offset, offset, offset + scaledSize, offset + scaledSize)
    canvas.drawBitmap(raw, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))

    pack.iconMask?.let { maskName ->
      loadDrawableByName(pack.resources, pack.packageName, maskName)?.let { maskDrw ->
        val maskBmp = drawableToBitmap(maskDrw, size)
        val maskPaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
          }
        canvas.drawBitmap(maskBmp, null, fullRect, maskPaint)
      }
    }

    pack.iconUpon?.let { uponName ->
      loadDrawableByName(pack.resources, pack.packageName, uponName)?.let { uponDrw ->
        val uponBmp = drawableToBitmap(uponDrw, size)
        canvas.drawBitmap(uponBmp, null, fullRect, Paint(Paint.FILTER_BITMAP_FLAG))
      }
    }
    return result
  }

  private fun loadDrawableByName(res: Resources, pkg: String, name: String): Drawable? {
    return try {
      val id = res.getIdentifier(name, "drawable", pkg)
      if (id != 0) ResourcesCompat.getDrawable(res, id, null) else null
    } catch (_: Exception) {
      null
    }
  }

  fun drawableToBitmap(drawable: Drawable, size: Int = 192): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
      val bmp = drawable.bitmap
      if (bmp.width == size && bmp.height == size) return bmp
      return Bitmap.createScaledBitmap(bmp, size, size, true)
    }
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
  }

  fun extractDominantColor(bitmap: Bitmap): Color {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= 0 || h <= 0) return Color(0xFF4A5588)

    var rSum = 0L
    var gSum = 0L
    var bSum = 0L
    var count = 0L

    val stepX = (w / 8).coerceAtLeast(1)
    val stepY = (h / 8).coerceAtLeast(1)
    for (y in stepY until h step stepY) {
      for (x in stepX until w step stepX) {
        val pixel = bitmap.getPixel(x, y)
        val alpha = (pixel ushr 24) and 0xFF
        if (alpha < 180) continue
        val r = (pixel ushr 16) and 0xFF
        val g = (pixel ushr 8) and 0xFF
        val b = pixel and 0xFF
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val weight = if (max - min > 24) 3L else 1L
        rSum += r * weight
        gSum += g * weight
        bSum += b * weight
        count += weight
      }
    }

    if (count == 0L) return Color(0xFF4A5588)
    val avgR = ((rSum / count).toInt()).coerceIn(28, 235)
    val avgG = ((gSum / count).toInt()).coerceIn(28, 235)
    val avgB = ((bSum / count).toInt()).coerceIn(28, 235)
    return Color(red = avgR, green = avgG, blue = avgB)
  }
}
