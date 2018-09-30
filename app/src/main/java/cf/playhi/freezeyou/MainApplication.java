package cf.playhi.freezeyou;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import net.grandcentrix.tray.AppPreferences;

import java.io.File;

import static cf.playhi.freezeyou.Support.addToOneKeyList;

public class MainApplication extends Application {

    private static String mCurrentPackage = " ";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init();
//        checkLanguage(getApplicationContext());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (new AppPreferences(this).getBoolean("onekeyFreezeWhenLockScreen", false)) {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(new Intent(this, ScreenLockOneKeyFreezeService.class));
            } else {
                startService(new Intent(this, ScreenLockOneKeyFreezeService.class));
            }
        }
    }

    static void setCurrentPackage(String pkgName) {
        if (pkgName != null)
            mCurrentPackage = pkgName;
    }

    static String getCurrentPackage() {
        return mCurrentPackage;
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
                        addToOneKeyList(this, new_key_name, tmp);
                }
                oldFile.delete();
            }
        }
    }
}
