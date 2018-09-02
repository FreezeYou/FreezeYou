package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import net.grandcentrix.tray.AppPreferences;

class ScreenLockListener {

    private final Context mContext;
    private final ScreenLockBroadcastReceiver mScreenLockReceiver;

    ScreenLockListener(Context context) {
        mContext = context;
        mScreenLockReceiver = new ScreenLockBroadcastReceiver();
    }

    private class ScreenLockBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
                        if (new AppPreferences(context).getBoolean("onekeyFreezeWhenLockScreen", false)) {
                            Support.startService(context,
                                    new Intent(context, OneKeyFreezeService.class)
                                            .putExtra("autoCheckAndLockScreen", false)
                            );
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenLockReceiver, filter);
    }

    void unregisterListener() {
        mContext.unregisterReceiver(mScreenLockReceiver);
    }
}
