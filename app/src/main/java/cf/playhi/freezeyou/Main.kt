package cf.playhi.freezeyou

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.adapter.MainAppListSimpleAdapter
import cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilder
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.fuf.FUFSinglePackage
import cf.playhi.freezeyou.service.FUFService
import cf.playhi.freezeyou.service.ForceStopService
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys.launchMode
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys.mainActivityPattern
import cf.playhi.freezeyou.ui.*
import cf.playhi.freezeyou.ui.fragment.MainActivityAppListFragment
import cf.playhi.freezeyou.utils.*
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getGrayBitmap
import cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.ClipboardUtils.copyToClipboard
import cf.playhi.freezeyou.utils.FUFUtils.askRun
import cf.playhi.freezeyou.utils.FUFUtils.processFreezeAction
import cf.playhi.freezeyou.utils.FUFUtils.processUnfreezeAction
import cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus
import cf.playhi.freezeyou.utils.LauncherShortcutUtils.checkSettingsAndRequestCreateShortcut
import cf.playhi.freezeyou.utils.LauncherShortcutUtils.createShortCut
import cf.playhi.freezeyou.utils.MoreUtils.processListFilter
import cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite
import cf.playhi.freezeyou.utils.OneKeyListUtils.addToOneKeyList
import cf.playhi.freezeyou.utils.OneKeyListUtils.removeFromOneKeyList
import cf.playhi.freezeyou.utils.Support.showChooseActionPopupMenu
import cf.playhi.freezeyou.utils.ThemeUtils.getThemeDot
import cf.playhi.freezeyou.utils.ThemeUtils.getThemeSecondDot
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import cf.playhi.freezeyou.utils.VersionUtils.checkUpdate
import cf.playhi.freezeyou.utils.VersionUtils.getVersionCode
import cf.playhi.freezeyou.utils.VersionUtils.isOutdated
import net.grandcentrix.tray.AppPreferences
import java.io.*
import java.text.Collator
import java.util.*

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
class Main : FreezeYouBaseActivity() {
    private val selectedPackages = ArrayList<String>()
    private var appListViewOnClickMode = APPListViewOnClickMode_chooseAction
    private var customThemeDisabledDot = R.drawable.shapedotblue
    private var customThemeEnabledDot = R.drawable.shapedotblack
    private var updateFrozenStatusBroadcastReceiver: BroadcastReceiver? = null
    private var currentFilter = "all"
    private var currentSortRule = SORT_BY_DEFAULT
    private var needProcessOnItemCheckedStateChanged = true
    private var shortcutsCompleted = true
    private var shortcutsCount = 0
    private var isGridMode = false
    private var mMainActivityAppListFragment: MainActivityAppListFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        try {
            manageCrashLog()
        } catch (e: Exception) {
            e.printStackTrace()
            checkIfNeedAskFirstTimeSetupAndShowDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!shortcutsCompleted && shortcutsCount > 0) {
            shortcutsCount -= 1
            val pkgName = selectedPackages[shortcutsCount]
            createShortCut(
                getApplicationLabel(this@Main, null, null, pkgName),
                pkgName,
                getApplicationIcon(
                    this@Main,
                    pkgName,
                    getApplicationInfoFromPkgName(pkgName, this),
                    false
                ),
                Freeze::class.java,
                "FreezeYou! $pkgName",
                this@Main
            )
            shortcutsCompleted = shortcutsCount <= 0
        }
        updateFrozenStatus()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPref.getBoolean(saveOnClickFunctionStatus.name, saveOnClickFunctionStatus.defaultValue())) {
            appListViewOnClickMode = sharedPref.getInt("onClickFunctionStatus", APPListViewOnClickMode_chooseAction)
        }
        if (sharedPref.getBoolean(saveSortMethodStatus.name, saveSortMethodStatus.defaultValue())) {
            currentSortRule = sharedPref.getInt("sortMethodStatus", SORT_BY_DEFAULT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateFrozenStatusBroadcastReceiver != null) unregisterReceiver(
            updateFrozenStatusBroadcastReceiver
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            80001 -> if (resultCode == RESULT_OK && data != null) {
                val c80001Title = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: ""
                val shortcutIntent = data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
                if (shortcutIntent != null) {
                    LauncherShortcutUtils.requestCreateShortCut(
                        c80001Title,
                        shortcutIntent.setAction(Intent.ACTION_MAIN),
                        null,
                        "FreezeYou! FolderShortcut" + c80001Title + Date().time,
                        this,
                        data.getParcelableExtra<Bitmap>(Intent.EXTRA_SHORTCUT_ICON)
                    )
                }
            }
            else -> {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        onPrepareMainOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return onMainOptionsItemSelected(item)
    }

    private fun generateList(filter: String) {
        generateList(filter, currentSortRule)
    }

    /**
     * @param filter   筛选规则 或 所有需要显示的软件包包名（以“,”分割）
     * @param sortRule 排序规则
     */
    private fun generateList(filter: String, sortRule: Int) {
        currentFilter = filter
        currentSortRule = sortRule
        val appListFragmentContainer =
            findViewById<FrameLayout>(R.id.main_appList_fragmentContainer_frameLayout)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val mainCautionTextView = findViewById<TextView>(R.id.main_caution_textView)
        val mainLoadingProgressTextView = findViewById<TextView>(R.id.main_loading_progress_textView)
        val linearLayout = findViewById<FrameLayout>(R.id.layout2)
        val AppList = ArrayList<MutableMap<String, Any?>>()
        val searchEditText = findViewById<EditText>(R.id.search_editText)
        val moreSettingsImageButton = findViewById<ImageButton>(R.id.main_moreSettings_button)
        val applicationContext = applicationContext
        moreSettingsImageButton.setOnLongClickListener {
            if (moreSettingsImageButton.alpha == 0.2f) {
                moreSettingsImageButton.alpha = 1f
            } else {
                moreSettingsImageButton.alpha = 0.2f
            }
            true
        }
        moreSettingsImageButton.setOnClickListener {
            moreSettingsImageButton.alpha = 1f
            val popupMenu = PopupMenu(this@Main, it)
            popupMenu.inflate(R.menu.menu)
            onPrepareMainOptionsMenu(popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item -> onMainOptionsItemSelected(item) }
            popupMenu.setOnDismissListener {
                val animation = RotateAnimation(
                    45f,
                    0f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                animation.duration = 300
                animation.repeatMode = RotateAnimation.REVERSE
                animation.fillAfter = true
                moreSettingsImageButton.startAnimation(animation)
            }
            val animation = RotateAnimation(
                0f,
                45f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            animation.duration = 300
            animation.repeatMode = RotateAnimation.REVERSE
            animation.fillAfter = true
            moreSettingsImageButton.startAnimation(animation)
            popupMenu.show()
        }
        if (isFinishing) return
        runOnUiThread {
            moreSettingsImageButton.setBackgroundResource(R.drawable.oval_ripple)
            linearLayout.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            mainLoadingProgressTextView.visibility = View.VISIBLE
            appListFragmentContainer.visibility = View.GONE
            mainLoadingProgressTextView.setText(R.string.loadingPkgList)
            if (noCaution.getValue(applicationContext)) {
                mainCautionTextView.visibility = View.GONE
            }
        }
        try {
            customThemeDisabledDot = getThemeDot(this@Main)
            customThemeEnabledDot = getThemeSecondDot(this@Main)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var packageInfo1: PackageInfo
        val packageManager = applicationContext.packageManager
        val packageInfo =
            packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES)
        val size = packageInfo.size
        val saveIconCache = cacheApplicationsIcons.getValue(applicationContext)
        when (filter) {
            "all" -> {
                for (i in 0 until size) {
                    packageInfo1 = packageInfo[i]
                    val keyValuePair = processAppStatus(
                        getApplicationLabel(
                            applicationContext,
                            packageManager,
                            packageInfo1.applicationInfo,
                            packageInfo1.packageName
                        ),
                        packageInfo1.packageName,
                        packageInfo1,
                        packageManager,
                        saveIconCache
                    )
                    if (keyValuePair != null) {
                        AppList.add(keyValuePair)
                    }
                }
                checkAndAddNotAvailablePair(AppList)
            }
            "OF" -> {
                for (i in 0 until size) {
                    packageInfo1 = packageInfo[i]
                    val keyValuePair = processAppStatus(
                        getApplicationLabel(
                            applicationContext,
                            packageManager,
                            packageInfo1.applicationInfo,
                            packageInfo1.packageName
                        ),
                        packageInfo1.packageName,
                        packageInfo1,
                        packageManager,
                        saveIconCache
                    )
                    if (keyValuePair != null && customThemeDisabledDot == keyValuePair["isFrozen"] as Int) {
                        AppList.add(keyValuePair)
                    }
                }
                checkAndAddNotAvailablePair(AppList)
            }
            "UF" -> {
                for (i in 0 until size) {
                    packageInfo1 = packageInfo[i]
                    val keyValuePair = processAppStatus(
                        getApplicationLabel(
                            applicationContext,
                            packageManager,
                            packageInfo1.applicationInfo,
                            packageInfo1.packageName
                        ),
                        packageInfo1.packageName,
                        packageInfo1,
                        packageManager,
                        saveIconCache
                    )
                    if (keyValuePair != null && customThemeEnabledDot == keyValuePair["isFrozen"] as Int) {
                        AppList.add(keyValuePair)
                    }
                }
                checkAndAddNotAvailablePair(AppList)
            }
            "OO" -> {
                oneKeyListCheckAndGenerate(
                    AppPreferences(applicationContext).getString(
                        getString(R.string.sAutoFreezeApplicationList),
                        ""
                    ),
                    AppList
                )
                checkAndAddNotAvailablePair(AppList)
            }
            "OOU" -> {
                oneKeyListCheckAndGenerate(
                    AppPreferences(applicationContext).getString(
                        getString(R.string.sOneKeyUFApplicationList),
                        ""
                    ),
                    AppList
                )
                checkAndAddNotAvailablePair(AppList)
            }
            "FOQ" -> {
                oneKeyListCheckAndGenerate(
                    AppPreferences(applicationContext).getString(
                        getString(R.string.sFreezeOnceQuit),
                        ""
                    ),
                    AppList
                )
                checkAndAddNotAvailablePair(AppList)
            }
            "OS" -> {
                for (i in 0 until size) {
                    packageInfo1 = packageInfo[i]
                    val appInfo = packageInfo1.applicationInfo ?: continue
                    if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM) {
                        val keyValuePair = processAppStatus(
                            getApplicationLabel(
                                applicationContext,
                                packageManager,
                                appInfo,
                                packageInfo1.packageName
                            ),
                            packageInfo1.packageName,
                            packageInfo1,
                            packageManager,
                            saveIconCache
                        )
                        if (keyValuePair != null) {
                            AppList.add(keyValuePair)
                        }
                    }
                }
                checkAndAddNotAvailablePair(AppList)
            }
            "OU" -> {
                for (i in 0 until size) {
                    packageInfo1 = packageInfo[i]
                    val appInfo = packageInfo1.applicationInfo ?: continue
                    if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != ApplicationInfo.FLAG_SYSTEM) {
                        val keyValuePair = processAppStatus(
                            getApplicationLabel(
                                applicationContext,
                                packageManager,
                                appInfo,
                                packageInfo1.packageName
                            ),
                            packageInfo1.packageName,
                            packageInfo1,
                            packageManager,
                            saveIconCache
                        )
                        if (keyValuePair != null) {
                            AppList.add(keyValuePair)
                        }
                    }
                }
                checkAndAddNotAvailablePair(AppList)
            }
            "UFU" -> {
                for (i in 0 until size) {
                    packageInfo1 = packageInfo[i]
                    val appInfo = packageInfo1.applicationInfo ?: continue
                    if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != ApplicationInfo.FLAG_SYSTEM) {
                        val keyValuePair = processAppStatus(
                            getApplicationLabel(
                                applicationContext,
                                packageManager,
                                appInfo,
                                packageInfo1.packageName
                            ),
                            packageInfo1.packageName,
                            packageInfo1,
                            packageManager,
                            saveIconCache
                        )
                        if (keyValuePair != null && customThemeEnabledDot == keyValuePair["isFrozen"] as Int) {
                            AppList.add(keyValuePair)
                        }
                    }
                }
                checkAndAddNotAvailablePair(AppList)
            }
            else -> {
                oneKeyListCheckAndGenerate(filter, AppList)
                checkAndAddNotAvailablePair(AppList)
            }
        }
        if (isFinishing) return
        runOnUiThread { mainLoadingProgressTextView.setText(R.string.sorting) }
        if (AppList.isNotEmpty()) {
            when (sortRule) {
                SORT_BY_DEFAULT -> setSortByDefault(AppList)
                SORT_BY_UF_ASCENDING -> {
                    setSortByDefault(AppList)
                    val ufTimesMap = getUFreezeTimesMap()
                    AppList.sortWith { m0, m1 ->
                        compareByTimesMap(m0, m1, ufTimesMap, descending = false)
                    }
                }
                SORT_BY_UF_DESCENDING -> {
                    setSortByDefault(AppList)
                    val uFreezeTimesMapTimesMap = getUFreezeTimesMap()
                    AppList.sortWith { m0, m1 ->
                        compareByTimesMap(m0, m1, uFreezeTimesMapTimesMap, descending = true)
                    }
                }
                SORT_BY_FF_ASCENDING -> {
                    setSortByDefault(AppList)
                    val freezeTimesMap = getFreezeTimesMap()
                    AppList.sortWith { m0, m1 ->
                        compareByTimesMap(m0, m1, freezeTimesMap, descending = false)
                    }
                }
                SORT_BY_FF_DESCENDING -> {
                    setSortByDefault(AppList)
                    val freezeTimesMap1 = getFreezeTimesMap()
                    AppList.sortWith { m0, m1 ->
                        compareByTimesMap(m0, m1, freezeTimesMap1, descending = true)
                    }
                }
                SORT_BY_US_ASCENDING -> {
                    setSortByDefault(AppList)
                    val useTimesMap1 = getUseTimesMap()
                    AppList.sortWith { m0, m1 ->
                        compareByTimesMap(m0, m1, useTimesMap1, descending = false)
                    }
                }
                SORT_BY_US_DESCENDING -> {
                    setSortByDefault(AppList)
                    val useTimesMap = getUseTimesMap()
                    AppList.sortWith { m0, m1 ->
                        compareByTimesMap(m0, m1, useTimesMap, descending = true)
                    }
                }
                SORT_BY_ALPHABETICAL -> {
                    setSortByDefault(AppList)
                    val collator = Collator.getInstance()
                    AppList.sortWith { m0: Map<String, Any?>, m1: Map<String, Any?> ->
                        collator.compare(
                            m0["Name"] as String?,
                            m1["Name"] as String?
                        )
                    }
                }
                SORT_BY_LAST_INSTALLED -> {
                    setSortByDefault(AppList)
                    AppList.sortWith { m0: Map<String, Any?>, m1: Map<String, Any?> ->
                        (m1["InstallTime"] as Long).compareTo(m0["InstallTime"] as Long)
                    }
                }
                SORT_BY_LAST_UPDATED -> {
                    setSortByDefault(AppList)
                    AppList.sortWith { m0: Map<String, Any?>, m1: Map<String, Any?> ->
                        (m1["UpdateTime"] as Long).compareTo(m0["UpdateTime"] as Long)
                    }
                }
                SORT_BY_NO -> {}
                else -> {}
            }
        }
        if (isFinishing) return
        runOnUiThread {
            val fragment = mMainActivityAppListFragment ?: return@runOnUiThread
            // Clone so search/filter mutations cannot clear the master AppList.
            val adapter = fragment.setAppListAdapter(
                this@Main,
                ArrayList(AppList),
                selectedPackages
            )
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                    if (adapter == null) return
                    val query = charSequence?.toString().orEmpty()
                    if (query.isEmpty()) {
                        adapter.replaceAllInFormerArrayList(AppList)
                    } else {
                        adapter.replaceAllInFormerArrayList(
                            processListFilter(
                                query,
                                AppList as ArrayList<Map<String, Any?>>
                            ) as List<MutableMap<String, Any?>>
                        )
                    }
                }

                override fun afterTextChanged(editable: Editable) {}
            })
            mainLoadingProgressTextView.setText(R.string.finish)
            progressBar.visibility = View.GONE
            mainCautionTextView.visibility = View.GONE
            mainLoadingProgressTextView.visibility = View.GONE
            linearLayout.visibility = View.GONE
            appListFragmentContainer.visibility = View.VISIBLE
            fragment.setMultiChoiceModeListener(object :
                AbsListView.MultiChoiceModeListener {
                override fun onItemCheckedStateChanged(
                    actionMode: ActionMode,
                    i: Int,
                    l: Long,
                    b: Boolean
                ) {
                    val mainAdapter =
                        fragment.getAppListAdapter() as? MainAppListSimpleAdapter ?: return
                    val pkgName =
                        mainAdapter.getStoredArrayList().getOrNull(i)?.get("PackageName") as? String
                    if (needProcessOnItemCheckedStateChanged) {
                        if (pkgName != null && selectedPackages.contains(pkgName)) {
                            selectedPackages.remove(pkgName)
                        } else {
                            if (pkgName != null) {
                                selectedPackages.add(pkgName)
                            }
                        }
                        needProcessOnItemCheckedStateChanged = false
                        fragment.setItemChecked(i, true)
                        actionMode.title = selectedPackages.size.toString()
                        adapter?.notifyDataSetChanged()
                    } else {
                        needProcessOnItemCheckedStateChanged = true
                    }
                }

                override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                    this@Main.menuInflater.inflate(R.menu.multichoicemenu, menu)
                    return true
                }

                override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                    try {
                        val addToUserDefinedSubMenu =
                            menu.findItem(R.id.list_menu_groupItem_addToUserDefined).subMenu
                                ?: return false
                        val removeFromUserDefinedSubMenu =
                            menu.findItem(R.id.list_menu_groupItem_removeFromUserDefined).subMenu
                                ?: return false
                        addToUserDefinedSubMenu.clear() // 清空先前产生的数据
                        removeFromUserDefinedSubMenu.clear()
                        addToUserDefinedSubMenu.add(
                            R.id.list_menu_groupItem_addToUserDefined_menuGroup,
                            R.id.list_menu_groupItem_addToUserDefined_newClassification,
                            0,
                            R.string.newClassification
                        ) // 加入“新建分类”
                        removeFromUserDefinedSubMenu.add(
                            R.id.list_menu_groupItem_removeFromUserDefined_menuGroup,
                            R.id.list_menu_groupItem_removeFromUserDefined_newClassification,
                            0,
                            R.string.newClassification
                        ) // 加入“新建分类”

                        // 添加用户定义的自定义分类
                        val userDefinedDb =
                            openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null)
                        userDefinedDb.execSQL(
                            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                        )
                        val cursor = userDefinedDb.query(
                            "categories",
                            arrayOf("label", "_id"),
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
                                addToUserDefinedSubMenu.add(
                                    R.id.list_menu_groupItem_addToUserDefined_menuGroup,
                                    id,
                                    id,
                                    String(Base64.decode(title, Base64.DEFAULT))
                                )
                                removeFromUserDefinedSubMenu.add(
                                    R.id.list_menu_groupItem_removeFromUserDefined_menuGroup,
                                    id,
                                    id,
                                    String(Base64.decode(title, Base64.DEFAULT))
                                )
                                cursor.moveToNext()
                            }
                        }
                        cursor.close()
                        userDefinedDb.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return false
                }

                override fun onActionItemClicked(
                    actionMode: ActionMode,
                    menuItem: MenuItem
                ): Boolean {
                    when (menuItem.groupId) {
                        R.id.list_menu_groupItem_addToUserDefined_menuGroup, R.id.list_menu_groupItem_removeFromUserDefined_menuGroup -> when (menuItem.itemId) {
                            R.id.list_menu_groupItem_addToUserDefined_newClassification, R.id.list_menu_groupItem_removeFromUserDefined_newClassification -> {
                                showAddNewUserDefinedClassificationDialog()
                                return true
                            }
                            else -> {
                                val isInRemoveMode =
                                    menuItem.groupId == R.id.list_menu_groupItem_removeFromUserDefined_menuGroup
                                val title = menuItem.title.toString()
                                var existsPkgsList: List<String?> = ArrayList()
                                var existsPkgs = ""
                                val userDefinedDb = openOrCreateDatabase(
                                    "userDefinedCategories",
                                    Context.MODE_PRIVATE,
                                    null
                                )
                                userDefinedDb.execSQL(
                                    "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                                )
                                val cursor = userDefinedDb.query(
                                    "categories",
                                    arrayOf("packages"),
                                    "label = '" + Base64.encodeToString(
                                        title.toByteArray(),
                                        Base64.DEFAULT
                                    ) + "'",
                                    null, null, null, null
                                )
                                if (cursor.moveToFirst()) {
                                    existsPkgs = cursor.getString(
                                        cursor.getColumnIndexOrThrow("packages")
                                    )
                                    existsPkgsList = listOf(*existsPkgs.split(",").toTypedArray())
                                }
                                val size = selectedPackages.size
                                if (isInRemoveMode) {
                                    for (i in 0 until size) {
                                        if (existsPkgsList.contains(selectedPackages[i])) {
                                            existsPkgs = existsPkgs.replace(
                                                selectedPackages[i] + ",",
                                                ""
                                            )
                                        }
                                    }
                                } else {
                                    val existsPkgsBuilder = StringBuilder(existsPkgs)
                                    for (i in 0 until size) {
                                        if (!existsPkgsList.contains(selectedPackages[i])) {
                                            existsPkgsBuilder.append(selectedPackages[i])
                                                .append(",")
                                        }
                                    }
                                    existsPkgs = existsPkgsBuilder.toString()
                                }
                                userDefinedDb.execSQL(
                                    "UPDATE categories SET packages = '"
                                            + existsPkgs
                                            + "' WHERE label = '"
                                            + Base64.encodeToString(
                                        title.toByteArray(),
                                        Base64.DEFAULT
                                    )
                                            + "';"
                                )
                                cursor.close()
                                userDefinedDb.close()
                                showToast(this@Main, if (isInRemoveMode) R.string.removed else R.string.added)
                                return true
                            }
                        }
                    }
                    when (menuItem.itemId) {
                        R.id.list_menu_selectAll -> {
                            val adpt = fragment.getAppListAdapter() as? MainAppListSimpleAdapter
                            if (adpt != null) {
                                for (i in 0 until adpt.count) {
                                    val pkg =
                                        adpt.getStoredArrayList().getOrNull(i)?.get("PackageName") as? String
                                    if (pkg != null && !selectedPackages.contains(pkg)) {
                                        selectedPackages.add(pkg)
                                    }
                                }
                                actionMode.title = selectedPackages.size.toString()
                                adpt.notifyDataSetChanged()
                            }
                            return true
                        }
                        R.id.list_menu_selectUnselected -> {
                            val adapt = fragment.getAppListAdapter() as? MainAppListSimpleAdapter
                            if (adapt != null) {
                                for (i in 0 until adapt.count) {
                                    val pkg =
                                        adapt.getStoredArrayList().getOrNull(i)?.get("PackageName") as? String
                                    if (pkg != null) {
                                        if (selectedPackages.contains(pkg)) {
                                            selectedPackages.remove(pkg)
                                        } else {
                                            selectedPackages.add(pkg)
                                        }
                                    }
                                }
                                actionMode.title = selectedPackages.size.toString()
                                adapt.notifyDataSetChanged()
                            }
                            return true
                        }
                        R.id.list_menu_addToOneKeyFreezeList -> {
                            processAddToOneKeyList(getString(R.string.sAutoFreezeApplicationList))
                            return true
                        }
                        R.id.list_menu_addToOneKeyUFList -> {
                            processAddToOneKeyList(getString(R.string.sOneKeyUFApplicationList))
                            return true
                        }
                        R.id.list_menu_addToFreezeOnceQuit -> {
                            processAddToOneKeyList(getString(R.string.sFreezeOnceQuit))
                            return true
                        }
                        R.id.list_menu_removeFromOneKeyFreezeList -> {
                            processRemoveFromOneKeyList(getString(R.string.sAutoFreezeApplicationList))
                            return true
                        }
                        R.id.list_menu_removeFromOneKeyUFList -> {
                            processRemoveFromOneKeyList(getString(R.string.sOneKeyUFApplicationList))
                            return true
                        }
                        R.id.list_menu_removeFromFreezeOnceQuit -> {
                            processRemoveFromOneKeyList(getString(R.string.sFreezeOnceQuit))
                            return true
                        }
                        R.id.list_menu_freezeImmediately -> {
                            processDisableAndEnableImmediately(true)
                            actionMode.finish()
                            return true
                        }
                        R.id.list_menu_UFImmediately -> {
                            processDisableAndEnableImmediately(false)
                            actionMode.finish()
                            return true
                        }
                        R.id.list_menu_ForceStopImmediately -> {
                            processForceStopImmediately()
                            actionMode.finish()
                            return true
                        }
                        R.id.list_menu_createDisEnableShortCut -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val mShortcutManager =
                                    this@Main.getSystemService(ShortcutManager::class.java)
                                if (mShortcutManager != null && mShortcutManager.isRequestPinShortcutSupported) {
                                    shortcutsCount = selectedPackages.size - 1
                                    if (shortcutsCount >= 0) {
                                        val pkgName = selectedPackages[shortcutsCount]
                                        createShortCut(
                                            getApplicationLabel(this@Main, null, null, pkgName),
                                            pkgName,
                                            getApplicationIcon(
                                                this@Main,
                                                pkgName,
                                                getApplicationInfoFromPkgName(pkgName, this@Main),
                                                false
                                            ),
                                            Freeze::class.java,
                                            "FreezeYou! $pkgName",
                                            this@Main
                                        )
                                    }
                                    shortcutsCompleted = shortcutsCount <= 0
                                } else {
                                    createFUFShortcutsBatch()
                                }
                            } else {
                                createFUFShortcutsBatch()
                            }
                            return true
                        }
                        R.id.list_menu_copyAfterBeingFormatted -> {
                            val formattedPackages = StringBuilder()
                            val size = selectedPackages.size
                            for (i in 0 until size) {
                                formattedPackages.append(selectedPackages[i]).append(",")
                            }
                            if (copyToClipboard(this@Main, formattedPackages.toString())) {
                                showToast(this@Main, R.string.success)
                            } else {
                                showToast(this@Main, R.string.failed)
                            }
                            return true
                        }
                        else -> return false
                    }
                }

                override fun onDestroyActionMode(actionMode: ActionMode) {
                    selectedPackages.clear()
                    adapter?.notifyDataSetChanged()
                }
            })
        }

        mMainActivityAppListFragment?.setOnAppListItemClickListener { _, view, i, _ ->
            val map =
                (mMainActivityAppListFragment?.getAppListAdapter() as? MainAppListSimpleAdapter)
                    ?.getStoredArrayList()?.getOrNull(i) ?: return@setOnAppListItemClickListener
            val name = map["Name"] as String?
            val pkgName = map["PackageName"] as String?
            if (name != null && pkgName != null && getString(R.string.notAvailable) != name) {
                when (appListViewOnClickMode) {
                    APPListViewOnClickMode_chooseAction -> {
                        showChooseActionPopupMenu(this@Main, this@Main, view, pkgName, name)
                    }
                    APPListViewOnClickMode_autoUFOrFreeze -> {
                        if (realGetFrozenStatus(this@Main, pkgName, null)) {
                            processUnfreezeAction(
                                this@Main, pkgName, null, null,
                                false, false, null, false
                            )
                        } else {
                            processFreezeAction(
                                this@Main, pkgName, null, null,
                                false, null, false
                            )
                        }
                    }
                    APPListViewOnClickMode_freezeImmediately -> {
                        if (!realGetFrozenStatus(this@Main, pkgName, null)) {
                            processFreezeAction(
                                this@Main, pkgName, null, null,
                                false, null, false
                            )
                        } else {
                            if (!lesserToast.getValue(null)) {
                                showToast(this@Main, R.string.freezeCompleted)
                            }
                        }
                    }
                    APPListViewOnClickMode_UFImmediately -> {
                        if (realGetFrozenStatus(this@Main, pkgName, null)) {
                            processUnfreezeAction(
                                this@Main, pkgName, null, null,
                                false, false, null, false
                            )
                        } else {
                            if (!lesserToast.getValue(null)) {
                                showToast(this@Main, R.string.UFCompleted)
                            }
                        }
                    }
                    APPListViewOnClickMode_UFAndRun -> {
                        if (realGetFrozenStatus(this@Main, pkgName, null)) {
                            processUnfreezeAction(
                                this@Main, pkgName, null, null,
                                true, false, null, false
                            )
                        } else {
                            if (!lesserToast.getValue(null)) {
                                showToast(this@Main, R.string.UFCompleted)
                            }
                            askRun(
                                this@Main, pkgName, null,
                                null, false, null, false
                            )
                        }
                    }
                    APPListViewOnClickMode_autoUFOrFreezeAndRun -> {
                        if (realGetFrozenStatus(this@Main, pkgName, null)) {
                            processUnfreezeAction(
                                this@Main, pkgName, null, null,
                                true, false, null, false
                            )
                        } else {
                            processFreezeAction(
                                this@Main, pkgName, null, null,
                                false, null, false
                            )
                        }
                    }
                    APPListViewOnClickMode_addToOFList -> {
                        showToast(
                            this@Main,
                            if (addToOneKeyList(
                                    this@Main,
                                    getString(R.string.sAutoFreezeApplicationList),
                                    pkgName
                                )
                            ) R.string.added else R.string.failed
                        )
                    }
                    APPListViewOnClickMode_removeFromOFList -> {
                        showToast(
                            this@Main,
                            if (removeFromOneKeyList(
                                    this@Main,
                                    getString(R.string.sAutoFreezeApplicationList),
                                    pkgName
                                )
                            ) R.string.removed else R.string.failed
                        )
                    }
                    APPListViewOnClickMode_addToOUFList -> {
                        showToast(
                            this@Main,
                            if (addToOneKeyList(
                                    this@Main,
                                    getString(R.string.sOneKeyUFApplicationList),
                                    pkgName
                                )
                            ) R.string.added else R.string.failed
                        )
                    }
                    APPListViewOnClickMode_removeFromOUFList -> {
                        showToast(
                            this@Main,
                            if (removeFromOneKeyList(
                                    this@Main,
                                    getString(R.string.sOneKeyUFApplicationList),
                                    pkgName
                                )
                            ) R.string.removed else R.string.failed
                        )
                    }
                    APPListViewOnClickMode_addToFOQList -> {
                        showToast(
                            this@Main,
                            if (addToOneKeyList(
                                    this@Main,
                                    getString(R.string.sFreezeOnceQuit),
                                    pkgName
                                )
                            ) R.string.added else R.string.failed
                        )
                    }
                    APPListViewOnClickMode_removeFromFOQList -> {
                        showToast(
                            this@Main,
                            if (removeFromOneKeyList(
                                    this@Main,
                                    getString(R.string.sFreezeOnceQuit),
                                    pkgName
                                )
                            ) R.string.removed else R.string.failed
                        )
                    }
                    APPListViewOnClickMode_createFUFShortcut -> {
                        checkSettingsAndRequestCreateShortcut(
                            name,
                            pkgName,
                            getApplicationIcon(
                                this@Main,
                                pkgName,
                                getApplicationInfoFromPkgName(pkgName, this@Main),
                                false
                            ),
                            Freeze::class.java,
                            "FreezeYou! $pkgName",
                            this@Main
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    private fun generateListForCategory(base64Label: String) {
        val userDefinedDb = openOrCreateDatabase("userDefinedCategories", Context.MODE_PRIVATE, null)
        userDefinedDb.execSQL(
            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        )
        val cursor = userDefinedDb.query(
            "categories",
            arrayOf("packages"),
            "label = '$base64Label'",
            null, null, null, null
        )
        if (cursor.moveToFirst()) {
            generateList(cursor.getString(cursor.getColumnIndexOrThrow("packages")))
        } else {
            showToast(this, R.string.failed)
        }
        cursor.close()
        userDefinedDb.close()
    }

    private fun onPrepareMainOptionsMenu(menu: Menu) {
        try {
            val vmUserDefinedSubMenu = menu.findItem(R.id.menu_vM_userDefined).subMenu ?: return
            val createUserDefinedShortcutSubMenu =
                menu.findItem(R.id.menu_createUserDefinedShortcut).subMenu ?: return
            val forceStopUserDefinedShortcutSubMenu =
                menu.findItem(R.id.menu_forceStopUserDefinedShortcut).subMenu ?: return
            addUserDefinedCategoriesTo(
                vmUserDefinedSubMenu,
                R.id.menu_vM_userDefined_menuGroup,
                R.id.menu_vM_userDefined_newClassification,
                0
            )
            addUserDefinedCategoriesTo(
                createUserDefinedShortcutSubMenu,
                R.id.menu_createUserDefinedShortcut_menuGroup,
                R.id.menu_createUserDefinedShortcut_newClassification,
                0x2AAAAAAA
            )
            addUserDefinedCategoriesTo(
                forceStopUserDefinedShortcutSubMenu,
                R.id.menu_forceStopUserDefinedShortcut_menuGroup,
                R.id.menu_forceStopUserDefinedShortcut_newClassification,
                0x55555555
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onMainOptionsItemSelected(item: MenuItem): Boolean {
        when (item.groupId) {
            R.id.menu_vM_userDefined_menuGroup -> {
                return when (item.itemId) {
                    R.id.menu_vM_userDefined_newClassification -> {
                        showAddNewUserDefinedClassificationDialog()
                        true
                    }
                    else -> {
                        val title = item.title.toString()
                        generateListForCategory(
                            Base64.encodeToString(
                                title.toByteArray(),
                                Base64.DEFAULT
                            )
                        )
                        true
                    }
                }
            }
            R.id.menu_createUserDefinedShortcut_menuGroup -> {
                return when (item.itemId) {
                    R.id.menu_createUserDefinedShortcut_newClassification -> {
                        showAddNewUserDefinedClassificationDialog()
                        true
                    }
                    else -> {
                        val title = item.title.toString()
                        @Suppress("DEPRECATION")
                        checkSettingsAndRequestCreateShortcut(
                            title,
                            "CATEGORY" + Base64.encodeToString(title.toByteArray(), Base64.DEFAULT),
                            shortcutIconDrawable(R.mipmap.ic_launcher_round),
                            Main::class.java,
                            "Category " + Base64.encodeToString(title.toByteArray(), Base64.DEFAULT),
                            this
                        )
                        true
                    }
                }
            }
            R.id.menu_forceStopUserDefinedShortcut_menuGroup -> {
                return when (item.itemId) {
                    R.id.menu_forceStopUserDefinedShortcut_newClassification -> {
                        showAddNewUserDefinedClassificationDialog()
                        true
                    }
                    else -> {
                        val title = item.title.toString()
                        @Suppress("DEPRECATION")
                        checkSettingsAndRequestCreateShortcut(
                            getString(R.string.forceStop) + " " + title,
                            "FORCESTOPCATEGORY" + Base64.encodeToString(
                                title.toByteArray(),
                                Base64.DEFAULT
                            ),
                            shortcutIconDrawable(R.mipmap.ic_launcher_round),
                            ForceStop::class.java,
                            "ForceStopCategory " + Base64.encodeToString(
                                title.toByteArray(),
                                Base64.DEFAULT
                            ),
                            this
                        )
                        true
                    }
                }
            }
        }
        when (item.itemId) {
            R.id.menu_createOneKeyFreezeShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.oneKeyFreeze),
                    "cf.playhi.freezeyou.extra.fuf",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    OneKeyFreeze::class.java,
                    "OneKeyFreeze",
                    this
                )
                return true
            }
            R.id.menu_createOneKeyUFShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.oneKeyUF),
                    "cf.playhi.freezeyou.extra.fuf",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    OneKeyUF::class.java,
                    "OneKeyUF",
                    this
                )
                return true
            }
            R.id.menu_createOneKeyLockScreenShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.oneKeyLockScreen),
                    "cf.playhi.freezeyou.extra.oklock",
                    shortcutIconDrawable(R.drawable.screenlock),
                    OneKeyScreenLockImmediatelyActivity::class.java,
                    "OneKeyLockScreen",
                    this
                )
                return true
            }
            R.id.menu_createOnlyFrozenShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.onlyFrozen),
                    "OF",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "OF",
                    this
                )
                return true
            }
            R.id.menu_createOnlyUFShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.onlyUF),
                    "UF",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "UF",
                    this
                )
                return true
            }
            R.id.menu_createOnlyOnekeyShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.onlyOnekey),
                    "OO",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "OO",
                    this
                )
                return true
            }
            R.id.menu_createOnlyOnekeyUFShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.oneKeyUF),
                    "OOU",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "OOU",
                    this
                )
                return true
            }
            R.id.menu_createFreezeOnceQuitShortCut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.freezeOnceQuit),
                    "FOQ",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "FOQ",
                    this
                )
                return true
            }
            R.id.menu_createOnlySAShortcut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.onlySA),
                    "OS",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "OS",
                    this
                )
                return true
            }
            R.id.menu_createOnlyUAShortcut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.onlyUA),
                    "OU",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "OU",
                    this
                )
                return true
            }
            R.id.menu_createUnfrozenUAShortcut -> {
                @Suppress("DEPRECATION")
                checkSettingsAndRequestCreateShortcut(
                    getString(R.string.unfrozenUA),
                    "UFU",
                    shortcutIconDrawable(R.mipmap.ic_launcher_round),
                    Main::class.java,
                    "UFU",
                    this
                )
                return true
            }
            R.id.menu_createNewFolderShortCut -> {
                startActivityForResult(
                    Intent(this, ShortcutLauncherFolderActivity::class.java)
                        .setAction(Intent.ACTION_CREATE_SHORTCUT),
                    80001
                )
                return true
            }
            R.id.menu_timedTasks -> {
                startActivity(Intent(this, ScheduledTasksManageActivity::class.java))
                return true
            }
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.menu_oneKeyFreezeImmediately -> {
                startActivity(
                    Intent(this, OneKeyFreeze::class.java).putExtra(
                        "autoCheckAndLockScreen",
                        false
                    )
                )
                return true
            }
            R.id.menu_oneKeyUFImmediately -> {
                startActivity(Intent(this, OneKeyUF::class.java))
                return true
            }
            R.id.menu_vM_onlyFrozen -> {
                Thread { generateList("OF") }.start()
                return true
            }
            R.id.menu_vM_onlyUF -> {
                Thread { generateList("UF") }.start()
                return true
            }
            R.id.menu_vM_all -> {
                Thread { generateList("all") }.start()
                return true
            }
            R.id.menu_vM_onlyOnekey -> {
                Thread { generateList("OO") }.start()
                return true
            }
            R.id.menu_vM_onlyOnekeyUF -> {
                Thread { generateList("OOU") }.start()
                return true
            }
            R.id.menu_vM_onlySA -> {
                Thread { generateList("OS") }.start()
                return true
            }
            R.id.menu_vM_onlyUA -> {
                Thread { generateList("OU") }.start()
                return true
            }
            R.id.menu_vM_freezeOnceQuit -> {
                Thread { generateList("FOQ") }.start()
                return true
            }
            R.id.menu_vM_unfrozenUA -> {
                Thread { generateList("UFU") }.start()
                return true
            }
            R.id.menu_vM_userDefined -> {
                item.subMenu?.let { subMenu ->
                    addUserDefinedCategoriesTo(
                        subMenu,
                        R.id.menu_vM_userDefined_menuGroup,
                        R.id.menu_vM_userDefined_newClassification,
                        0
                    )
                }
                return true
            }
            R.id.menu_update -> {
                checkUpdate(this@Main)
                return true
            }
            R.id.menu_moreSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.menu_faq -> {
                requestOpenWebSite(
                    this,
                    String.format(
                        "https://www.zidon.net/%1\$s/faq/",
                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                    )
                )
                return true
            }
            R.id.menu_onClickFunc_autoUFOrFreeze -> {
                appListViewOnClickMode = APPListViewOnClickMode_autoUFOrFreeze
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_freezeImmediately -> {
                appListViewOnClickMode = APPListViewOnClickMode_freezeImmediately
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_UFImmediately -> {
                appListViewOnClickMode = APPListViewOnClickMode_UFImmediately
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_chooseAction -> {
                appListViewOnClickMode = APPListViewOnClickMode_chooseAction
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_addToFOQList -> {
                appListViewOnClickMode = APPListViewOnClickMode_addToFOQList
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_addToOFList -> {
                appListViewOnClickMode = APPListViewOnClickMode_addToOFList
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_addToOUFList -> {
                appListViewOnClickMode = APPListViewOnClickMode_addToOUFList
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_removeFromFOQList -> {
                appListViewOnClickMode = APPListViewOnClickMode_removeFromFOQList
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_removeFromOFList -> {
                appListViewOnClickMode = APPListViewOnClickMode_removeFromOFList
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_removeFromOUFList -> {
                appListViewOnClickMode = APPListViewOnClickMode_removeFromOUFList
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_UFAndRun -> {
                appListViewOnClickMode = APPListViewOnClickMode_UFAndRun
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_autoUFOrFreezeAndRun -> {
                appListViewOnClickMode = APPListViewOnClickMode_autoUFOrFreezeAndRun
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_onClickFunc_createFUFShortcut -> {
                appListViewOnClickMode = APPListViewOnClickMode_createFUFShortcut
                saveOnClickFunctionStatus(appListViewOnClickMode)
                return true
            }
            R.id.menu_sB_default -> {
                Thread { generateList(currentFilter, SORT_BY_DEFAULT) }.start()
                saveSortMethodStatus(SORT_BY_DEFAULT)
                return true
            }
            R.id.menu_sB_no -> {
                Thread { generateList(currentFilter, SORT_BY_NO) }.start()
                saveSortMethodStatus(SORT_BY_NO)
                return true
            }
            R.id.menu_sB_uf_ascending -> {
                Thread { generateList(currentFilter, SORT_BY_UF_ASCENDING) }.start()
                saveSortMethodStatus(SORT_BY_UF_ASCENDING)
                return true
            }
            R.id.menu_sB_uf_descending -> {
                Thread { generateList(currentFilter, SORT_BY_UF_DESCENDING) }.start()
                saveSortMethodStatus(SORT_BY_UF_DESCENDING)
                return true
            }
            R.id.menu_sB_ff_ascending -> {
                Thread { generateList(currentFilter, SORT_BY_FF_ASCENDING) }.start()
                saveSortMethodStatus(SORT_BY_FF_ASCENDING)
                return true
            }
            R.id.menu_sB_ff_descending -> {
                Thread { generateList(currentFilter, SORT_BY_FF_DESCENDING) }.start()
                saveSortMethodStatus(SORT_BY_FF_DESCENDING)
                return true
            }
            R.id.menu_sB_us_ascending -> {
                AccessibilityUtils.checkAndRequestIfAccessibilitySettingsOff(this)
                Thread { generateList(currentFilter, SORT_BY_US_ASCENDING) }.start()
                saveSortMethodStatus(SORT_BY_US_ASCENDING)
                return true
            }
            R.id.menu_sB_us_descending -> {
                AccessibilityUtils.checkAndRequestIfAccessibilitySettingsOff(this)
                Thread { generateList(currentFilter, SORT_BY_US_DESCENDING) }.start()
                saveSortMethodStatus(SORT_BY_US_DESCENDING)
                return true
            }
            R.id.menu_sB_alphabetical -> {
                Thread { generateList(currentFilter, SORT_BY_ALPHABETICAL) }.start()
                saveSortMethodStatus(SORT_BY_ALPHABETICAL)
                return true
            }
            R.id.menu_sB_last_install -> {
                Thread { generateList(currentFilter, SORT_BY_LAST_INSTALLED) }.start()
                saveSortMethodStatus(SORT_BY_LAST_INSTALLED)
                return true
            }
            R.id.menu_sB_last_update -> {
                Thread { generateList(currentFilter, SORT_BY_LAST_UPDATED) }.start()
                saveSortMethodStatus(SORT_BY_LAST_UPDATED)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showAddNewUserDefinedClassificationDialog() {
        val vmUserDefinedNameAlertDialogEditText = EditText(this)
        val vmUserDefinedNameAlertDialog = FreezeYouAlertDialogBuilder(this)
        vmUserDefinedNameAlertDialog.setTitle(R.string.label)
        vmUserDefinedNameAlertDialog.setView(vmUserDefinedNameAlertDialogEditText)
        vmUserDefinedNameAlertDialog.setPositiveButton(R.string.save) { _: DialogInterface?, _: Int ->
            val label = Base64.encodeToString(
                vmUserDefinedNameAlertDialogEditText.text.toString().toByteArray(),
                Base64.DEFAULT
            )
            if ("" == label) {
                showToast(this@Main, R.string.emptyNotAllowed)
            } else {
                var alreadyExists = false
                val vmUserDefinedDb = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null)
                vmUserDefinedDb.execSQL(
                    "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                )
                val cursor = vmUserDefinedDb.query(
                    "categories",
                    arrayOf("label"),
                    null,
                    null,
                    null,
                    null,
                    null
                )
                if (cursor.moveToFirst()) {
                    for (i in 0 until cursor.count) {
                        if (label == cursor.getString(cursor.getColumnIndexOrThrow("label"))) {
                            alreadyExists = true
                            break
                        }
                        cursor.moveToNext()
                    }
                }
                cursor.close()
                if (alreadyExists) {
                    showToast(this@Main, R.string.alreadyExist)
                } else {
                    vmUserDefinedDb.execSQL(
                        "replace into categories(_id,label,packages) VALUES ( "
                                + null + ",'"
                                + label + "','')"
                    )
                }
                vmUserDefinedDb.close()
            }
        }
        vmUserDefinedNameAlertDialog.setNegativeButton(R.string.cancel, null)
        vmUserDefinedNameAlertDialog.show()
    }

    override fun finish() {
        if (!showInRecents.getValue(null)) {
            finishAndRemoveTask()
        }
        super.finish()
    }

    private fun manageCrashLog() {
        val crashCheck =
            File(cacheDir.toString() + File.separator + "log" + File.separator + "NeedUpload.log")
        if (crashCheck.exists()) {
            val bufferedReader = BufferedReader(FileReader(crashCheck))
            val filePath = bufferedReader.readLine()
            bufferedReader.close()
            val fileInputStream = FileInputStream(filePath)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(fileInputStream.available())
            fileInputStream.read(buffer)
            byteArrayOutputStream.write(buffer)
            fileInputStream.close()
            AlertDialogUtils.buildAlertDialog(
                this@Main,
                R.mipmap.ic_launcher_new_round,
                R.string.ifUploadCrashLog,
                R.string.notice
            )
                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                    val webPage =
                        Uri.parse("https://freezeyou.playhi.net/crashReport.php?data=" + Base64.encodeToString(
                            byteArrayOutputStream.toByteArray(),
                            Base64.DEFAULT
                        ))
                    val report = Intent(Intent.ACTION_VIEW, webPage)
                    if (report.resolveActivity(packageManager) != null) {
                        startActivity(report)
                    } else {
                        showToast(this@Main, R.string.failed)
                    }
                    checkIfNeedAskFirstTimeSetupAndShowDialog()
                }
                .setNeutralButton(R.string.update) { _: DialogInterface?, _: Int ->
                    checkUpdate(this@Main)
                    checkIfNeedAskFirstTimeSetupAndShowDialog()
                }
                .setNegativeButton(R.string.no) { _: DialogInterface?, _: Int ->
                    checkIfNeedAskFirstTimeSetupAndShowDialog()
                }
                .setOnCancelListener { checkIfNeedAskFirstTimeSetupAndShowDialog() }
                .create()
                .show()
            //删除数据
            File(filePath).delete()
            crashCheck.delete()
        } else {
            checkIfNeedAskFirstTimeSetupAndShowDialog()
        }
    }

    private fun go() {
        if (updateFrozenStatusBroadcastReceiver == null) {
            updateFrozenStatusBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    updateFrozenStatus()
                }
            }
            val filter = IntentFilter("cf.playhi.freezeyou.action.packageStatusChanged")
            filter.addAction("cf.playhi.freezeyou.action.packageStatusChanged")
            ContextCompat.registerReceiver(
                this,
                updateFrozenStatusBroadcastReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
        TasksUtils.checkTimeTasks(this)
        TasksUtils.checkTriggerTasks(this)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this@Main)
        if (sharedPref.getBoolean(saveOnClickFunctionStatus.name, saveOnClickFunctionStatus.defaultValue())) {
            appListViewOnClickMode = sharedPref.getInt("onClickFunctionStatus", APPListViewOnClickMode_chooseAction)
        }
        if (sharedPref.getBoolean(saveSortMethodStatus.name, saveSortMethodStatus.defaultValue())) {
            currentSortRule = sharedPref.getInt("sortMethodStatus", SORT_BY_DEFAULT)
        }
        if (!sharedPref.getBoolean(noCaution.name, noCaution.defaultValue())) {
            AlertDialogUtils.buildAlertDialog(
                this@Main,
                R.mipmap.ic_launcher_new_round,
                R.string.cautionContent,
                R.string.caution
            )
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> }
                .setNeutralButton(R.string.hToUse) { _: DialogInterface?, _: Int ->
                    requestOpenWebSite(this@Main, "https://www.zidon.net/")
                }
                .setNegativeButton(R.string.nCaution) { _: DialogInterface?, _: Int ->
                    sharedPref.edit().putBoolean(noCaution.name, true).apply()
                }
                .create().show()
        }
        val mainPattern = sharedPref.getString(mainActivityPattern.name, mainActivityPattern.defaultValue())
        if (mainActivityPattern.defaultValue().equals(mainPattern, ignoreCase = true)) {
            @Suppress("DEPRECATION")
            isGridMode =
                windowManager.defaultDisplay.width > windowManager.defaultDisplay.height * 1.2
        } else {
            isGridMode = "grid" == mainPattern
        }
        val appListFragment = mMainActivityAppListFragment ?: MainActivityAppListFragment().also {
            it.setUseGridMode(isGridMode)
            mMainActivityAppListFragment = it
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(
            R.id.main_appList_fragmentContainer_frameLayout,
            appListFragment
        )
        fragmentTransaction.commit()
        val initThread = Thread {
            var mode = intent.getStringExtra("pkgName") // 快捷方式提供
            if (mode == null) {
                mode = launchMode.getValue(this@Main)
            }
            if (mode == null) {
                mode = ""
            }
            if (mode.startsWith("CATEGORY")) {
                val categoryLabel = mode.substring("CATEGORY".length)
                generateListForCategory(categoryLabel)
                return@Thread
            }
            when (mode) {
                "OF" -> generateList("OF")
                "UF" -> generateList("UF")
                "OO" -> generateList("OO")
                "OOU" -> generateList("OOU")
                "OS" -> generateList("OS")
                "OU" -> generateList("OU")
                "FOQ" -> generateList("FOQ")
                "UFU" -> generateList("UFU")
                else -> generateList("all")
            }
        }
        initThread.start()
        checkLongTimeNotUpdated()
    }

    private fun processFrozenStatus(
        keyValuePair: MutableMap<String, Any?>,
        packageName: String,
        packageManager: PackageManager?
    ) {
        keyValuePair["isFrozen"] = getFrozenStatus(packageName, packageManager)
    }

    /**
     * @param packageName 应用包名
     * @return 资源 Id
     */
    private fun getFrozenStatus(packageName: String, packageManager: PackageManager?): Int {
        return if (realGetFrozenStatus(
                this@Main,
                packageName,
                packageManager
            )
        ) customThemeDisabledDot else customThemeEnabledDot
    }

    private fun processAppStatus(
        name: String,
        packageName: String,
        packageInfo: PackageInfo,
        packageManager: PackageManager,
        saveIconCache: Boolean
    ): MutableMap<String, Any?>? {
        if (!("android" == packageName || "cf.playhi.freezeyou" == packageName)) {
            val keyValuePair: MutableMap<String, Any?> = HashMap()
            keyValuePair["Img"] =
                if (isGridMode && realGetFrozenStatus(this, packageName, packageManager)) BitmapDrawable(
                    resources,
                    getGrayBitmap(
                        getBitmapFromDrawable(
                            getApplicationIcon(
                                this, packageName,
                                packageInfo.applicationInfo,
                                false,
                                saveIconCache
                            )
                        )
                    )
                ) else getApplicationIcon(
                    this@Main,
                    packageName,
                    packageInfo.applicationInfo,
                    false,
                    saveIconCache
                )
            keyValuePair["Name"] = name
            processFrozenStatus(keyValuePair, packageName, packageManager)
            keyValuePair["PackageName"] = packageName
            keyValuePair["InstallTime"] = packageInfo.firstInstallTime
            keyValuePair["UpdateTime"] = packageInfo.lastUpdateTime
            return keyValuePair
        }
        return null
    }

    private fun oneKeyListGenerate(source: Array<String>, AppList: MutableList<MutableMap<String, Any?>>) {
        var name: String
        var icon: Drawable?
        for (aPkg in source) {
            name = getApplicationLabel(applicationContext, null, null, aPkg)
            var installTime = 0L
            var updateTime = 0L
            try {
                val pi = packageManager.getPackageInfo(aPkg, PackageManager.GET_UNINSTALLED_PACKAGES)
                installTime = pi.firstInstallTime
                updateTime = pi.lastUpdateTime
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            if (!("android" == aPkg || "cf.playhi.freezeyou" == aPkg || "" == aPkg)) {
                val keyValuePair: MutableMap<String, Any?> = HashMap()
                icon =
                    if (isGridMode && realGetFrozenStatus(this, aPkg, null)) BitmapDrawable(
                        resources,
                        getGrayBitmap(
                            getBitmapFromDrawable(
                                getApplicationIcon(
                                    this, aPkg,
                                    getApplicationInfoFromPkgName(aPkg, this),
                                    false
                                )
                            )
                        )
                    ) else getApplicationIcon(
                        this@Main,
                        aPkg,
                        getApplicationInfoFromPkgName(aPkg, this@Main),
                        true
                    )
                keyValuePair["Img"] = icon
                keyValuePair["Name"] = name
                processFrozenStatus(keyValuePair, aPkg, packageManager)
                keyValuePair["PackageName"] = aPkg
                keyValuePair["InstallTime"] = installTime
                keyValuePair["UpdateTime"] = updateTime
                AppList.add(keyValuePair)
            }
        }
    }

    private fun oneKeyListCheckAndGenerate(pkgNames: String?, AppList: MutableList<MutableMap<String, Any?>>) {
        if (pkgNames != null) {
            oneKeyListGenerate(pkgNames.split(",").toTypedArray(), AppList)
        }
    }

    private fun checkAndAddNotAvailablePair(AppList: MutableList<MutableMap<String, Any?>>) {
        if (AppList.size == 0) {
            addNotAvailablePair(applicationContext, AppList)
        }
    }

    private fun processAddToOneKeyList(string: String) {
        val size = selectedPackages.size
        for (i in 0 until size) {
            if (!addToOneKeyList(applicationContext, string, selectedPackages[i])) {
                showToast(this@Main, selectedPackages[i] + getString(R.string.failed))
            }
        }
        showToast(this@Main, R.string.success)
    }

    private fun processRemoveFromOneKeyList(s: String) {
        val size = selectedPackages.size
        for (i in 0 until size) {
            if (!removeFromOneKeyList(applicationContext, s, selectedPackages[i])) {
                showToast(this@Main, selectedPackages[i] + getString(R.string.failed))
            }
        }
        showToast(this@Main, R.string.success)
    }

    private fun processDisableAndEnableImmediately(freeze: Boolean) {
        val size = selectedPackages.size
        val pkgNameList = selectedPackages.toTypedArray()
        ServiceUtils.startService(
            this@Main,
            Intent(this@Main, FUFService::class.java)
                .putExtra("single", false)
                .putExtra("packages", pkgNameList)
                .putExtra("freeze", freeze)
        )
    }

    private fun processForceStopImmediately() {
        val size = selectedPackages.size
        val pkgNameList = selectedPackages.toTypedArray()
        ServiceUtils.startService(
            this@Main,
            Intent(this@Main, ForceStopService::class.java)
                .putExtra("packages", pkgNameList)
        )
    }

    private fun updateFrozenStatus() {
        val adapter =
            mMainActivityAppListFragment?.getAppListAdapter() as? MainAppListSimpleAdapter ?: return
        val pm = packageManager
        val count = adapter.count
        for (i in 0 until count) {
            val hm = adapter.getStoredArrayList().getOrNull(i) ?: continue
            val pkgName = hm["PackageName"] as? String ?: continue
            val applicationInfo = getApplicationInfoFromPkgName(pkgName, this)

            //检查是否已卸载
            if (applicationInfo == null) {
                hm["Name"] = getString(R.string.uninstalled)
                break
            }

            //更新冻结状态信息
            if (hm["isFrozen"] as? Int != getFrozenStatus(pkgName, pm)) {
                //更新冻结状态点
                processFrozenStatus(hm, pkgName, pm)

                //更新图标
                if (isGridMode) {
                    hm["Img"] =
                        if (realGetFrozenStatus(this, pkgName, pm)) BitmapDrawable(
                            resources,
                            getGrayBitmap(
                                getBitmapFromDrawable(
                                    getApplicationIcon(
                                        this,
                                        pkgName,
                                        applicationInfo,
                                        false
                                    )
                                )
                            )
                        ) else getApplicationIcon(
                            this@Main,
                            pkgName,
                            applicationInfo,
                            true
                        )
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveOnClickFunctionStatus(status: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (sharedPreferences.getBoolean(
                saveOnClickFunctionStatus.name, saveOnClickFunctionStatus.defaultValue()
            )
        ) {
            sharedPreferences.edit().putInt("onClickFunctionStatus", status).apply()
        }
    }

    private fun saveSortMethodStatus(status: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (sharedPreferences.getBoolean(saveSortMethodStatus.name, saveSortMethodStatus.defaultValue())) {
            sharedPreferences.edit().putInt("sortMethodStatus", status).apply()
        }
    }

    private fun checkLongTimeNotUpdated() {
        Thread {
            try {
                val sharedPreferences = getSharedPreferences("Ver", MODE_PRIVATE)
                if (sharedPreferences.getInt("Ver", 0) < getVersionCode(applicationContext)) {
                    sharedPreferences.edit()
                        .putInt("Ver", getVersionCode(applicationContext))
                        .putLong("Time", Date().time)
                        .apply()
                }
                if (isOutdated(sharedPreferences)) {
                    if (isFinishing) return@Thread
                    runOnUiThread {
                        AlertDialogUtils.buildAlertDialog(
                            this@Main,
                            R.mipmap.ic_launcher_new_round,
                            R.string.notUpdatedForALongTimeMessage,
                            R.string.notice
                        )
                            .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                                checkUpdate(this@Main)
                            }
                            .setNegativeButton(R.string.no, null)
                            .setNeutralButton(R.string.later) { _: DialogInterface?, _: Int ->
                                sharedPreferences.edit()
                                    .putLong("Time", Date().time)
                                    .apply()
                            }
                            .create().show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun createFUFShortcutsBatch() { //小于 Android 8.0 适用
        val sps = selectedPackages.size
        var pkgName: String
        for (i in 0 until sps) {
            pkgName = selectedPackages[i]
            createShortCut(
                getApplicationLabel(this@Main, null, null, pkgName),
                pkgName,
                getApplicationIcon(
                    this@Main,
                    pkgName,
                    getApplicationInfoFromPkgName(pkgName, this@Main),
                    false
                ),
                Freeze::class.java,
                "FreezeYou! $pkgName",
                this@Main
            )
        }
    }

    private fun setSortByDefault(AppList: ArrayList<MutableMap<String, Any?>>) {
        AppList.sortWith { stringObjectMap: MutableMap<String, Any?>, t1: MutableMap<String, Any?> ->
            (stringObjectMap["PackageName"] as String).compareTo(
                (t1["PackageName"] as String)
            )
        }
    }

    /**
     * Compare two apps by a times map (usage / freeze / unfreeze counts).
     * Ascending: smaller counts first; missing or zero counts sort first among sparse entries
     * matching the previous Java behavior.
     */
    private fun compareByTimesMap(
        m0: Map<String, Any?>,
        m1: Map<String, Any?>,
        timesMap: Map<String, Int>,
        descending: Boolean
    ): Int {
        val s0 = Base64.encodeToString(
            (m0["PackageName"] as String).toByteArray(),
            Base64.DEFAULT
        )
        val s1 = Base64.encodeToString(
            (m1["PackageName"] as String).toByteArray(),
            Base64.DEFAULT
        )
        val t0 = timesMap[s0]
        val t1 = timesMap[s1]
        return when {
            t0 != null && t1 != null ->
                if (descending) t1.compareTo(t0) else t0.compareTo(t1)
            t0 != null && t0 > 0 -> if (descending) -1 else 1
            t1 != null && t1 > 0 -> if (descending) 1 else -1
            else -> 0
        }
    }

    private fun shortcutIconDrawable(resId: Int): Drawable {
        return ContextCompat.getDrawable(this, resId)
            ?: resources.getDrawable(resId, theme)
    }

    private fun checkIfNeedAskFirstTimeSetupAndShowDialog() {
        if (getSharedPreferences("Ver", MODE_PRIVATE).getInt("Ver", 0) != 0) {
            go()
            return
        }
        val builder = FreezeYouAlertDialogBuilder(this)
        builder.setIcon(R.mipmap.ic_launcher_new_round)
        builder.setTitle(
            String.format(
                getString(R.string.welcomeToUseAppName),
                getString(R.string.app_name)
            )
        )
        builder.setMessage(
            String.format(
                getString(R.string.welcomeToUseAppName),
                getString(R.string.app_name)
            )
        )
        builder.setPositiveButton(R.string.quickSetup) { _: DialogInterface?, _: Int ->
            startActivity(
                Intent(applicationContext, FirstTimeSetupActivity::class.java)
            )
            go()
        }
        builder.setNegativeButton(R.string.importConfig) { _: DialogInterface?, _: Int ->
            startActivity(
                Intent(applicationContext, BackupMainActivity::class.java)
            )
            go()
        }
        builder.setNeutralButton(R.string.okay) { _: DialogInterface?, _: Int -> go() }
        builder.setOnCancelListener { go() }
        builder.show()
    }

    private fun getUFreezeTimesMap(): HashMap<String, Int> {
        val db = openOrCreateDatabase("ApplicationsUFreezeTimes", MODE_PRIVATE, null)
        val hashMap = getTimesMap(db)
        db.close()
        return hashMap
    }

    private fun getFreezeTimesMap(): HashMap<String, Int> {
        val db = openOrCreateDatabase("ApplicationsFreezeTimes", MODE_PRIVATE, null)
        val hashMap = getTimesMap(db)
        db.close()
        return hashMap
    }

    private fun getUseTimesMap(): HashMap<String, Int> {
        val db = openOrCreateDatabase("ApplicationsUseTimes", MODE_PRIVATE, null)
        val hashMap = getTimesMap(db)
        db.close()
        return hashMap
    }

    private fun getTimesMap(db: SQLiteDatabase?): HashMap<String, Int> {
        val hashMap = HashMap<String, Int>()
        if (db == null) {
            return hashMap
        }
        db.execSQL(
            "create table if not exists TimesList(_id integer primary key autoincrement,pkg varchar,times int)"
        )
        val cursor = db.query("TimesList", arrayOf("pkg", "times"), null, null, null, null, null) ?: return hashMap
        if (cursor.moveToFirst()) {
            for (i in 0 until cursor.count) {
                hashMap[cursor.getString(cursor.getColumnIndexOrThrow("pkg"))] =
                    cursor.getString(cursor.getColumnIndexOrThrow("times")).toInt()
                cursor.moveToNext()
            }
        }
        cursor.close()
        return hashMap
    }

    private fun addUserDefinedCategoriesTo(
        subMenu: SubMenu,
        groupId: Int,
        newClassificationId: Int,
        idBase: Int
    ) {
        subMenu.clear() // 清空先前产生的数据
        subMenu.add(
            groupId,
            newClassificationId,
            0,
            R.string.newClassification
        ) // 加入“新建分类”

        // 添加用户定义的自定义分类
        val userDefinedDb = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null)
        userDefinedDb.execSQL(
            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        )
        val cursor = userDefinedDb.query("categories", arrayOf("label", "_id"), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            for (i in 0 until cursor.count) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("label"))
                subMenu.add(
                    groupId,
                    id + idBase,
                    id,
                    String(Base64.decode(title, Base64.DEFAULT))
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
        userDefinedDb.close()
    }

    companion object {
        private const val APPListViewOnClickMode_chooseAction = 0
        private const val APPListViewOnClickMode_autoUFOrFreeze = 1
        private const val APPListViewOnClickMode_freezeImmediately = 2
        private const val APPListViewOnClickMode_UFImmediately = 3
        private const val APPListViewOnClickMode_addToOFList = 4
        private const val APPListViewOnClickMode_removeFromOFList = 5
        private const val APPListViewOnClickMode_addToOUFList = 6
        private const val APPListViewOnClickMode_removeFromOUFList = 7
        private const val APPListViewOnClickMode_addToFOQList = 8
        private const val APPListViewOnClickMode_removeFromFOQList = 9
        private const val APPListViewOnClickMode_UFAndRun = 10
        private const val APPListViewOnClickMode_autoUFOrFreezeAndRun = 11
        private const val APPListViewOnClickMode_createFUFShortcut = 12
        private const val SORT_BY_DEFAULT = 0
        private const val SORT_BY_NO = 1
        private const val SORT_BY_UF_ASCENDING = 2
        private const val SORT_BY_UF_DESCENDING = 3
        private const val SORT_BY_FF_ASCENDING = 4
        private const val SORT_BY_FF_DESCENDING = 5
        private const val SORT_BY_US_ASCENDING = 6
        private const val SORT_BY_US_DESCENDING = 7
        private const val SORT_BY_ALPHABETICAL = 8
        private const val SORT_BY_LAST_INSTALLED = 9
        private const val SORT_BY_LAST_UPDATED = 10

        private fun addNotAvailablePair(context: Context, AppList: MutableList<MutableMap<String, Any?>>) {
            val keyValuePair: MutableMap<String, Any?> = HashMap()
            keyValuePair["Img"] = android.R.drawable.sym_def_app_icon
            keyValuePair["Name"] = context.getString(R.string.notAvailable)
            keyValuePair["PackageName"] = context.getString(R.string.notAvailable)
            keyValuePair["InstallTime"] = 0L
            keyValuePair["UpdateTime"] = 0L
            AppList.add(keyValuePair)
        }
    }
}
