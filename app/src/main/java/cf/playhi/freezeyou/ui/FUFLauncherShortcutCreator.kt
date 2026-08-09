package cf.playhi.freezeyou.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.AppListItem
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus
import cf.playhi.freezeyou.utils.OneKeyListUtils.existsInOneKeyList
import cf.playhi.freezeyou.utils.ThemeUtils
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import java.util.Locale

class FUFLauncherShortcutCreator : FreezeYouBaseActivity() {
    private data class AppEntry(
        val icon: Drawable?, val name: String, val packageName: String, val statusIcon: Int
    )

    private var customThemeDisabledDot = R.drawable.shapedotblue
    private var customThemeEnabledDot = R.drawable.shapedotblack
    private var applications by mutableStateOf<List<AppEntry>>(emptyList())
    private var loading by mutableStateOf(true)
    private var search by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.processSetTheme(this)
        super.onCreate(savedInstanceState)
        ThemeUtils.processActionBar(supportActionBar)
        val folderName = intent.getStringExtra("slf_n")
        val returnPackageName = intent.getBooleanExtra("returnPkgName", false)
        if (folderName == null && !returnPackageName) {
            finish()
            return
        }
        setTitle(if (folderName != null) R.string.add else R.string.plsSelect)
        try {
            customThemeDisabledDot = ThemeUtils.getThemeDot(this)
            customThemeEnabledDot = ThemeUtils.getThemeSecondDot(this)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        setContent {
            FreezeYouTheme {
                val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]
                    ?: Locale.ROOT
                val query = search.lowercase(locale)
                val displayed = if (query.isEmpty()) applications else applications.filter {
                    it.name.lowercase(locale).contains(query) ||
                        it.packageName.lowercase(locale).contains(query)
                }
                Column(Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(displayed, key = { it.packageName }) { app ->
                            AppListItem(
                                icon = app.icon,
                                title = app.name,
                                subtitle = app.packageName,
                                statusIcon = app.statusIcon
                            ) { selectApp(app, folderName) }
                        }
                    }
                }
            }
        }
        loadApplications()
    }

    private fun loadApplications() {
        Thread {
            val packageManager = applicationContext.packageManager
            val matchUninstalled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PackageManager.MATCH_UNINSTALLED_PACKAGES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_UNINSTALLED_PACKAGES
            }
            val result = packageManager
                .getInstalledApplications(matchUninstalled)
                .mapNotNull { processAppStatus(it, packageManager) }
                .sortedBy { it.packageName }
            runOnUiThread {
                applications = result
                loading = false
            }
        }.start()
    }

    private fun selectApp(app: AppEntry, folderName: String?) {
        if (folderName != null) {
            val preferences = getSharedPreferences(folderName, MODE_PRIVATE)
            if (!existsInOneKeyList(preferences.getString("pkgS", ""), app.packageName)) {
                preferences.edit()
                    .putString("pkgS", preferences.getString("pkgS", "") + app.packageName + ",")
                    .apply()
                showToast(this, R.string.added)
            } else {
                showToast(this, R.string.alreadyExist)
            }
            setResult(RESULT_OK)
        } else {
            setResult(
                RESULT_OK,
                Intent()
                    .putExtra("pkgName", app.packageName)
                    .putExtra("name", app.name)
                    .putExtra("id", "FreezeYou! ${app.packageName}")
            )
            finish()
        }
    }

    private fun processAppStatus(
        applicationInfo: ApplicationInfo,
        packageManager: PackageManager
    ): AppEntry? {
        val packageName = applicationInfo.packageName
        if (packageName == "android" || packageName == "cf.playhi.freezeyou") return null
        return AppEntry(
            icon = getApplicationIcon(this, packageName, applicationInfo, true),
            name = getApplicationLabel(this, packageManager, applicationInfo, packageName),
            packageName = packageName,
            statusIcon = if (realGetFrozenStatus(this, packageName, packageManager)) {
                customThemeDisabledDot
            } else {
                customThemeEnabledDot
            }
        )
    }
}
