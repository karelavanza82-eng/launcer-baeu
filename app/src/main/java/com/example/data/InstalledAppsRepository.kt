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
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import com.example.model.AppItem
import com.example.model.BuiltInIconPack
import com.example.model.ExternalIconPackInfo
import java.io.File
import java.io.FileOutputStream
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
  private val iconPackManager = IconPackManager(context)

  fun observePackageChanges(): Flow<Unit> =
    callbackFlow {
        trySend(Unit)

        val launcherCallback =
          object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
              trySend(Unit)
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
              trySend(Unit)
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
              trySend(Unit)
            }

            override fun onPackagesAvailable(
              packageNames: Array<out String>,
              user: UserHandle,
              replacing: Boolean,
            ) {
              trySend(Unit)
            }

            override fun onPackagesUnavailable(
              packageNames: Array<out String>,
              user: UserHandle,
              replacing: Boolean,
            ) {
              trySend(Unit)
            }
          }

        val packageReceiver =
          object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
              trySend(Unit)
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

  suspend fun getExternalIconPacks(): List<ExternalIconPackInfo> =
    withContext(Dispatchers.IO) { iconPackManager.discoverExternalIconPacks() }

  suspend fun getInstalledApps(iconPackId: String = BuiltInIconPack.SYSTEM.id): List<AppItem> =
    withContext(Dispatchers.IO) { loadInstalledAppsSync(iconPackId) }

  private fun loadInstalledAppsSync(iconPackId: String): List<AppItem> {
    val ownPackage = context.packageName
    val appsByComponent = LinkedHashMap<String, AppItem>()
    val externalPack =
      if (iconPackId.startsWith("pkg:")) {
        iconPackManager.loadExternalPack(iconPackId.removePrefix("pkg:"))
      } else {
        null
      }

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
        val (bitmap, dominant) =
          iconPackManager.resolveStyledIcon(component, drawable, iconPackId, externalPack)
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
      val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
      val resolved =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
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
        val (bitmap, dominant) =
          iconPackManager.resolveStyledIcon(component, drawable, iconPackId, externalPack)
        val isSystem = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
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

  suspend fun saveCustomWallpaper(uri: Uri): String? =
    withContext(Dispatchers.IO) {
      try {
        val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
        val decoded = input.use { BitmapFactory.decodeStream(it) } ?: return@withContext null
        val maxDim = 2160f
        val scale = minOf(maxDim / decoded.width, maxDim / decoded.height, 1f)
        val finalBmp =
          if (scale < 1f) {
            Bitmap.createScaledBitmap(
              decoded,
              (decoded.width * scale).toInt().coerceAtLeast(1),
              (decoded.height * scale).toInt().coerceAtLeast(1),
              true,
            )
          } else {
            decoded
          }
        val outFile = File(context.filesDir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
        // Remove previous saved custom wallpaper files to keep storage clean
        context.filesDir
          .listFiles()
          ?.filter { it.name.startsWith("custom_wallpaper_") && it.name != outFile.name }
          ?.forEach { it.delete() }
        FileOutputStream(outFile).use { fos ->
          finalBmp.compress(Bitmap.CompressFormat.JPEG, 92, fos)
        }
        outFile.absolutePath
      } catch (_: Exception) {
        null
      }
    }

  fun launchApp(activityContext: Context, app: AppItem, sourceBounds: Rect?): Boolean {
    val optionsBundle =
      try {
        ActivityOptions.makeCustomAnimation(activityContext, android.R.anim.fade_in, 0).toBundle()
      } catch (_: Exception) {
        null
      }

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
      val homeIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
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
}
