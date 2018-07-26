package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import cf.playhi.freezeyou.service.ScreenLockOneKeyFreezeService;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("onekeyFreezeWhenLockScreen",false)){
                if (Build.VERSION.SDK_INT>=26){
                    context.startForegroundService(new Intent(context, ScreenLockOneKeyFreezeService.class));
                } else {
                    context.startService(new Intent(context, ScreenLockOneKeyFreezeService.class));
                }
            }
        }
    }
}
