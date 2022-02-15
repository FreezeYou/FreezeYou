package cf.playhi.freezeyou.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import cf.playhi.freezeyou.ui.AskLockScreenActivity
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseService
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils
import cf.playhi.freezeyou.utils.FUFUtils.oneKeyAction
import net.grandcentrix.tray.AppPreferences

class OneKeyFreezeService : FreezeYouBaseService() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "OneKeyFreeze",
                getString(R.string.oneKeyFreeze),
                NotificationManager.IMPORTANCE_NONE
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.createNotificationChannel(channel)
            val mBuilder = Notification.Builder(this, "OneKeyFreeze")
            mBuilder.setSmallIcon(R.drawable.ic_notification)
            mBuilder.setContentText(getString(R.string.oneKeyFreeze))
            startForeground(2, mBuilder.build())
        } else {
            startForeground(2, Notification())
        }
        val auto = intent.getBooleanExtra("autoCheckAndLockScreen", true)
        val pref = AppPreferences(applicationContext)
        val pkgNames = pref.getString(getString(R.string.sAutoFreezeApplicationList), "")
        if (pkgNames != null) {
            oneKeyAction(
                this, true,
                pkgNames.trim(',').split(",").toTypedArray(),
                pref.getInt("selectFUFMode", 0)
            )
            checkAuto(auto, this)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun checkAndLockScreen(context: Context) {
        var options = AppPreferences(applicationContext).getString(
            "shortCutOneKeyFreezeAdditionalOptions",
            "nothing"
        )
        if (options == null) options = ""
        when (options) {
            "askLockScreen" -> {
                startActivity(
                    Intent(
                        applicationContext,
                        AskLockScreenActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                doFinish()
            }
            "lockScreenImmediately" -> {
                DevicePolicyManagerUtils.doLockScreen(context)
                doFinish()
            }
            "nothing" -> doFinish()
            else -> doFinish()
        }
    }

    private fun doFinish() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationManager?.cancel(2)
        stopSelf()
    }

    private fun checkAuto(auto: Boolean, context: Context) {
        if (auto) {
            checkAndLockScreen(context)
        } else {
            doFinish()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}