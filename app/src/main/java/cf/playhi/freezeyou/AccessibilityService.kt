package cf.playhi.freezeyou

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.freezeOnceQuit
import cf.playhi.freezeyou.utils.DataStatisticsUtils
import cf.playhi.freezeyou.utils.FUFUtils.processFreezeAction
import cf.playhi.freezeyou.utils.OneKeyListUtils
import cf.playhi.freezeyou.utils.OneKeyListUtils.existsInOneKeyList
import cf.playhi.freezeyou.utils.ServiceUtils
import cf.playhi.freezeyou.utils.Support.getLocalString
import cf.playhi.freezeyou.utils.TasksUtils
import cf.playhi.freezeyou.utils.TasksUtils.cancelAllUnexecutedDelayTasks
import java.util.*

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
class AccessibilityService : android.accessibilityservice.AccessibilityService() {
    @CallSuper
    override fun attachBaseContext(newBase: Context) {
        val locale = getLocalString(newBase)
        val configuration = Configuration()
        configuration.setLocale(
            if ("Default" == locale) Locale.getDefault() else Locale.forLanguageTag(locale)
        )
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        val type = accessibilityEvent.eventType
        when (type) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (!accessibilityEvent.isFullScreen) return
                val pkgName = accessibilityEvent.packageName ?: return
                val pkgNameString = pkgName.toString()
                addUpUseTimes(pkgNameString) //使用次数计数（增加）
                if ("" == pkgNameString || "android" == pkgNameString || "com.android.systemui" == pkgNameString || "com.android.packageinstaller" == pkgNameString // Grant permissions, etc.
                    || "miui.systemui.plugin" == pkgNameString // MIUI 系统界面组件：各类弹窗，声音助手（含音量条）、无线反向充电等
                ) {
                    return
                }
                val className = accessibilityEvent.className?.toString() ?: return
                if ("android.inputmethodservice.SoftInputWindow" == className
                    || "com.miui.misound.playervolume.MiuiVolumeDialogImpl\$CustomDialog" == className // MIUI 音质音效：声音助手 - 多应用媒体音调节浮窗
                ) {
                    return
                }
                val pm = getSystemService(POWER_SERVICE) as PowerManager? ?: return
                if (!pm.isInteractive) return
                val previousPkg = MainApplication.currentPackage
                MainApplication.currentPackage = pkgNameString
                if (pkgNameString != previousPkg
                    && freezeOnceQuit.getValue(null)
                    && existsInOneKeyList(
                        applicationContext,
                        getString(R.string.sFreezeOnceQuit),
                        previousPkg
                    )
                ) {
                    processFreezeAction(
                        applicationContext, previousPkg, null, null,
                        false, null, false
                    )
                }
                onLeaveApplications(previousPkg, pkgNameString) //检测+执行
                onApplicationsForeground(previousPkg, pkgNameString) //检测+执行
            }
            else -> {}
        }
    }

    override fun onInterrupt() {}
    private fun onApplicationsForeground(previousPkg: String?, pkgNameString: String) {
        if (pkgNameString != previousPkg && "cf.playhi.freezeyou" != previousPkg) {
            cancelAllUnexecutedDelayTasks(this, "OSA_$previousPkg") //撤销全部属于上一应用的未执行的打开应用时
            val db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null)
            db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
            )
            val cursor = db.query("tasks", null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                for (i in 0 until cursor.count) {
                    var tgExtra = cursor.getString(cursor.getColumnIndexOrThrow("tgextra"))
                    if (tgExtra == null) {
                        tgExtra = ""
                    }
                    val tg = cursor.getString(cursor.getColumnIndexOrThrow("tg"))
                    val enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabled"))
                    if (enabled == 1 && "onApplicationsForeground" == tg && ("" == tgExtra || listOf(
                            *OneKeyListUtils.decodeUserListsInPackageNames(
                                this,
                                tgExtra.split(",").toTypedArray()
                            )
                        ).contains(pkgNameString))
                    ) {
                        val task = cursor.getString(cursor.getColumnIndexOrThrow("task"))
                        if (task != null && "" != task) {
                            TasksUtils.runTask(
                                task.replace("[ppkgn]", previousPkg ?: "")
                                    .replace("[cpkgn]", pkgNameString),
                                this,
                                if ("" == tgExtra) null else "OSA_$pkgNameString"
                            )
                        }
                    }
                    cursor.moveToNext()
                }
            }
            cursor.close()
            db.close()
        }
    }

    private fun onLeaveApplications(previousPkg: String?, pkgNameString: String) {
        checkAndInstallWaitingForLeavingToInstallApplication(previousPkg)
        if (pkgNameString != previousPkg && "cf.playhi.freezeyou" != previousPkg) {
            cancelAllUnexecutedDelayTasks(this, "OLA_$pkgNameString") //撤销全部属于被打开应用的未执行的离开应用时
            val db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null)
            db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
            )
            val cursor = db.query("tasks", null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                for (i in 0 until cursor.count) {
                    val tg = cursor.getString(cursor.getColumnIndexOrThrow("tg"))
                    val enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabled"))
                    var tgExtra = cursor.getString(cursor.getColumnIndexOrThrow("tgextra"))
                    if (tgExtra == null) {
                        tgExtra = ""
                    }
                    if (enabled == 1 && "onLeaveApplications" == tg && ("" == tgExtra || listOf(
                            *OneKeyListUtils.decodeUserListsInPackageNames(
                                this,
                                tgExtra.split(",").toTypedArray()
                            )
                        ).contains(previousPkg))
                    ) {
                        val task = cursor.getString(cursor.getColumnIndexOrThrow("task"))
                        if (task != null && "" != task) {
                            TasksUtils.runTask(
                                task.replace("[ppkgn]", previousPkg ?: "")
                                    .replace("[cpkgn]", pkgNameString),
                                this,
                                if ("" == tgExtra) null else "OLA_$previousPkg"
                            )
                        }
                    }
                    cursor.moveToNext()
                }
            }
            cursor.close()
            db.close()
        }
    }

    private fun addUpUseTimes(currentPackage: String) {
        DataStatisticsUtils.addUseTimes(applicationContext, currentPackage)
    }

    private fun checkAndInstallWaitingForLeavingToInstallApplication(previousPkg: String?) {
        val intent = MainApplication.waitingForLeavingToInstallApplicationIntent ?: return  //无待处理
        val packageInfo = intent.getParcelableExtra<PackageInfo>("packageInfo")
        if (packageInfo != null && previousPkg == packageInfo.packageName) {
            ServiceUtils.startService(this, intent)
            MainApplication.waitingForLeavingToInstallApplicationIntent = null
        }
    }
}
