package cf.playhi.freezeyou.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import cf.playhi.freezeyou.service.OneKeyFreezeService;
import cf.playhi.freezeyou.utils.ServiceUtils;

import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.onekeyFreezeWhenLockScreen;

public class ScreenLockListener {

    private final Context mContext;
    private final ScreenLockBroadcastReceiver mScreenLockReceiver;

    public ScreenLockListener(Context context) {
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
                        if (onekeyFreezeWhenLockScreen.getValue(null)) {
                            ServiceUtils.startService(context,
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

    public void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenLockReceiver, filter);
    }

    public void unregisterListener() {
        mContext.unregisterReceiver(mScreenLockReceiver);
    }
}
