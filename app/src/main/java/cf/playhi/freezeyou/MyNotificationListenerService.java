package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
@TargetApi(21)
public class MyNotificationListenerService extends NotificationListenerService {

    private static StatusBarNotification[] statusBarNotifications = new StatusBarNotification[]{};
    private boolean mListenerConnected = false;

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        mListenerConnected = false;
        statusBarNotifications = new StatusBarNotification[]{};
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        mListenerConnected = true;
        statusBarNotifications = getActiveNotifications();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (mListenerConnected)
            statusBarNotifications = getActiveNotifications();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        if (mListenerConnected)
            statusBarNotifications = getActiveNotifications();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public static StatusBarNotification[] getStatusBarNotifications() {
        return statusBarNotifications;
    }
}
