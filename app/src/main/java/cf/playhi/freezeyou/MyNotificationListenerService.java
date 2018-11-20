package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

@TargetApi(21)
public class MyNotificationListenerService extends NotificationListenerService {

    static StatusBarNotification[] statusBarNotifications = new StatusBarNotification[]{};

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        statusBarNotifications = getActiveNotifications();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        statusBarNotifications = getActiveNotifications();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        statusBarNotifications = getActiveNotifications();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    static StatusBarNotification[] getStatusBarNotifications(){
        return statusBarNotifications;
    }
}
