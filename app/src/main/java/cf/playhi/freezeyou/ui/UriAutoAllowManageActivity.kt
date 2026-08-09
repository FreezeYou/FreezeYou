package cf.playhi.freezeyou.ui

import android.os.Bundle
import android.util.Base64
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
import cf.playhi.freezeyou.utils.MoreUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import net.grandcentrix.tray.AppPreferences

class UriAutoAllowManageActivity : FreezeYouBaseActivity() {
    private val preferences by lazy { AppPreferences(this) }
    private val ipaMode by lazy { intent.getBooleanExtra("isIpaMode", false) }
    private val preferenceKey: String
        get() = if (ipaMode) "installPkgs_autoAllowPkgs_allows" else "uriAutoAllowPkgs_allows"
    private var entries by mutableStateOf<List<PackageListEntry>>(emptyList())
    private var encodedPackages: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        if (ipaMode) setTitle(R.string.manageIpaAutoAllow)
        refreshEntries()
        setContent {
            FreezeYouTheme {
                PackageListScreen(entries, if (encodedPackages.isEmpty()) null else ::confirmDelete)
            }
        }
    }

    private fun refreshEntries() {
        encodedPackages = preferences.getString(preferenceKey, "").orEmpty()
            .split(',').filter(String::isNotEmpty)
        entries = if (encodedPackages.isEmpty()) {
            listOf(PackageListEntry(getString(R.string.notAvailable), getString(R.string.notAvailable)))
        } else {
            encodedPackages.map { encoded ->
                val packageName = String(Base64.decode(encoded, Base64.DEFAULT))
                PackageListEntry(
                    ApplicationLabelUtils.getApplicationLabel(this, null, null, packageName),
                    packageName
                )
            }
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
                val encoded = Base64.encodeToString(entry.packageName.toByteArray(), Base64.DEFAULT)
                preferences.put(
                    preferenceKey,
                    MoreUtils.listToString(encodedPackages.toMutableList().apply { remove(encoded) }, ",")
                )
                refreshEntries()
            }
            .create().show()
    }
}
