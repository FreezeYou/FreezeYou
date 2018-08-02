package cf.playhi.freezeyou;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import cf.playhi.freezeyou.service.ScreenLockOneKeyFreezeService;

public class MainApplication extends Application {

    private static String mCurrentPackage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onekeyFreezeWhenLockScreen",false)){
            if (Build.VERSION.SDK_INT>=26){
                startForegroundService(new Intent(this, ScreenLockOneKeyFreezeService.class));
            } else {
                startService(new Intent(this, ScreenLockOneKeyFreezeService.class));
            }
        }
    }

    static void setCurrentPackage(String pkgName){
        mCurrentPackage = pkgName;
        Log.e("!",mCurrentPackage);
    }

    static String getCurrentPackage(){
        Log.e("!!",mCurrentPackage);
        return mCurrentPackage;
    }
}
