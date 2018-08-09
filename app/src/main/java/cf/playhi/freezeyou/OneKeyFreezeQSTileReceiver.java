package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class OneKeyFreezeQSTileReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("cf.playhi.freezeyou.customtiles.okf.ACTION_TOGGLE_STATE".equals(intent.getAction())){
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(
                        new Intent(context, OneKeyFreezeService.class));
            } else {
                context.startService(
                        new Intent(context, OneKeyFreezeService.class));
            }
        }
    }
}
