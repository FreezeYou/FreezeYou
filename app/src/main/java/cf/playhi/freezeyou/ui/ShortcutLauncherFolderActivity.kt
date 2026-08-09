package cf.playhi.freezeyou.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.DrawableImage
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getGrayBitmap
import cf.playhi.freezeyou.utils.ApplicationInfoUtils
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.FUFUtils
import cf.playhi.freezeyou.utils.OneKeyListUtils
import cf.playhi.freezeyou.utils.Support
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import java.util.Date

open class ShortcutLauncherFolderActivity : FreezeYouBaseActivity(), OnSharedPreferenceChangeListener {
    private data class FolderItem(val icon: Drawable?, val label: String, val packageName: String)

    private var folderName by mutableStateOf("")
    private var folderItems by mutableStateOf<List<FolderItem>>(emptyList())
    private var renameDialogVisible by mutableStateOf(false)
    private var createIcon by mutableStateOf<Drawable?>(null)
    private var createFolderName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this, Intent.ACTION_CREATE_SHORTCUT == intent.action)
        if (Intent.ACTION_CREATE_SHORTCUT != intent.action) {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        super.onCreate(savedInstanceState)
        registerFolderListener(intent.getStringExtra("UUID"))
        if (Intent.ACTION_CREATE_SHORTCUT == intent.action) showCreateShortcut() else showFolder()
    }

    override fun onNewIntent(intent: Intent) {
        unregisterFolderListener(getIntent().getStringExtra("UUID"))
        super.onNewIntent(intent)
        setIntent(intent)
        registerFolderListener(intent.getStringExtra("UUID"))
        if (Intent.ACTION_CREATE_SHORTCUT == intent.action) showCreateShortcut() else showFolder()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ICON_REQUEST && resultCode == RESULT_OK) {
            data?.bitmapExtra("Icon")?.let { createIcon = BitmapDrawable(resources, it) }
        }
    }

    override fun onDestroy() {
        unregisterFolderListener(intent.getStringExtra("UUID"))
        super.onDestroy()
    }

    private fun showCreateShortcut() {
        createFolderName = getString(R.string.folder)
        createIcon = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_new_round)
        setContent {
            FreezeYouTheme {
                AlertDialog(
                    onDismissRequest = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                    title = { Text(stringResource(R.string.folder)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DrawableImage(
                                createIcon,
                                stringResource(R.string.icon),
                                Modifier.size(72.dp).clickable(onClick = ::selectCreateIcon)
                            )
                            OutlinedTextField(
                                value = createFolderName,
                                onValueChange = { createFolderName = it },
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = ::finishCreateShortcut) {
                            Text(stringResource(R.string.finish))
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            setResult(RESULT_CANCELED)
                            finish()
                        }) { Text(stringResource(R.string.cancel)) }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @androidx.compose.runtime.Composable
    private fun FolderScreen(uuid: String, preferences: SharedPreferences) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = folderName,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = { renameDialogVisible = true },
                    onLongClick = { renameDialogVisible = true }
                ).padding(10.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(72.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(folderItems, key = { it.packageName }) { item ->
                    var itemAnchor by remember { mutableStateOf<View?>(null) }
                    Box {
                        Column(
                            modifier = Modifier.combinedClickable(
                                onClick = { openFolderItem(item, uuid) },
                                onLongClick = {
                                    if (item.packageName != ADD_PACKAGE) {
                                        itemAnchor?.let { anchor ->
                                            Support.showChooseActionPopupMenu(
                                                this@ShortcutLauncherFolderActivity,
                                                this@ShortcutLauncherFolderActivity,
                                                anchor,
                                                item.packageName,
                                                item.label,
                                                true,
                                                preferences
                                            )
                                        }
                                    }
                                }
                            ).padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DrawableImage(item.icon, item.label, Modifier.size(52.dp))
                            Text(item.label, maxLines = 1, textAlign = TextAlign.Center)
                        }
                        AndroidView(
                            factory = { context -> View(context).also { itemAnchor = it } },
                            modifier = Modifier.align(Alignment.Center).size(1.dp)
                        )
                    }
                }
            }
        }
        if (renameDialogVisible) RenameDialog(preferences)
    }

    @androidx.compose.runtime.Composable
    private fun RenameDialog(preferences: SharedPreferences) {
        var proposedName by remember(folderName) { mutableStateOf(folderName) }
        AlertDialog(
            onDismissRequest = { renameDialogVisible = false },
            title = { Text(stringResource(R.string.name)) },
            text = {
                OutlinedTextField(proposedName, { proposedName = it }, Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    preferences.edit().putString("folderName", proposedName).apply()
                    renameDialogVisible = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                Button(onClick = { renameDialogVisible = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    private fun showFolder() {
        val uuid = intent.getStringExtra("UUID")
        if (uuid == null) {
            showToast(this, R.string.failed)
            finish()
            return
        }
        val preferences = getSharedPreferences(uuid, MODE_PRIVATE)
        refreshFolder(preferences)
        setContent { FreezeYouTheme { FolderScreen(uuid, preferences) } }
    }

    private fun refreshFolder(preferences: SharedPreferences) {
        folderName = preferences.getString("folderName", getString(R.string.folder)).orEmpty()
        folderItems = generateFolderItems(preferences)
    }

    private fun generateFolderItems(preferences: SharedPreferences): List<FolderItem> {
        val result = preferences.getString("pkgS", "").orEmpty().split(',')
            .filter(String::isNotEmpty)
            .map { packageName ->
                val applicationInfo = ApplicationInfoUtils.getApplicationInfoFromPkgName(packageName, this)
                val icon = getApplicationIcon(this, packageName, applicationInfo, false)
                FolderItem(
                    icon = if (FUFUtils.realGetFrozenStatus(this, packageName, null)) {
                        BitmapDrawable(resources, getGrayBitmap(getBitmapFromDrawable(icon)))
                    } else icon,
                    label = getApplicationLabel(this, null, null, packageName),
                    packageName = packageName
                )
            }.toMutableList()
        result += FolderItem(
            ContextCompat.getDrawable(this, R.drawable.grid_add),
            getString(R.string.add),
            ADD_PACKAGE
        )
        return result
    }

    private fun openFolderItem(item: FolderItem, uuid: String) {
        if (item.packageName == ADD_PACKAGE) {
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent(this, FUFLauncherShortcutCreator::class.java).putExtra("slf_n", uuid),
                ADD_APP_REQUEST
            )
        } else {
            FUFUtils.checkFrozenStatusAndStartApp(this, item.packageName, null, null)
        }
    }

    private fun selectCreateIcon() {
        @Suppress("DEPRECATION")
        startActivityForResult(Intent(this, SelectShortcutIconActivity::class.java), ICON_REQUEST)
    }

    private fun finishCreateShortcut() {
        val uuid = "Folder_${createFolderName.hashCode()}_${Date().time}"
        val result = Intent()
            .putExtra(
                Intent.EXTRA_SHORTCUT_INTENT,
                Intent(this, ShortcutLauncherFolderActivity::class.java).putExtra("UUID", uuid)
            )
            .putExtra(Intent.EXTRA_SHORTCUT_NAME, createFolderName)
            .putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(createIcon))
        OneKeyListUtils.addToOneKeyList(this, "FolderUUIDs", uuid)
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == "pkgS" || key == "folderName") refreshFolder(sharedPreferences)
    }

    private fun registerFolderListener(uuid: String?) {
        if (uuid != null) getSharedPreferences(uuid, MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(this)
    }

    private fun unregisterFolderListener(uuid: String?) {
        if (uuid != null) getSharedPreferences(uuid, MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun Intent.bitmapExtra(name: String): Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(name)
        }

    private companion object {
        const val ICON_REQUEST = 6
        const val ADD_APP_REQUEST = 7001
        const val ADD_PACKAGE = "freezeyou@add"
    }
}
