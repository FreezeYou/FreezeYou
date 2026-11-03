package cf.playhi.freezeyou

import android.annotation.TargetApi
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
@TargetApi(21)
class MyNotificationListenerService : NotificationListenerService() {
    private var mListenerConnected = false
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        mListenerConnected = false
        statusBarNotifications = arrayOf()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        mListenerConnected = true
        snapshotActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (mListenerConnected) snapshotActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        if (mListenerConnected) snapshotActiveNotifications()
    }

    // API 23–27 can return null. The explicit nullable type keeps Kotlin from
    // inserting a null check that would kill the listener callback.
    private fun snapshotActiveNotifications() {
        val notifications: Array<StatusBarNotification>? = activeNotifications
        statusBarNotifications = notifications ?: emptyArray()
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    companion object {
        @JvmStatic
        var statusBarNotifications = arrayOf<StatusBarNotification>()
            private set
    }
}
