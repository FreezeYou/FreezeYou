package cf.playhi.freezeyou

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.service.ScreenLockOneKeyFreezeService
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.languagePref
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.selectFUFMode
import cf.playhi.freezeyou.storage.mmkv.DefaultMultiProcessMMKVStorage
import cf.playhi.freezeyou.utils.FUFUtils.checkAndEnableShizukuMultiProcessSupport
import cf.playhi.freezeyou.utils.OneKeyListUtils
import cf.playhi.freezeyou.utils.ServiceUtils
import com.getkeepsafe.relinker.ReLinker
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV
import net.grandcentrix.tray.AppPreferences
import java.io.File
import java.io.IOException

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler().init(this)

        // Initialize MMKV,
        // use [ReLinker](https://github.com/KeepSafe/ReLinker)
        // to avoid errors like `java.lang.UnsatisfiedLinkError`.
        MMKV.initialize(this) { libName: String? ->
            ReLinker.loadLibrary(
                this@MainApplication,
                libName
            )
        }
        try {
            checkAndMigrateOneKeyConfig()
            checkAndMigrateSharedPreferenceDataToTray()
            checkAndMigrateAppIconDataPreference()
            checkAndMigrateEnableAuthenticationPreferenceDataToMMKV()
            checkAndMigrateMultiProcessPreferenceDataToMMKV()
            checkAndMigrateKeySAFUFAndNCWFUSAFUFAndOIAUUSAFUF()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        checkAndEnableShizukuMultiProcessSupport(this)
        checkAndStartScreenLockOneKeyFreezeService()
    }

    private fun migrateOneKeyConfig() {
        val absoluteFilesPath = filesDir.absolutePath
        val sharedPrefsPath = (absoluteFilesPath.substring(0, absoluteFilesPath.length - 5)
                + "shared_prefs" + File.separator)
        migrateOneKeyData(
            File(sharedPrefsPath + "AutoFreezeApplicationList.xml"),
            "AutoFreezeApplicationList",
            getString(R.string.sAutoFreezeApplicationList)
        )
        migrateOneKeyData(
            File(sharedPrefsPath + "OneKeyUFApplicationList.xml"),
            "OneKeyUFApplicationList",
            getString(R.string.sOneKeyUFApplicationList)
        )
        migrateOneKeyData(
            File(sharedPrefsPath + "FreezeOnceQuit.xml"),
            "FreezeOnceQuit",
            getString(R.string.sFreezeOnceQuit)
        )
    }

    private fun migrateOneKeyData(
        oldFile: File,
        old_shared_prefs_name: String,
        new_key_name: String
    ) {
        if (oldFile.exists() && oldFile.isFile) {
            val pkgNameS = applicationContext.getSharedPreferences(
                old_shared_prefs_name, MODE_PRIVATE
            ).getString("pkgName", "")
            if (pkgNameS != null) {
                val pkgNames = pkgNameS.split("\\|\\|".toRegex()).toTypedArray()
                for (aPkgNameList in pkgNames) {
                    val tmp = aPkgNameList.replace("\\|".toRegex(), "")
                    if ("" != tmp) OneKeyListUtils.addToOneKeyList(this, new_key_name, tmp)
                }
                oldFile.delete()
            }
        }
    }

    @Throws(IOException::class)
    private fun checkAndMigrateOneKeyConfig() {
        val checkFile = File(filesDir.absolutePath + File.separator + "20180808")
        if (!checkFile.exists()) {
            migrateOneKeyConfig()
            checkFile.createNewFile()
        }
    }

    private fun checkAndStartScreenLockOneKeyFreezeService() {
        if (AppPreferences(this)
                .getBoolean("onekeyFreezeWhenLockScreen", false)
        ) {
            ServiceUtils.startService(
                this,
                Intent(this, ScreenLockOneKeyFreezeService::class.java)
            )
        }
    }

    @Throws(IOException::class)
    private fun checkAndMigrateAppIconDataPreference() {
        val appIconDataTransfer20181014 = File(
            filesDir.absolutePath
                    + File.separator + "appIconDataTransfer20181014.lock"
        )
        if (!appIconDataTransfer20181014.exists()) {
            val pm = packageManager
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val theCls = arrayOf(
                "cf.playhi.freezeyou.FirstIcon",
                "cf.playhi.freezeyou.SecondIcon",
                "cf.playhi.freezeyou.ThirdIcon"
            )
            val theAppIconPrefs =
                arrayOf("firstIconEnabled", "secondIconEnabled", "thirdIconEnabled")
            for (i in theCls.indices) {
                if (sharedPreferences.getBoolean(
                        theAppIconPrefs[i], theAppIconPrefs[2] == theAppIconPrefs[i]
                    )
                ) {
                    pm.setComponentEnabledSetting(
                        ComponentName(this, theCls[i]),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } else {
                    pm.setComponentEnabledSetting(
                        ComponentName(this, theCls[i]),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
            appIconDataTransfer20181014.createNewFile()
        }
    }

    @Throws(IOException::class)
    private fun checkAndMigrateSharedPreferenceDataToTray() {
        val importTrayLock = File(filesDir.absolutePath + File.separator + "p2d.lock")
        if (!importTrayLock.exists()) {
            ImportTrayPreferences(this)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val appPreferences = AppPreferences(this)
            appPreferences.put(
                "freezeOnceQuit",
                sharedPreferences.getBoolean("freezeOnceQuit", false)
            )
            appPreferences.put(
                "shortCutOneKeyFreezeAdditionalOptions",
                sharedPreferences.getString("shortCutOneKeyFreezeAdditionalOptions", "nothing")
            )
            appPreferences.put(
                "useForegroundService",
                sharedPreferences.getBoolean("useForegroundService", false)
            )
            appPreferences.put(
                "onekeyFreezeWhenLockScreen",
                sharedPreferences.getBoolean("onekeyFreezeWhenLockScreen", false)
            )
            importTrayLock.createNewFile()
        }
        val dataTransfer20180816Lock =
            File(filesDir.absolutePath + File.separator + "20180816.lock")
        if (!dataTransfer20180816Lock.exists()) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val appPreferences = AppPreferences(this)
            appPreferences.put(
                "notificationBarFreezeImmediately",
                sharedPreferences.getBoolean("notificationBarFreezeImmediately", true)
            )
            appPreferences.put(
                "openImmediately",
                sharedPreferences.getBoolean("openImmediately", false)
            )
            appPreferences.put(
                "openAndUFImmediately",
                sharedPreferences.getBoolean("openAndUFImmediately", false)
            )
            appPreferences.put(
                "notificationBarDisableSlideOut",
                sharedPreferences.getBoolean("notificationBarDisableSlideOut", false)
            )
            appPreferences.put(
                "notificationBarDisableClickDisappear",
                sharedPreferences.getBoolean("notificationBarDisableClickDisappear", true)
            )
            dataTransfer20180816Lock.createNewFile()
        }
    }

    @Throws(IOException::class)
    private fun checkAndMigrateEnableAuthenticationPreferenceDataToMMKV() {
        val migrateLock = File(
            filesDir.absolutePath + File.separator
                    + "migrateEnableAuthenticationPreferenceDataToMMKV.lock"
        )
        if (!migrateLock.exists()) {
            DefaultMultiProcessMMKVStorage()
                .putBoolean(
                    enableAuthentication.name,
                    AppPreferences(this)
                        .getBoolean(enableAuthentication.name, enableAuthentication.defaultValue())
                )
                .sync()
            migrateLock.createNewFile()
        }
    }

    @Throws(IOException::class)
    private fun checkAndMigrateMultiProcessPreferenceDataToMMKV() {
        checkAndMigratePreferenceDataFromSharedPreferenceToTrayForFurtherMigrateToMMKV()
        val migrateLock = File(
            filesDir.absolutePath + File.separator
                    + "migrateMultiProcessPreferenceDataToMMKV.lock"
        )
        if (!migrateLock.exists()) {
            val appPreference = AppPreferences(this)
            DefaultMultiProcessMMKVStorage().run {
                DefaultMultiProcessMMKVStorageBooleanKeys.values().forEach { key ->
                    if (key.name != enableAuthentication.name) {
                        putBoolean(
                            key.name,
                            appPreference
                                .getBoolean(key.name, key.defaultValue())
                        )
                    }
                }
                DefaultMultiProcessMMKVStorageStringKeys.values().forEach { key ->
                    putString(
                        key.name,
                        appPreference
                            .getString(key.name, key.defaultValue())
                    )
                }
                sync()
            }
            migrateLock.createNewFile()
        }
    }

    @Throws(IOException::class)
    private fun checkAndMigratePreferenceDataFromSharedPreferenceToTrayForFurtherMigrateToMMKV() {
        val migrateLock = File(
            filesDir.absolutePath + File.separator
                    + "migrateSomePreferenceDataFromSharedPreferenceToTrayForFurtherMigrateToMMKV.lock"
        )
        if (!migrateLock.exists()) {
            val sp = PreferenceManager.getDefaultSharedPreferences(this@MainApplication)
            AppPreferences(this).run {
                put(
                    languagePref.name,
                    sp.getString(languagePref.name, languagePref.defaultValue())
                )
                remove(selectFUFMode.name)
                put(
                    selectFUFMode.name,
                    sp.getString(selectFUFMode.name, selectFUFMode.defaultValue())
                )
                put(
                    tryDelApkAfterInstalled.name,
                    sp.getBoolean(
                        tryDelApkAfterInstalled.name,
                        tryDelApkAfterInstalled.defaultValue()
                    )
                )
            }
            migrateLock.createNewFile()
        }
    }

    @Throws(IOException::class)
    private fun checkAndMigrateKeySAFUFAndNCWFUSAFUFAndOIAUUSAFUF() {
        val migrateLock = File(
            filesDir.absolutePath + File.separator
                    + "migrateKeySAFUFAndNCWFUSAFUFAndOIAUUSAFUF.lock"
        )
        if (!migrateLock.exists()) {
            DefaultMultiProcessMMKVStorage().run {
                val three = arrayOf(
                    shortcutAutoFUF,
                    needConfirmWhenFreezeUseShortcutAutoFUF,
                    openImmediatelyAfterUnfreezeUseShortcutAutoFUF
                )
                three.forEach { key ->
                    putBoolean(
                        key.name,
                        PreferenceManager.getDefaultSharedPreferences(this@MainApplication)
                            .getBoolean(key.name, key.defaultValue())
                    )
                }
                sync()
            }
            migrateLock.createNewFile()
        }
    }

    companion object {
        private var mCurrentPackage = " "

        @JvmStatic
        var waitingForLeavingToInstallApplicationIntent: Intent? = null
            /**
             * @return Intent，可能为 null （无等待处理内容）
             */
            get() = field
            /**
             * @param intent 可空，使用后尽快置空
             */
            set(intent) {
                field = intent
            }

        @JvmStatic
        var currentPackage: String?
            @NonNull
            get() = mCurrentPackage
            set(pkgName) {
                if (pkgName != null) mCurrentPackage = pkgName
            }
    }
}
