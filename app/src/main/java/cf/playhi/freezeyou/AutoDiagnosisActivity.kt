package cf.playhi.freezeyou

import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.ThemeUtils.processActionBar
import cf.playhi.freezeyou.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.AccessibilityUtils.isAccessibilitySettingsOn
import cf.playhi.freezeyou.utils.AccessibilityUtils.openAccessibilitySettings
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils
import cf.playhi.freezeyou.utils.FileUtils.clearIconCache
import cf.playhi.freezeyou.utils.NotificationUtils.startAppNotificationSettingsSystemActivity
import cf.playhi.freezeyou.utils.ProcessUtils.destroyProcess
import cf.playhi.freezeyou.utils.VersionUtils.checkUpdate
import cf.playhi.freezeyou.utils.VersionUtils.isOutdated
import net.grandcentrix.tray.AppPreferences
import java.io.DataOutputStream

class AutoDiagnosisActivity : FreezeYouBaseActivity() {
    private var mDiagnosisThread: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autodiagnosis)
        processActionBar(supportActionBar)
    }

    override fun onResume() {
        super.onResume()
        if (mDiagnosisThread != null && mDiagnosisThread!!.isAlive) {
            mDiagnosisThread!!.interrupt()
        }
        mDiagnosisThread = Thread { go() }
        mDiagnosisThread?.start()
    }

    override fun onPause() {
        super.onPause()
        if (mDiagnosisThread != null && mDiagnosisThread!!.isAlive) {
            mDiagnosisThread!!.interrupt()
        }
    }

    private fun go() {
        val listView = findViewById<ListView>(R.id.adg_listView)
        val progressBar = findViewById<ProgressBar>(R.id.adg_progressBar)
        val problemsList: MutableList<Map<String, Any>> = ArrayList()
        val appPreferences = AppPreferences(this)
        disableIndeterminate(progressBar)
        setProgress(progressBar, 5)
        checkSystemVersion(problemsList)
        setProgress(progressBar, 10)
        checkLongTimeNoUpdate(problemsList)
        setProgress(progressBar, 15)
        checkAccessibilityService(problemsList, appPreferences)
        setProgress(progressBar, 20)
        checkNotificationListenerPermission(problemsList, appPreferences)
        setProgress(progressBar, 25)
        checkNotifyPermission(problemsList)
        setProgress(progressBar, 30)
        checkIsDeviceOwner(problemsList)
        setProgress(progressBar, 35)
        checkRootPermission(problemsList)
        setProgress(progressBar, 40)
        doRegenerateSomeCache(problemsList, progressBar)
        setProgress(progressBar, 90)
        checkIsPowerSaveMode(problemsList)
        setProgress(progressBar, 95)
        checkIsIgnoringBatteryOptimizations(problemsList)
        setProgress(progressBar, 97)
        checkIfNoProblemFound(problemsList)
        setProgress(progressBar, 98)
        problemsList.sortWith { t0: Map<String, Any>, t1: Map<String, Any> ->
            val i = (t0["status"] as Int).compareTo(
                (t1["status"] as Int)
            )
            if (i == 0) (t0["id"] as String).compareTo((t1["id"] as String)) else i
        }
        val adapter = SimpleAdapter(
            this,
            problemsList,
            R.layout.adg_list_item,
            arrayOf("title", "sTitle", "status", "id"),
            intArrayOf(
                R.id.adgli_title_textView,
                R.id.adgli_subTitle_textView,
                R.id.adgli_status_imageView
            )
        )
        listView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val s = problemsList[position]["id"] as String?
                if (s != null) {
                    when (s) {
                        "-30" -> checkUpdate(this@AutoDiagnosisActivity)
                        "1" -> openAccessibilitySettings(this@AutoDiagnosisActivity)
                        "2" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        "4" -> if (Build.VERSION.SDK_INT >= 23) {
                            val intent =
                                if ((getSystemService(POWER_SERVICE) as PowerManager)
                                        .isIgnoringBatteryOptimizations(packageName)
                                ) {
                                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                } else {
                                    Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:$packageName")
                                    )
                                }

                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        "6" -> startAppNotificationSettingsSystemActivity(
                            this@AutoDiagnosisActivity,
                            "cf.play" + "hi.free" + "zeyou",
                            applicationInfo.uid
                        )
                    }
                }
            }
        setProgress(progressBar, 100)
        done(progressBar, listView, adapter)
    }

    private fun disableIndeterminate(progressBar: ProgressBar) {
        runOnUiThread { progressBar.isIndeterminate = false }
    }

    private fun done(progressBar: ProgressBar, listView: ListView, adapter: SimpleAdapter) {
        runOnUiThread {
            listView.adapter = adapter
            progressBar.visibility = View.GONE
        }
    }

    private fun setProgress(progressBar: ProgressBar, progress: Int) {
        runOnUiThread {
            if (Build.VERSION.SDK_INT >= 24) {
                progressBar.setProgress(progress, true)
            } else {
                progressBar.progress = progress
            }
        }
    }

    private fun generateHashMap(
        title: String,
        sTitle: String,
        id: String,
        statusId: Int
    ): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = title
        hashMap["sTitle"] = sTitle
        hashMap["id"] = id
        hashMap["status"] = statusId
        return hashMap
    }

    private fun checkSystemVersion(problemsList: MutableList<Map<String, Any>>) {
        if (Build.VERSION.SDK_INT < 21) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.sysVerLow),
                    getString(R.string.someFuncUn),
                    "-50",
                    R.drawable.ic_attention
                )
            )
        } else {
            problemsList.add(
                generateHashMap(
                    getString(R.string.sysVerLow),
                    getString(R.string.someFuncUn),
                    "-1",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkAccessibilityService(
        problemsList: MutableList<Map<String, Any>>,
        appPreferences: AppPreferences
    ) {
        problemsList.add(
            if ((getDatabasePath("scheduledTriggerTasks").exists()
                        || appPreferences.getBoolean(
                    "freezeOnceQuit", false
                ) || appPreferences.getBoolean(
                    "avoidFreezeForegroundApplications",
                    false
                ))
                && !isAccessibilitySettingsOn(this)
            ) {
                generateHashMap(
                    getString(R.string.ACBSNotEnabled),
                    getString(R.string.affect) + " " + getString(R.string.avoidFreezeForegroundApplications) + " " + getString(
                        R.string.scheduledTasks
                    ) + " " + getString(R.string.etc),
                    "1",
                    R.drawable.ic_attention
                )
            } else {
                generateHashMap(
                    getString(R.string.ACBSNotEnabled),
                    getString(R.string.affect) + " " + getString(R.string.avoidFreezeForegroundApplications) + " " + getString(
                        R.string.scheduledTasks
                    ) + " " + getString(R.string.etc),
                    "1",
                    R.drawable.ic_done
                )
            }
        )
    }

    private fun checkNotificationListenerPermission(
        problemsList: MutableList<Map<String, Any>>,
        appPreferences: AppPreferences
    ) {
        if (Build.VERSION.SDK_INT >= 21
            && appPreferences.getBoolean(
                "avoidFreezeNotifyingApplications", false
            )
        ) {
            val s = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            problemsList.add(
                if (s == null
                    || !s.contains("cf.playhi.freezeyou/cf.playhi.freezeyou.MyNotificationListenerService")
                ) {
                    generateHashMap(
                        getString(R.string.noNotificationListenerPermission),
                        getString(R.string.affect) + " " + getString(R.string.avoidFreezeNotifyingApplications),
                        "2",
                        R.drawable.ic_attention
                    )
                } else {
                    generateHashMap(
                        getString(R.string.noNotificationListenerPermission),
                        getString(R.string.affect) + " " + getString(R.string.avoidFreezeNotifyingApplications),
                        "2",
                        R.drawable.ic_done
                    )
                }
            )
        }
    }

    private fun checkNotifyPermission(problemsList: MutableList<Map<String, Any>>) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 24 && !notificationManager.areNotificationsEnabled()) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noNotifyPermission),
                    getString(R.string.mayCannotNotify),
                    "6",
                    R.drawable.ic_attention
                )
            )
        } else {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noNotifyPermission),
                    getString(R.string.mayCannotNotify),
                    "6",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkIsDeviceOwner(problemsList: MutableList<Map<String, Any>>) {
        if (!DevicePolicyManagerUtils.isDeviceOwner(this)) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noMRootPermission),
                    getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_attention
                )
            )
        } else {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noMRootPermission),
                    getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkLongTimeNoUpdate(problemsList: MutableList<Map<String, Any>>) {
        if (isOutdated(applicationContext)) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.notUpdatedForALongTime),
                    getString(R.string.someNewFuncMayPub),
                    "-30",
                    R.drawable.ic_attention
                )
            )
        } else {
            problemsList.add(
                generateHashMap(
                    getString(R.string.notUpdatedForALongTime),
                    getString(R.string.someNewFuncMayPub),
                    "-30",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkRootPermission(problemsList: MutableList<Map<String, Any>>) {
        var hasPermission = true
        var value = -1
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            value = process.waitFor()
            destroyProcess(outputStream, process)
        } catch (e: Exception) {
            if (e.message!!.toLowerCase().contains("permission denied")
                || e.message!!.toLowerCase().contains("not found")
            ) {
                hasPermission = false
            }
        }
        if (!hasPermission || value != 0) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noRootPermission),
                    getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_attention
                )
            )
        } else {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noRootPermission),
                    getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkIsIgnoringBatteryOptimizations(problemsList: MutableList<Map<String, Any>>) {
        if (Build.VERSION.SDK_INT >= 23
            && !(getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
                "cf.playhi.freezeyou"
            )
        ) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noIgnoringBO),
                    getString(R.string.someFuncMayBeAff),
                    "4",
                    R.drawable.ic_attention
                )
            )
        } else {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noIgnoringBO),
                    getString(R.string.someFuncMayBeAff),
                    "4",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkIsPowerSaveMode(problemsList: MutableList<Map<String, Any>>) {
        if (Build.VERSION.SDK_INT >= 21
            && (getSystemService(POWER_SERVICE) as PowerManager).isPowerSaveMode
        ) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.inPowerSaveMode),
                    getString(R.string.someFuncMayBeAff),
                    "5",
                    R.drawable.ic_attention
                )
            )
        } else {
            problemsList.add(
                generateHashMap(
                    getString(R.string.inPowerSaveMode),
                    getString(R.string.someFuncMayBeAff),
                    "5",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun doRegenerateSomeCache(
        problemsList: MutableList<Map<String, Any>>, progressBar: ProgressBar
    ) {
        getSharedPreferences("NameOfPackages", MODE_PRIVATE).edit().clear().apply()
        clearIconCache(applicationContext)
        val cacheApplicationsIcons = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        ).getBoolean("cacheApplicationsIcons", false)
        val pm = packageManager
        val installedApplications =
            pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)
        if (installedApplications != null) {
            val size = installedApplications.size
            for (i in 0 until size) {
                val applicationInfo = installedApplications[i]
                getApplicationLabel(
                    this,
                    pm,
                    applicationInfo,
                    applicationInfo.packageName
                )
                if (cacheApplicationsIcons) {
                    getApplicationIcon(
                        this, applicationInfo.packageName,
                        applicationInfo, false, true
                    )
                }
                setProgress(progressBar, 40 + (i.toDouble() / size.toDouble() * 50).toInt())
            }
        }
        problemsList.add(
            generateHashMap(
                getString(R.string.regenerateSomeCache),
                getString(R.string.updateSomeData), "10", R.drawable.ic_done
            )
        )
    }

    private fun checkIfNoProblemFound(problemsList: MutableList<Map<String, Any>>) {
        if (problemsList.isEmpty()) {
            problemsList.add(
                generateHashMap(
                    getString(R.string.noProblemsFound),
                    getString(R.string.everySeemsAllRight),
                    "-99",
                    R.drawable.ic_done
                )
            )
        }
    }
}