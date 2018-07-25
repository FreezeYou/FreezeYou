package cf.playhi.freezeyou.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;

import cf.playhi.freezeyou.OneKeyFreeze;

public class ScreenLockListener {

    private Context mContext;
    private ScreenLockBroadcastReceiver mScreenLockReceiver;

    public ScreenLockListener(Context context){
        mContext = context;
        mScreenLockReceiver = new ScreenLockBroadcastReceiver();
    }

    private class ScreenLockBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("onekeyFreezeWhenLockScreen", false)) {
                    context.startActivity(new Intent(context, OneKeyFreeze.class).putExtra("autoCheckAndLockScreen",false));
                }
            }
        }
    }

    public void registerListener(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenLockReceiver, filter);
    }

    public void unregisterListener(){
        mContext.unregisterReceiver(mScreenLockReceiver);
    }
}
