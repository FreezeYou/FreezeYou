package cf.playhi.freezeyou

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import cf.playhi.freezeyou.app.FreezeYouBaseService
import cf.playhi.freezeyou.utils.FUFUtils.oneKeyAction
import net.grandcentrix.tray.AppPreferences

class OneKeyUFService : FreezeYouBaseService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "OneKeyUF", getString(R.string.oneKeyUF), NotificationManager.IMPORTANCE_NONE
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.createNotificationChannel(channel)
            val mBuilder = Notification.Builder(this, "OneKeyUF")
            mBuilder.setSmallIcon(R.drawable.ic_notification)
            mBuilder.setContentText(getString(R.string.oneKeyUF))
            startForeground(3, mBuilder.build())
        } else {
            startForeground(3, Notification())
        }
        val pref = AppPreferences(applicationContext)
        val pkgNames: String? = pref.getString(getString(R.string.sOneKeyUFApplicationList), "")
        if (pkgNames != null) {
            oneKeyAction(
                this, false,
                pkgNames.trim(',').split(",").toTypedArray(),
                pref.getInt("selectFUFMode", 0)
            )
            doFinish()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun doFinish() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationManager?.cancel(3)
        stopSelf()
    }
}