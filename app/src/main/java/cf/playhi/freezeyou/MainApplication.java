package cf.playhi.freezeyou;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import java.io.File;

import cyanogenmod.app.CMStatusBarManager;
import cyanogenmod.app.CustomTile;

import static cf.playhi.freezeyou.Support.addToOneKeyList;

public class MainApplication extends Application {

    private static String mCurrentPackage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onekeyFreezeWhenLockScreen", false)) {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(new Intent(this, ScreenLockOneKeyFreezeService.class));
            } else {
                startService(new Intent(this, ScreenLockOneKeyFreezeService.class));
            }
        }
        try {
            File checkFile = new File(getFilesDir().getAbsolutePath() + File.separator + "20180808");
            if (!checkFile.exists()) {
                String absoluteFilesPath = getFilesDir().getAbsolutePath();
                String shared_prefsPath = absoluteFilesPath.substring(0, absoluteFilesPath.length() - 5) + "shared_prefs" + File.separator;
                File autoFreezeApplicationList = new File(shared_prefsPath + "AutoFreezeApplicationList.xml");
                if (autoFreezeApplicationList.exists() && autoFreezeApplicationList.isFile()) {
                    String[] autoFreezeApplicationPkgNames = this.getSharedPreferences(
                            "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName", "").split("\\|\\|");
                    for (String aPkgNameList : autoFreezeApplicationPkgNames) {
                        String tmp = aPkgNameList.replaceAll("\\|", "");
                        if (!"".equals(tmp))
                            addToOneKeyList(this, getString(R.string.sAutoFreezeApplicationList), tmp);
                    }
                    autoFreezeApplicationList.delete();
                }
                File oneKeyUFApplicationList = new File(shared_prefsPath + "OneKeyUFApplicationList.xml");
                if (oneKeyUFApplicationList.exists() && oneKeyUFApplicationList.isFile()) {
                    String[] autoUFApplicationPkgNames = getApplicationContext().getSharedPreferences(
                            "OneKeyUFApplicationList", Context.MODE_PRIVATE).getString("pkgName", "").split("\\|\\|");
                    for (String aPkgNameList : autoUFApplicationPkgNames) {
                        String tmp = aPkgNameList.replaceAll("\\|", "");
                        if (!"".equals(tmp))
                            addToOneKeyList(this, getString(R.string.sOneKeyUFApplicationList), tmp);
                    }
                    oneKeyUFApplicationList.delete();
                }
                File freezeOnceQuit = new File(shared_prefsPath + "FreezeOnceQuit.xml");
                if (freezeOnceQuit.exists() && freezeOnceQuit.isFile()) {
                    String[] freezeOnceQuitPkgNames = this.getSharedPreferences(
                            "FreezeOnceQuit", Context.MODE_PRIVATE).getString("pkgName", "").split("\\|\\|");
                    for (String aPkgNameList : freezeOnceQuitPkgNames) {
                        String tmp = aPkgNameList.replaceAll("\\|", "");
                        if (!"".equals(tmp))
                            addToOneKeyList(this, getString(R.string.sFreezeOnceQuit), tmp);
                    }
                    freezeOnceQuit.delete();
                }
                checkFile.createNewFile();
            }
            File importTrayLock = new File(getFilesDir().getAbsolutePath() + File.separator + "p2d.lock");
            if (!importTrayLock.exists()) {
                new ImportTrayPreferences(this);
                createCMTiles("cf.playhi.freezeyou.customtiles.okf.ACTION_TOGGLE_STATE", 1, R.string.oneKeyFreeze);
                createCMTiles("cf.playhi.freezeyou.customtiles.okuf.ACTION_TOGGLE_STATE", 2, R.string.oneKeyUF);
                importTrayLock.createNewFile();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void setCurrentPackage(String pkgName) {
        mCurrentPackage = pkgName;
    }

    static String getCurrentPackage() {
        return mCurrentPackage;
    }

    private void createCMTiles(String action, int id, int labelRes) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("state", 1);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, id,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
        CustomTile mCustomTile = new CustomTile.Builder(this)
                .setOnClickIntent(pendingIntent)
                .setContentDescription(labelRes)
                .setLabel(labelRes)
                .shouldCollapsePanel(false)
                .setIcon(R.drawable.ic_notification)
                .build();
        CMStatusBarManager.getInstance(this)
                .publishTile(id, mCustomTile);
    }
}
