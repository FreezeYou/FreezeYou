package cf.playhi.freezeyou.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.ui.compose.PackageListEntry
import cf.playhi.freezeyou.ui.compose.PackageListScreen
import cf.playhi.freezeyou.utils.AlertDialogUtils
import cf.playhi.freezeyou.utils.ApplicationLabelUtils
import cf.playhi.freezeyou.utils.NotificationUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import net.grandcentrix.tray.AppPreferences

class FUFNotificationsManageActivity : FreezeYouBaseActivity() {
    private val preferences by lazy { AppPreferences(this) }
    private var entries by mutableStateOf<List<PackageListEntry>>(emptyList())
    private var hasEntries by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        refreshEntries()
        setContent {
            FreezeYouTheme {
                PackageListScreen(entries, if (hasEntries) ::confirmDelete else null)
            }
        }
    }

    private fun refreshEntries() {
        val stored = preferences.getString("notifying", "").orEmpty()
        val packages = stored.split(',').filter(String::isNotEmpty)
        hasEntries = packages.isNotEmpty()
        entries = if (hasEntries) {
            packages.map { packageName ->
                PackageListEntry(
                    ApplicationLabelUtils.getApplicationLabel(this, null, null, packageName),
                    packageName
                )
            }
        } else {
            listOf(PackageListEntry(getString(R.string.notAvailable), getString(R.string.notAvailable)))
        }
    }

    private fun confirmDelete(entry: PackageListEntry) {
        AlertDialogUtils.buildAlertDialog(
            this,
            null,
            "${entry.label}${System.lineSeparator()}${entry.packageName}",
            getString(R.string.askIfDel)
        ).setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                val stored = preferences.getString("notifying", "").orEmpty()
                preferences.put("notifying", stored.replace("${entry.packageName},", ""))
                NotificationUtils.deleteNotification(this, entry.packageName)
                refreshEntries()
            }
            .create().show()
    }
}
