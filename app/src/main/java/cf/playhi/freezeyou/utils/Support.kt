package cf.playhi.freezeyou.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import cf.playhi.freezeyou.ForceStop
import cf.playhi.freezeyou.Freeze
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilder
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.freezeOnceQuit
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys
import cf.playhi.freezeyou.ui.InstallPackagesActivity
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ClipboardUtils.copyToClipboard
import cf.playhi.freezeyou.utils.LauncherShortcutUtils.checkSettingsAndRequestCreateShortcut
import cf.playhi.freezeyou.utils.OneKeyListUtils.addToOneKeyList
import cf.playhi.freezeyou.utils.OneKeyListUtils.existsInOneKeyList
import cf.playhi.freezeyou.utils.OneKeyListUtils.removeFromOneKeyList
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import net.grandcentrix.tray.AppPreferences

object Support {
    @JvmStatic
    fun checkAddOrRemove(context: Context, pkgNames: String?, pkgName: String, oneKeyName: String) {
        if (existsInOneKeyList(pkgNames, pkgName)) {
            showToast(
                context,
                if (removeFromOneKeyList(
                        context,
                        oneKeyName,
                        pkgName
                    )
                ) R.string.removed else R.string.removeFailed
            )
        } else {
            showToast(
                context,
                if (addToOneKeyList(
                        context,
                        oneKeyName,
                        pkgName
                    )
                ) R.string.added else R.string.addFailed
            )
            if (context.getString(R.string.sFreezeOnceQuit) == oneKeyName) {
                if (!freezeOnceQuit.getValue(null)) {
                    freezeOnceQuit.setValue(null, true)
                }
                AccessibilityUtils.checkAndRequestIfAccessibilitySettingsOff(context)
            }
        }
    }

    @JvmStatic
    fun showChooseActionPopupMenu(
        context: Context,
        activity: Activity,
        view: View,
        pkgName: String,
        name: String,
        onSelect: (() -> Unit)? = null
    ) {
        generateChooseActionPopupMenu(context, activity, view, pkgName, name, false, null, onSelect).show()
    }

    @JvmStatic
    fun showChooseActionPopupMenu(
        context: Context,
        activity: Activity,
        view: View,
        pkgName: String,
        name: String,
        canRemoveItem: Boolean,
        folderPkgListSp: SharedPreferences?
    ) {
        generateChooseActionPopupMenu(
            context,
            activity,
            view,
            pkgName,
            name,
            canRemoveItem,
            folderPkgListSp
        ).show()
    }

    private fun generateChooseActionPopupMenu(
        context: Context,
        activity: Activity,
        view: View,
        pkgName: String,
        name: String,
        canRemoveItem: Boolean,
        folderPkgListSp: SharedPreferences?,
        onSelect: (() -> Unit)? = null
    ): PopupMenu {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.main_single_choose_action_menu)
        popup.menu.findItem(R.id.main_sca_menu_select).isVisible = onSelect != null
        val vmUserDefinedSubMenu = popup.menu.findItem(R.id.main_sca_userDefined).subMenu
        vmUserDefinedSubMenu!!.clear()
        vmUserDefinedSubMenu.add(
            R.id.main_sca_menu_userDefined_menuGroup,
            R.id.main_sca_menu_userDefined_newClassification,
            0,
            R.string.newClassification
        ) // 加入“新建分类”
        val userDefinedCategoriesHashMap = HashMap<Int, String?>()
        val vmUserDefinedDb =
            context.openOrCreateDatabase("userDefinedCategories", Context.MODE_PRIVATE, null)
        vmUserDefinedDb.execSQL(
            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        )
        val cursor = vmUserDefinedDb.query(
            "categories",
            arrayOf("label", "_id", "packages"),
            null,
            null,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            for (i in 0 until cursor.count) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("label"))
                userDefinedCategoriesHashMap[id] =
                    cursor.getString(cursor.getColumnIndexOrThrow("packages"))
                vmUserDefinedSubMenu.add(
                    R.id.main_sca_menu_userDefined_menuGroup,
                    id,
                    id,
                    String(Base64.decode(title, Base64.DEFAULT))
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        vmUserDefinedDb.close()
        val sharedPreferences = AppPreferences(context)
        val pkgNames =
            sharedPreferences.getString(context.getString(R.string.sAutoFreezeApplicationList), "")
        if (existsInOneKeyList(pkgNames, pkgName)) {
            popup.menu.findItem(R.id.main_sca_menu_addToOneKeyList).setTitle(R.string.removeFromOneKeyList)
        }
        val freezeOnceQuitPkgNames =
            sharedPreferences.getString(context.getString(R.string.sFreezeOnceQuit), "")
        if (existsInOneKeyList(freezeOnceQuitPkgNames, pkgName)) {
            popup.menu.findItem(R.id.main_sca_menu_addToFreezeOnceQuit)
                .setTitle(R.string.removeFromFreezeOnceQuit)
        }
        val ufPkgNames =
            sharedPreferences.getString(context.getString(R.string.sOneKeyUFApplicationList), "")
        if (existsInOneKeyList(ufPkgNames, pkgName)) {
            popup.menu.findItem(R.id.main_sca_menu_addToOneKeyUFList)
                .setTitle(R.string.removeFromOneKeyUFList)
        }
        if (FUFUtils.realGetFrozenStatus(context, pkgName, null)) {
            popup.menu.findItem(R.id.main_sca_menu_disableAEnable).setTitle(R.string.UfSlashRun)
        } else {
            popup.menu.findItem(R.id.main_sca_menu_disableAEnable).setTitle(R.string.freezeSlashRun)
        }
        if (!canRemoveItem) {
            popup.menu.removeItem(R.id.main_sca_menu_removeFromTheList)
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.groupId) {
                R.id.main_sca_menu_userDefined_menuGroup -> when (item.itemId) {
                    R.id.main_sca_menu_userDefined_newClassification -> {
                        val vmUserDefinedNameAlertDialogEditText = EditText(activity)
                        val vmUserDefinedNameAlertDialog = FreezeYouAlertDialogBuilder(activity)
                        vmUserDefinedNameAlertDialog.setTitle(R.string.label)
                        vmUserDefinedNameAlertDialog.setView(vmUserDefinedNameAlertDialogEditText)
                        vmUserDefinedNameAlertDialog.setPositiveButton(
                            R.string.save
                        ) { dialog, which ->
                            val label = Base64.encodeToString(
                                vmUserDefinedNameAlertDialogEditText.text.toString().toByteArray(),
                                Base64.DEFAULT
                            )
                            if ("" == label) {
                                showToast(activity, R.string.emptyNotAllowed)
                            } else {
                                var alreadyExists = false
                                val vmUserDefinedDb1 = context.openOrCreateDatabase(
                                    "userDefinedCategories",
                                    Context.MODE_PRIVATE,
                                    null
                                )
                                vmUserDefinedDb1.execSQL(
                                    "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                                )
                                val cursor1 = vmUserDefinedDb1.query(
                                    "categories",
                                    arrayOf("label"),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                                if (cursor1.moveToFirst()) {
                                    for (i in 0 until cursor1.count) {
                                        if (label == cursor1.getString(
                                                cursor1.getColumnIndexOrThrow(
                                                    "label"
                                                )
                                            )
                                        ) {
                                            alreadyExists = true
                                            break
                                        }
                                        cursor1.moveToNext()
                                    }
                                }
                                cursor1.close()
                                if (alreadyExists) {
                                    showToast(activity, R.string.alreadyExist)
                                } else {
                                    vmUserDefinedDb1.execSQL(
                                        "replace into categories(_id,label,packages) VALUES ( "
                                                + null + ",'"
                                                + label + "','')"
                                    )
                                }
                                vmUserDefinedDb1.close()
                            }
                        }
                        vmUserDefinedNameAlertDialog.setNegativeButton(R.string.cancel, null)
                        vmUserDefinedNameAlertDialog.show()
                    }
                    else -> {
                        val itemId = item.itemId
                        if (userDefinedCategoriesHashMap.containsKey(itemId)) {
                            val vmUserDefinedDb1 = context.openOrCreateDatabase(
                                "userDefinedCategories",
                                Context.MODE_PRIVATE,
                                null
                            )
                            vmUserDefinedDb1.execSQL(
                                "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                            )
                            var pkgs = userDefinedCategoriesHashMap[itemId]
                            if (pkgs == null) {
                                pkgs = ""
                            }
                            val existed = existsInOneKeyList(pkgs, pkgName)
                            pkgs = if (existed) {
                                pkgs.replace("$pkgName,", "")
                            } else {
                                pkgs + pkgName + ","
                            }
                            vmUserDefinedDb1.execSQL(
                                "UPDATE categories SET packages = '"
                                        + pkgs
                                        + "' WHERE _id = "
                                        + itemId
                                        + ";"
                            )
                            vmUserDefinedDb1.close()
                            showToast(activity, if (existed) R.string.removed else R.string.added)
                        }
                    }
                }
            }
            when (item.itemId) {
                R.id.main_sca_menu_select -> onSelect?.invoke()
                R.id.main_sca_menu_forceStop -> if (context.getString(R.string.notAvailable) != name) {
                    context.startActivity(
                        Intent(context, ForceStop::class.java).putExtra(
                            "pkgName",
                            pkgName
                        )
                    )
                }
                R.id.main_sca_menu_addToFreezeOnceQuit -> checkAddOrRemove(
                    context,
                    freezeOnceQuitPkgNames,
                    pkgName,
                    context.getString(R.string.sFreezeOnceQuit)
                )
                R.id.main_sca_menu_addToOneKeyList -> checkAddOrRemove(
                    context,
                    pkgNames,
                    pkgName,
                    context.getString(R.string.sAutoFreezeApplicationList)
                )
                R.id.main_sca_menu_addToOneKeyUFList -> checkAddOrRemove(
                    context,
                    ufPkgNames,
                    pkgName,
                    context.getString(R.string.sOneKeyUFApplicationList)
                )
                R.id.main_sca_menu_appDetail -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", pkgName, null)
                    intent.data = uri
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast(context, e.localizedMessage)
                    }
                }
                R.id.main_sca_menu_copyPkgName -> showToast(
                    context,
                    if (copyToClipboard(context, pkgName)) R.string.success else R.string.failed
                )
                R.id.main_sca_menu_disableAEnable -> if (context.getString(R.string.notAvailable) != name) {
                    context.startActivity(
                        Intent(context, Freeze::class.java).putExtra("pkgName", pkgName).putExtra(
                            "fromShortcut",
                            false
                        )
                    )
                }
                R.id.main_sca_menu_createDisEnableShortCut -> checkSettingsAndRequestCreateShortcut(
                    name,
                    pkgName,
                    getApplicationIcon(
                        context,
                        pkgName,
                        ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context),
                        false
                    ),
                    Freeze::class.java,
                    "FreezeYou! $pkgName",
                    context
                )
                R.id.main_sca_menu_removeFromTheList -> if (folderPkgListSp != null) {
                    val folderPkgs = folderPkgListSp.getString("pkgS", "")
                    if (existsInOneKeyList(folderPkgs, pkgName)) {
                        folderPkgListSp.edit()
                            .putString("pkgS", folderPkgs!!.replace("$pkgName,", ""))
                            .apply()
                    }
                }
                R.id.main_sca_menu_gotoStore -> {
                    val gotoStoreIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details/?id=$pkgName")
                    )
                    val title = activity.getString(R.string.plsSelect)
                    val chooser = Intent.createChooser(gotoStoreIntent, title)
                    if (gotoStoreIntent.resolveActivity(activity.packageManager) != null) {
                        activity.startActivity(chooser)
                    }
                }
                R.id.main_sca_menu_uninstall -> if (context.getString(R.string.notAvailable) != name &&
                    context.packageManager
                        .getComponentEnabledSetting(
                            ComponentName(
                                "cf.playhi.freezeyou",
                                "cf.playhi.freezeyou.InstallPackagesActivity"
                            )
                        ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                ) {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_DELETE,
                            Uri.parse("package:$pkgName"),
                            activity,
                            InstallPackagesActivity::class.java
                        )
                    )
                } else {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_DELETE,
                            Uri.parse("package:$pkgName")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                else -> {}
            }
            true
        }
        return popup
    }

    @JvmStatic
    fun getLocalString(context: Context): String {
        return DefaultMultiProcessMMKVStorageStringKeys.languagePref.getValue(context)
            ?: DefaultMultiProcessMMKVStorageStringKeys.languagePref.defaultValue() ?: "Default"
    }
}
