package cf.playhi.freezeyou.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cf.playhi.freezeyou.ForceStop
import cf.playhi.freezeyou.Main
import cf.playhi.freezeyou.OneKeyFreeze
import cf.playhi.freezeyou.OneKeyUF
import cf.playhi.freezeyou.Freeze
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.DrawableImage
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable
import cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName
import cf.playhi.freezeyou.utils.LauncherShortcutUtils.createShortCut
import cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite
import cf.playhi.freezeyou.utils.ThemeUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import java.io.File
import java.util.Date

class ShortcutDraft : ViewModel() {
    var finalDrawable by mutableStateOf<Drawable?>(null)
    var selectedPackage by mutableStateOf("")
    var displayName by mutableStateOf("")
    var target by mutableStateOf("")
    var task by mutableStateOf("")
    var shortcutId by mutableStateOf("")
}

class LauncherShortcutConfirmAndGenerateActivity : FreezeYouBaseActivity() {
    private var requestFromLauncher = false
    private var targetSelfClass: Class<*>? = null
    private val draft: ShortcutDraft by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        requestFromLauncher = Intent.ACTION_CREATE_SHORTCUT == intent.action
        targetSelfClass = if (requestFromLauncher) Freeze::class.java else {
            @Suppress("DEPRECATION")
            (intent.getSerializableExtra("class") as? Class<*>)?.takeIf { it in shortcutActivities }
        }
        setTitle(R.string.create_shortcut)
        if (draft.shortcutId.isEmpty() && draft.finalDrawable == null) initializeFromIntent(intent)
        setContent { FreezeYouTheme(readableControls = true) { ShortcutEditor() } }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.lscaga_menu, menu)
        val theme = ThemeUtils.getUiTheme(this)
        if (theme == "white" || theme == "default") {
            menu.findItem(R.id.lscaga_menu_help).setIcon(R.drawable.ic_action_help_outline_light)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.lscaga_menu_help -> {
            requestOpenWebSite(
                this,
                "https://www.zidon.net/${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/guide/schedules.html"
            )
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null || resultCode != RESULT_OK) return
        when (requestCode) {
            TARGET_REQUEST -> {
                draft.target = data.getStringExtra("name").orEmpty()
                draft.shortcutId = data.getStringExtra("id").orEmpty()
                draft.displayName = data.getStringExtra("label").orEmpty()
                data.bitmapExtra("icon")?.let { draft.finalDrawable = BitmapDrawable(resources, it) }
            }
            PACKAGE_REQUEST -> initializeFromIntent(data)
            ICON_REQUEST -> data.bitmapExtra("Icon")?.let {
                draft.finalDrawable = BitmapDrawable(resources, it)
            }
        }
    }

    @Composable
    private fun ShortcutEditor() {
        var advanced by rememberSaveable { mutableStateOf(false) }
        var confirmation by rememberSaveable { mutableStateOf<String?>(null) }
        val canRun = draft.selectedPackage.isNotEmpty() && targetSelfClass != null
        val canCreate = canRun && draft.displayName.isNotBlank() && draft.finalDrawable != null &&
                (requestFromLauncher || draft.shortcutId.isNotBlank())
        Column(Modifier.fillMaxSize().imePadding()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DrawableImage(draft.finalDrawable, stringResource(R.string.change_icon),
                        Modifier.size(52.dp).clickable { selectIcon() })
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(draft.displayName, style = MaterialTheme.typography.titleMedium)
                        if (draft.selectedPackage.isNotEmpty()) {
                            Text(draft.selectedPackage, style = MaterialTheme.typography.bodySmall)
                            if (targetSelfClass == Freeze::class.java) Text(draft.target, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                OutlinedTextField(
                    value = draft.displayName, onValueChange = { draft.displayName = it },
                    singleLine = true, label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (targetSelfClass == Freeze::class.java) TextButton(onClick = ::startSelectPackageActivityForResult) {
                    Text(stringResource(R.string.choose_application))
                }
                TextButton(onClick = { advanced = !advanced }) {
                    Text(stringResource(if (advanced) R.string.hide_advanced else R.string.advanced_options))
                }
                if (advanced) {
                    TextButton(enabled = canRun && targetSelfClass == Freeze::class.java,
                        onClick = { startSelectTargetActivityForResult(draft.selectedPackage) }) {
                        Text(stringResource(R.string.shortcut_target, draft.target))
                    }
                    OutlinedTextField(
                        value = draft.task, onValueChange = { draft.task = it },
                        label = { Text(stringResource(R.string.task)) }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draft.shortcutId, onValueChange = { draft.shortcutId = it }, singleLine = true,
                        label = { Text(stringResource(R.string.id)) }, modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(enabled = canRun, onClick = { confirmation = runDescription() }) {
                    Text(stringResource(R.string.run_now))
                }
            }
            Button(enabled = canCreate, onClick = ::generateShortcut,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(stringResource(R.string.create_shortcut))
            }
        }
        confirmation?.let { message ->
            AlertDialog(
                onDismissRequest = { confirmation = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(stringResource(R.string.run_now)) },
                text = { Text(message, Modifier.verticalScroll(rememberScrollState())) },
                confirmButton = {
                    Button(onClick = { confirmation = null; runShortcut() }) { Text(stringResource(R.string.run_now)) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmation = null }) { Text(stringResource(android.R.string.cancel)) }
                }
            )
        }
    }

    private fun runDescription(): String {
        val effect = when (targetSelfClass) {
            OneKeyFreeze::class.java -> R.string.oneKeyFreeze
            OneKeyUF::class.java -> R.string.oneKeyUF
            Main::class.java -> R.string.quickView
            ForceStop::class.java -> R.string.forceStop
            Freeze::class.java -> {
                val frozen = realGetFrozenStatus(this, draft.selectedPackage, null)
                when {
                    shortcutAutoFUF.getValue() && frozen -> if (openImmediatelyAfterUnfreezeUseShortcutAutoFUF.getValue() &&
                        draft.target != getString(R.string.onlyUnfreeze)) R.string.UFAndRun else R.string.UFImmediately
                    shortcutAutoFUF.getValue() && !needConfirmWhenFreezeUseShortcutAutoFUF.getValue() -> R.string.freezeImmediately
                    !shortcutAutoFUF.getValue() && openAndUFImmediately.getValue() -> when {
                        draft.target == getString(R.string.onlyUnfreeze) -> R.string.onlyUnfreeze
                        frozen -> R.string.UFAndRun
                        else -> R.string.launch
                    }
                    else -> R.string.chooseAction
                }
            }
            else -> R.string.oneKeyLockScreen
        }
        return buildString {
            append(getString(effect)).append("\n").append(draft.selectedPackage)
            if (targetSelfClass == Freeze::class.java) {
                append("\n").append(getString(R.string.shortcut_target, draft.target))
                if (draft.task.isNotBlank()) append("\n").append(getString(R.string.task)).append(" · ").append(draft.task)
            }
        }
    }

    private fun initializeFromIntent(source: Intent) {
        draft.displayName = source.getStringExtra("name").orEmpty()
        draft.shortcutId = source.getStringExtra("id") ?: Date().time.toString()
        draft.selectedPackage = source.getStringExtra("pkgName").orEmpty()
        draft.target = getString(R.string.launch)
        draft.task = ""
        draft.finalDrawable = when {
            draft.selectedPackage in setOf("cf.playhi.freezeyou.extra.fuf", "OF", "UF", "OO", "OOU", "FOQ", "OS", "OU", "UFU") ->
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round)
            draft.selectedPackage == "cf.playhi.freezeyou.extra.oklock" ->
                ContextCompat.getDrawable(this, R.drawable.screenlock)
            draft.selectedPackage.startsWith("CATEGORY") || draft.selectedPackage.startsWith("FORCESTOPCATEGORY") ->
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round)
            draft.selectedPackage.isEmpty() ->
                ContextCompat.getDrawable(this, R.drawable.grid_add)
            else -> getApplicationIcon(
                this,
                draft.selectedPackage,
                getApplicationInfoFromPkgName(draft.selectedPackage, this),
                false
            )
        }
    }

    private fun selectIcon() {
        @Suppress("DEPRECATION")
        startActivityForResult(Intent(this, SelectShortcutIconActivity::class.java), ICON_REQUEST)
    }

    private fun generateShortcut() {
        val normalizedTarget = draft.target.takeUnless { it == getString(R.string.launch) }.orEmpty()
        val icon = draft.finalDrawable ?: return
        if (requestFromLauncher) {
            val shortcutIntent = Intent(this, Freeze::class.java)
                .putExtra("pkgName", draft.selectedPackage)
                .putExtra("target", normalizedTarget)
                .putExtra("tasks", draft.task)
            val result = Intent()
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, draft.displayName)
                .putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(icon))
            setResult(RESULT_OK, result)
            finish()
        } else {
            val targetClass = targetSelfClass ?: return
            createShortCut(
                draft.displayName, draft.selectedPackage, icon, targetClass, draft.shortcutId,
                applicationContext, normalizedTarget, draft.task
            )
        }
    }

    private fun runShortcut() {
        val targetClass = targetSelfClass ?: return
        val normalizedTarget = draft.target.takeUnless { it == getString(R.string.launch) }.orEmpty()
        startActivity(
            Intent(this, targetClass).setAction(Intent.ACTION_MAIN)
                .putExtra("pkgName", draft.selectedPackage)
                .putExtra("target", normalizedTarget)
                .putExtra("tasks", draft.task)
        )
    }

    private fun startSelectPackageActivityForResult() {
        @Suppress("DEPRECATION")
        startActivityForResult(
            Intent(this, FUFLauncherShortcutCreator::class.java).putExtra("returnPkgName", true),
            PACKAGE_REQUEST
        )
    }

    private fun startSelectTargetActivityForResult(packageName: String) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent(this, SelectTargetActivityActivity::class.java)
                    .putExtra("pkgName", packageName),
                TARGET_REQUEST
            )
        } catch (exception: PackageManager.NameNotFoundException) {
            exception.printStackTrace()
            showToast(this, R.string.packageNotFound)
        } catch (exception: Exception) {
            exception.printStackTrace()
            showToast(this, getString(R.string.failed) + File.separator + exception.localizedMessage)
        }
    }

    private fun Intent.bitmapExtra(name: String): Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(name)
        }

    private companion object {
        val shortcutActivities = setOf<Class<*>>(Freeze::class.java, ForceStop::class.java, Main::class.java,
            OneKeyFreeze::class.java, OneKeyUF::class.java, OneKeyScreenLockImmediatelyActivity::class.java,
            cf.playhi.freezeyou.OneKeyScreenLockImmediatelyActivity::class.java)

        const val TARGET_REQUEST = 8
        const val PACKAGE_REQUEST = 11
        const val ICON_REQUEST = 21
    }
}
