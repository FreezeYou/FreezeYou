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
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cf.playhi.freezeyou.Freeze
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.ActionButton
import cf.playhi.freezeyou.ui.compose.DrawableImage
import cf.playhi.freezeyou.ui.compose.EqualButtons
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

class LauncherShortcutConfirmAndGenerateActivity : FreezeYouBaseActivity() {
    private var requestFromLauncher = false
    private var targetSelfClass: Class<*>? = null
    private var finalDrawable by mutableStateOf<Drawable?>(null)
    private var selectedPackage by mutableStateOf("")
    private var displayName by mutableStateOf("")
    private var target by mutableStateOf("")
    private var task by mutableStateOf("")
    private var shortcutId by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        requestFromLauncher = Intent.ACTION_CREATE_SHORTCUT == intent.action
        targetSelfClass = if (requestFromLauncher) Freeze::class.java else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("class") as Class<*>?
        }
        initializeFromIntent()
        setContent { FreezeYouTheme { ShortcutEditor() } }
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
                target = data.getStringExtra("name").orEmpty()
                shortcutId = data.getStringExtra("id").orEmpty()
                displayName = data.getStringExtra("label").orEmpty()
                data.bitmapExtra("icon")?.let { finalDrawable = BitmapDrawable(resources, it) }
            }
            PACKAGE_REQUEST -> {
                intent = data
                initializeFromIntent()
            }
            ICON_REQUEST -> data.bitmapExtra("Icon")?.let {
                finalDrawable = BitmapDrawable(resources, it)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun ShortcutEditor() {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            PickerField(
                value = selectedPackage,
                label = stringResource(R.string.application),
                onPick = ::startSelectPackageActivityForResult
            )
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.icon), Modifier.padding(top = 12.dp))
            DrawableImage(
                finalDrawable,
                stringResource(R.string.icon),
                Modifier.size(140.dp).clickable { selectIcon() }
            )
            PickerField(
                value = target,
                label = stringResource(R.string.target),
                onPick = { startSelectTargetActivityForResult(selectedPackage) }
            )
            OutlinedTextField(
                value = task,
                onValueChange = { task = it },
                label = { Text(stringResource(R.string.task)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = shortcutId,
                onValueChange = { shortcutId = it },
                label = { Text(stringResource(R.string.id)) },
                modifier = Modifier.fillMaxWidth()
            )
            EqualButtons {
                ActionButton(stringResource(R.string.cancel)) { finish() }
                ActionButton(stringResource(R.string.generate)) { generateShortcut() }
                ActionButton(stringResource(R.string.simulate)) { simulateShortcut() }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun PickerField(
        value: String,
        label: String,
        onPick: () -> Unit
    ) {
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onPick) { Text("…") }
        }
    }

    private fun initializeFromIntent() {
        displayName = intent.getStringExtra("name") ?: getString(R.string.name)
        shortcutId = intent.getStringExtra("id") ?: Date().time.toString()
        selectedPackage = intent.getStringExtra("pkgName") ?: getString(R.string.plsSelect)
        target = getString(R.string.launch)
        task = ""
        finalDrawable = when {
            selectedPackage in setOf("cf.playhi.freezeyou.extra.fuf", "OF", "UF", "OO", "OOU", "FOQ", "OS", "OU", "UFU") ->
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round)
            selectedPackage == "cf.playhi.freezeyou.extra.oklock" ->
                ContextCompat.getDrawable(this, R.drawable.screenlock)
            selectedPackage.startsWith("CATEGORY") || selectedPackage.startsWith("FORCESTOPCATEGORY") ->
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round)
            selectedPackage == getString(R.string.plsSelect) ->
                ContextCompat.getDrawable(this, R.drawable.grid_add)
            else -> getApplicationIcon(
                this,
                selectedPackage,
                getApplicationInfoFromPkgName(selectedPackage, this),
                false
            )
        }
    }

    private fun selectIcon() {
        @Suppress("DEPRECATION")
        startActivityForResult(Intent(this, SelectShortcutIconActivity::class.java), ICON_REQUEST)
    }

    private fun generateShortcut() {
        val normalizedTarget = target.takeUnless { it == getString(R.string.launch) }.orEmpty()
        val icon = finalDrawable ?: return
        if (requestFromLauncher) {
            val shortcutIntent = Intent(this, Freeze::class.java)
                .putExtra("pkgName", selectedPackage)
                .putExtra("target", normalizedTarget)
                .putExtra("tasks", task)
            val result = Intent()
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, displayName)
                .putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(icon))
            setResult(RESULT_OK, result)
            finish()
        } else {
            val targetClass = targetSelfClass ?: return
            createShortCut(
                displayName, selectedPackage, icon, targetClass, shortcutId,
                applicationContext, normalizedTarget, task
            )
        }
    }

    private fun simulateShortcut() {
        val normalizedTarget = target.takeUnless { it == getString(R.string.launch) }.orEmpty()
        startActivity(
            Intent(this, Freeze::class.java)
                .putExtra("pkgName", selectedPackage)
                .putExtra("target", normalizedTarget)
                .putExtra("tasks", task)
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
        const val TARGET_REQUEST = 8
        const val PACKAGE_REQUEST = 11
        const val ICON_REQUEST = 21
    }
}
