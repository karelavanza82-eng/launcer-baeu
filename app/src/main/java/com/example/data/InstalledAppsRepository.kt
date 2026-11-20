package com.example.data

import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.example.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

  private val packageManager: PackageManager = context.packageManager
  private val launcherApps: LauncherApps? =
    context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps

  fun observeInstalledApps(): Flow<List<AppItem>> =
    callbackFlow {
        val sendLatest = {
          trySend(loadInstalledAppsSync())
          Unit
        }

        // Emit initial snapshot immediately
        sendLatest()

        val launcherCallback =
          object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
              sendLatest()
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
              sendLatest()
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
              sendLatest()
            }

            override fun onPackagesAvailable(
              packageNames: Array<out String>,
              user: UserHandle,
              replacing: Boolean,
            ) {
              sendLatest()
            }

            override fun onPackagesUnavailable(
              packageNames: Array<out String>,
              user: UserHandle,
              replacing: Boolean,
            ) {
              sendLatest()
            }
          }

        val packageReceiver =
          object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
              sendLatest()
            }
          }

        try {
          launcherApps?.registerCallback(launcherCallback)
        } catch (_: Exception) {}

        val filter =
          IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
          }
        try {
          ContextCompat.registerReceiver(
            context,
            packageReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
          )
        } catch (_: Exception) {}

        awaitClose {
          try {
            launcherApps?.unregisterCallback(launcherCallback)
          } catch (_: Exception) {}
          try {
            context.unregisterReceiver(packageReceiver)
          } catch (_: Exception) {}
        }
      }
      .conflate()

  suspend fun getInstalledApps(): List<AppItem> =
    withContext(Dispatchers.IO) { loadInstalledAppsSync() }

  private fun loadInstalledAppsSync(): List<AppItem> {
    val ownPackage = context.packageName
    val appsByComponent = LinkedHashMap<String, AppItem>()

    val launcherActivities =
      try {
        launcherApps?.getActivityList(null, Process.myUserHandle()).orEmpty()
      } catch (_: Exception) {
        emptyList()
      }

    if (launcherActivities.isNotEmpty()) {
      for (info in launcherActivities) {
        val component = info.componentName
        if (component.packageName == ownPackage) continue
        val key = "${component.packageName}/${component.className}"
        val drawable =
          try {
            info.getBadgedIcon(0)
          } catch (_: Exception) {
            try {
              info.getIcon(0)
            } catch (_: Exception) {
              null
            }
          }
        val bitmap = drawable?.let { drawableToBitmap(it) }
        val dominant = bitmap?.let { extractDominantColor(it) } ?: Color(0xFF4A5588)
        val isSystem = (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        appsByComponent[key] =
          AppItem(
            componentName = component,
            packageName = component.packageName,
            className = component.className,
            label = info.label?.toString()?.trim().orEmpty().ifEmpty { component.packageName },
            icon = bitmap?.asImageBitmap(),
            dominantColor = dominant,
            isSystemApp = isSystem,
          )
      }
    } else {
      val intent =
        Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
      val resolved =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(0L),
          )
        } else {
          @Suppress("DEPRECATION")
          packageManager.queryIntentActivities(intent, 0)
        }
      for (resolveInfo in resolved) {
        val activityInfo = resolveInfo.activityInfo ?: continue
        val pkg = activityInfo.packageName ?: continue
        if (pkg == ownPackage) continue
        val cls = activityInfo.name ?: continue
        val component = ComponentName(pkg, cls)
        val key = "$pkg/$cls"
        val drawable =
          try {
            resolveInfo.loadIcon(packageManager)
          } catch (_: Exception) {
            null
          }
        val bitmap = drawable?.let { drawableToBitmap(it) }
        val dominant = bitmap?.let { extractDominantColor(it) } ?: Color(0xFF4A5588)
        val isSystem =
          (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val label =
          try {
            resolveInfo.loadLabel(packageManager).toString().trim()
          } catch (_: Exception) {
            pkg
          }
        appsByComponent[key] =
          AppItem(
            componentName = component,
            packageName = pkg,
            className = cls,
            label = label.ifEmpty { pkg },
            icon = bitmap?.asImageBitmap(),
            dominantColor = dominant,
            isSystemApp = isSystem,
          )
      }
    }

    return appsByComponent.values.sortedBy { it.label.lowercase() }
  }

  fun launchApp(activityContext: Context, app: AppItem, sourceBounds: Rect?): Boolean {
    val optionsBundle =
      try {
        if (sourceBounds != null && !sourceBounds.isEmpty) {
          ActivityOptions.makeCustomAnimation(activityContext, android.R.anim.fade_in, 0)
            .toBundle()
        } else {
          ActivityOptions.makeCustomAnimation(activityContext, android.R.anim.fade_in, 0)
            .toBundle()
        }
      } catch (_: Exception) {
        null
      }

    // Prefer LauncherApps.startMainActivity for exact component launch
    try {
      val la = launcherApps
      if (la != null) {
        val boundsRect =
          sourceBounds?.let {
            android.graphics.Rect(
              it.left.toInt(),
              it.top.toInt(),
              it.right.toInt(),
              it.bottom.toInt(),
            )
          }
        la.startMainActivity(app.componentName, Process.myUserHandle(), boundsRect, optionsBundle)
        return true
      }
    } catch (_: Exception) {}

    // Fallback to explicit component intent
    return try {
      val launchIntent =
        Intent(Intent.ACTION_MAIN).apply {
          addCategory(Intent.CATEGORY_LAUNCHER)
          component = app.componentName
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
      activityContext.startActivity(launchIntent, optionsBundle)
      true
    } catch (_: Exception) {
      try {
        val pkgIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (pkgIntent != null) {
          pkgIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
          )
          activityContext.startActivity(pkgIntent, optionsBundle)
          true
        } else {
          false
        }
      } catch (_: Exception) {
        false
      }
    }
  }

  fun openAppInfo(activityContext: Context, packageName: String) {
    try {
      val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts("package", packageName, null)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      activityContext.startActivity(intent)
    } catch (_: Exception) {}
  }

  fun requestUninstall(activityContext: Context, packageName: String) {
    try {
      val intent =
        Intent(Intent.ACTION_DELETE).apply {
          data = Uri.fromParts("package", packageName, null)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      activityContext.startActivity(intent)
    } catch (_: Exception) {}
  }

  fun isDefaultLauncher(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val roleManager = context.getSystemService(RoleManager::class.java)
      roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
    } else {
      val homeIntent =
        Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
      val resolved = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
      resolved?.activityInfo?.packageName == context.packageName
    }
  }

  fun createDefaultHomeIntent(): Intent {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val roleManager = context.getSystemService(RoleManager::class.java)
      if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
        return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
      }
    }
    return Intent(Settings.ACTION_HOME_SETTINGS)
  }

  private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
      return drawable.bitmap
    }
    val size = 192
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
  }

  private fun extractDominantColor(bitmap: Bitmap): Color {
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
        // Avoid near-pure white or near-pure black for richer tint cards
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
