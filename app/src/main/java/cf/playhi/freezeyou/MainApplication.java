package cf.playhi.freezeyou;

import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;

import cf.playhi.freezeyou.service.ScreenLockOneKeyFreezeService;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onekeyFreezeWhenLockScreen",false)){
            startService(new Intent(this, ScreenLockOneKeyFreezeService.class));
        }
    }

}
