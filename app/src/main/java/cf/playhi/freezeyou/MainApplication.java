package cf.playhi.freezeyou;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.getkeepsafe.relinker.ReLinker;
import com.tencent.mmkv.MMKV;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;

import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils;
import cf.playhi.freezeyou.utils.OneKeyListUtils;
import cf.playhi.freezeyou.utils.ServiceUtils;
import cf.playhi.freezeyou.utils.Support;

public class MainApplication extends Application {

    private static String mCurrentPackage = " ";
    private static Intent mWaitingForLeavingToInstallApplicationIntent = null;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = new CrashHandler();
        crashHandler.init(this);
        Support.checkLanguage(getApplicationContext());

        // Initialize MMKV,
        // use [ReLinker](https://github.com/KeepSafe/ReLinker)
        // to avoid errors like `java.lang.UnsatisfiedLinkError`.
        MMKV.initialize(this, libName ->
                ReLinker.loadLibrary(MainApplication.this, libName));

        try {

            File checkFile = new File(getFilesDir().getAbsolutePath() + File.separator + "20180808");
            if (!checkFile.exists()) {
                updateConfig();
                checkFile.createNewFile();
            }

            File importTrayLock = new File(getFilesDir().getAbsolutePath() + File.separator + "p2d.lock");
            if (!importTrayLock.exists()) {
                new ImportTrayPreferences(this);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                AppPreferences appPreferences = new AppPreferences(this);
                appPreferences.put("freezeOnceQuit", sharedPreferences.getBoolean("freezeOnceQuit", false));
                appPreferences.put("shortCutOneKeyFreezeAdditionalOptions", sharedPreferences.getString("shortCutOneKeyFreezeAdditionalOptions", "nothing"));
                appPreferences.put("useForegroundService", sharedPreferences.getBoolean("useForegroundService", false));
                appPreferences.put("onekeyFreezeWhenLockScreen", sharedPreferences.getBoolean("onekeyFreezeWhenLockScreen", false));
                importTrayLock.createNewFile();
            }

            File dataTransfer20180816Lock = new File(getFilesDir().getAbsolutePath() + File.separator + "20180816.lock");
            if (!dataTransfer20180816Lock.exists()) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                AppPreferences appPreferences = new AppPreferences(this);
                appPreferences.put("notificationBarFreezeImmediately", sharedPreferences.getBoolean("notificationBarFreezeImmediately", true));
                appPreferences.put("openImmediately", sharedPreferences.getBoolean("openImmediately", false));
                appPreferences.put("openAndUFImmediately", sharedPreferences.getBoolean("openAndUFImmediately", false));
                appPreferences.put("notificationBarDisableSlideOut", sharedPreferences.getBoolean("notificationBarDisableSlideOut", false));
                appPreferences.put("notificationBarDisableClickDisappear", sharedPreferences.getBoolean("notificationBarDisableClickDisappear", false));
                dataTransfer20180816Lock.createNewFile();
            }

            File appIconDataTransfer20181014 = new File(getFilesDir().getAbsolutePath() + File.separator + "appIconDataTransfer20181014.lock");
            if (!appIconDataTransfer20181014.exists()) {
                PackageManager pm = getPackageManager();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String[] theCls = new String[]{"cf.playhi.freezeyou.FirstIcon", "cf.playhi.freezeyou.SecondIcon", "cf.playhi.freezeyou.ThirdIcon"};
                String[] theAppIconPrefs = new String[]{"firstIconEnabled", "secondIconEnabled", "thirdIconEnabled"};
                for (int i = 0; i < theCls.length; i++) {
                    if (sharedPreferences.getBoolean(theAppIconPrefs[i], "thirdIconEnabled".equals(theAppIconPrefs[i]))) {
                        pm.setComponentEnabledSetting(new ComponentName(this, theCls[i]),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    } else {
                        pm.setComponentEnabledSetting(new ComponentName(this, theCls[i]),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    }
                }
                appIconDataTransfer20181014.createNewFile();
            }

            File organizationName = new File(getFilesDir().getAbsolutePath() + File.separator + "organizationName.lock");
            if (!organizationName.exists()) {
                DevicePolicyManagerUtils.checkAndSetOrganizationName(this, getString(R.string.app_name));
                organizationName.createNewFile();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (new AppPreferences(this).getBoolean("onekeyFreezeWhenLockScreen", false)) {
            ServiceUtils.startService(this, new Intent(this, ScreenLockOneKeyFreezeService.class));
        }
    }

    public static void setCurrentPackage(String pkgName) {
        if (pkgName != null)
            mCurrentPackage = pkgName;
    }

    public static String getCurrentPackage() {
        return mCurrentPackage;
    }

    /**
     * @param intent 可空，使用后尽快置空
     */
    public static void setWaitingForLeavingToInstallApplicationIntent(Intent intent) {
        mWaitingForLeavingToInstallApplicationIntent = intent;
    }

    /**
     * @return Intent ，可能为 null （无等待处理内容）
     */
    public static Intent getWaitingForLeavingToInstallApplicationIntent() {
        return mWaitingForLeavingToInstallApplicationIntent;
    }

    private void updateConfig() {
        String absoluteFilesPath = getFilesDir().getAbsolutePath();
        String shared_prefsPath = absoluteFilesPath.substring(0, absoluteFilesPath.length() - 5) + "shared_prefs" + File.separator;
        updateOneKeyData(
                new File(shared_prefsPath + "AutoFreezeApplicationList.xml"),
                "AutoFreezeApplicationList",
                getString(R.string.sAutoFreezeApplicationList));
        updateOneKeyData(
                new File(shared_prefsPath + "OneKeyUFApplicationList.xml"),
                "OneKeyUFApplicationList",
                getString(R.string.sOneKeyUFApplicationList));
        updateOneKeyData(
                new File(shared_prefsPath + "FreezeOnceQuit.xml"),
                "FreezeOnceQuit",
                getString(R.string.sFreezeOnceQuit));
    }

    private void updateOneKeyData(File oldFile, String old_shared_prefs_name, String new_key_name) {
        if (oldFile.exists() && oldFile.isFile()) {
            String pkgNameS = getApplicationContext().getSharedPreferences(
                    old_shared_prefs_name, Context.MODE_PRIVATE).getString("pkgName", "");
            if (pkgNameS != null) {
                String[] pkgNames = pkgNameS.split("\\|\\|");
                for (String aPkgNameList : pkgNames) {
                    String tmp = aPkgNameList.replaceAll("\\|", "");
                    if (!"".equals(tmp))
                        OneKeyListUtils.addToOneKeyList(this, new_key_name, tmp);
                }
                oldFile.delete();
            }
        }
    }
}
