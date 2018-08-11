package cf.playhi.freezeyou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.grandcentrix.tray.AppPreferences;

public class NotificationDeletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppPreferences appPreferences = new AppPreferences(context);
        String notifying = appPreferences.getString("notifying", "");
        String pkgName = intent.getStringExtra("pkgName");
        if (null != pkgName && !"".equals(pkgName) && notifying != null)
            appPreferences.put("notifying", notifying.replace(pkgName + ",", ""));
    }
}
