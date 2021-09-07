package cf.playhi.freezeyou

import android.app.Application
import android.app.NotificationManager
import android.content.Context.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.utils.AccessibilityUtils.isAccessibilitySettingsOn
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner
import cf.playhi.freezeyou.utils.FileUtils.clearIconCache
import cf.playhi.freezeyou.utils.ProcessUtils.destroyProcess
import cf.playhi.freezeyou.utils.VersionUtils.isOutdated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.io.DataOutputStream

class AutoDiagnosisViewModel(application: Application) : AndroidViewModel(application) {

    private val mutex = Mutex()
    private val loadingProgress: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = -1 }

    private val problemsList: MutableLiveData<MutableList<Map<String, Any>>> =
        MutableLiveData<MutableList<Map<String, Any>>>().apply { value = ArrayList() }

    fun getLoadingProgress(): LiveData<Int> {
        return loadingProgress
    }

    fun getProblemsList(): LiveData<MutableList<Map<String, Any>>> {
        return problemsList
    }

    fun refreshDiagnosisData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (mutex.tryLock("AutoDiagnosisViewModelRefreshingDiagnosisData")) {
                loadingProgress.postValue(-1)

                problemsList.value!!.clear()
                loadingProgress.postValue(5)

                checkSystemVersion()
                loadingProgress.postValue(10)

                checkLongTimeNoUpdate()
                loadingProgress.postValue(15)

                checkAccessibilityService()
                loadingProgress.postValue(20)

                checkNotificationListenerPermission()
                loadingProgress.postValue(25)

                checkNotifyPermission()
                loadingProgress.postValue(30)

                checkIsDeviceOwner()
                loadingProgress.postValue(35)

                checkRootPermission()
                loadingProgress.postValue(40)

                doRegenerateSomeCache()
                loadingProgress.postValue(90)

                checkIsPowerSaveMode()
                loadingProgress.postValue(95)

                checkIsIgnoringBatteryOptimizations()
                loadingProgress.postValue(97)

                checkIfNoProblemFound()
                loadingProgress.postValue(98)

                problemsList.value!!.sortWith { t0: Map<String, Any>, t1: Map<String, Any> ->
                    val i = (t0["level"] as Int).compareTo(
                        (t1["level"] as Int)
                    )
                    if (i == 0) (t0["id"] as String).compareTo((t1["id"] as String)) else i
                }

                loadingProgress.postValue(100)

                mutex.unlock("AutoDiagnosisViewModelRefreshingDiagnosisData")
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
        hashMap["level"] = if (statusId == R.drawable.ic_warning) 0 else 1
        return hashMap
    }

    private fun checkSystemVersion() {
        if (Build.VERSION.SDK_INT < 21) {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.sysVerLow),
                    getApplication<Application>().getString(R.string.someFuncUn),
                    "-50",
                    R.drawable.ic_warning
                )
            )
        } else {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.sysVerLow),
                    getApplication<Application>().getString(R.string.someFuncUn),
                    "-1",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkAccessibilityService() {
        problemsList.value!!.add(
            if (isAccessibilitySettingsOn(getApplication())) {
                generateHashMap(
                    getApplication<Application>().getString(R.string.ACBSNotEnabled),
                    getApplication<Application>().getString(R.string.affect) + " " + getApplication<Application>().getString(
                        R.string.avoidFreezeForegroundApplications
                    ) + " " + getApplication<Application>().getString(
                        R.string.scheduledTasks
                    ) + " " + getApplication<Application>().getString(R.string.etc),
                    "1",
                    R.drawable.ic_done
                )
            } else {
                generateHashMap(
                    getApplication<Application>().getString(R.string.ACBSNotEnabled),
                    getApplication<Application>().getString(R.string.affect) + " " + getApplication<Application>().getString(
                        R.string.avoidFreezeForegroundApplications
                    ) + " " + getApplication<Application>().getString(
                        R.string.scheduledTasks
                    ) + " " + getApplication<Application>().getString(R.string.etc),
                    "1",
                    R.drawable.ic_warning
                )
            }
        )
    }

    private fun checkNotificationListenerPermission() {
        if (Build.VERSION.SDK_INT >= 21) {
            val s = Settings.Secure.getString(
                getApplication<Application>().contentResolver,
                "enabled_notification_listeners"
            )
            problemsList.value!!.add(
                if (s == null
                    || !s.contains("cf.playhi.freezeyou/cf.playhi.freezeyou.MyNotificationListenerService")
                ) {
                    generateHashMap(
                        getApplication<Application>().getString(R.string.noNotificationListenerPermission),
                        getApplication<Application>().getString(R.string.affect) + " " + getApplication<Application>().getString(
                            R.string.avoidFreezeNotifyingApplications
                        ),
                        "2",
                        R.drawable.ic_warning
                    )
                } else {
                    generateHashMap(
                        getApplication<Application>().getString(R.string.noNotificationListenerPermission),
                        getApplication<Application>().getString(R.string.affect) + " " + getApplication<Application>().getString(
                            R.string.avoidFreezeNotifyingApplications
                        ),
                        "2",
                        R.drawable.ic_done
                    )
                }
            )
        }
    }

    private fun checkNotifyPermission() {
        val notificationManager =
            getApplication<Application>().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 24 && !notificationManager.areNotificationsEnabled()) {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noNotifyPermission),
                    getApplication<Application>().getString(R.string.mayCannotNotify),
                    "6",
                    R.drawable.ic_warning
                )
            )
        } else {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noNotifyPermission),
                    getApplication<Application>().getString(R.string.mayCannotNotify),
                    "6",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkIsDeviceOwner() {
        if (isDeviceOwner(getApplication())) {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noMRootPermission),
                    getApplication<Application>().getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_done
                )
            )
        } else {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noMRootPermission),
                    getApplication<Application>().getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_warning
                )
            )
        }
    }

    private fun checkLongTimeNoUpdate() {
        if (isOutdated(getApplication<Application>())) {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.notUpdatedForALongTime),
                    getApplication<Application>().getString(R.string.someNewFuncMayPub),
                    "-30",
                    R.drawable.ic_warning
                )
            )
        } else {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.notUpdatedForALongTime),
                    getApplication<Application>().getString(R.string.someNewFuncMayPub),
                    "-30",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkRootPermission() {
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
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noRootPermission),
                    getApplication<Application>().getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_warning
                )
            )
        } else {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noRootPermission),
                    getApplication<Application>().getString(R.string.someFuncMayRestrict),
                    "-3",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkIsIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= 23
            && !(getApplication<Application>().getSystemService(POWER_SERVICE) as PowerManager)
                .isIgnoringBatteryOptimizations("cf.playhi.freezeyou")
        ) {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noIgnoringBO),
                    getApplication<Application>().getString(R.string.someFuncMayBeAff),
                    "4",
                    R.drawable.ic_warning
                )
            )
        } else {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noIgnoringBO),
                    getApplication<Application>().getString(R.string.someFuncMayBeAff),
                    "4",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun checkIsPowerSaveMode() {
        if (Build.VERSION.SDK_INT >= 21
            && (getApplication<Application>().getSystemService(POWER_SERVICE) as PowerManager).isPowerSaveMode
        ) {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.inPowerSaveMode),
                    getApplication<Application>().getString(R.string.someFuncMayBeAff),
                    "5",
                    R.drawable.ic_warning
                )
            )
        } else {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.inPowerSaveMode),
                    getApplication<Application>().getString(R.string.someFuncMayBeAff),
                    "5",
                    R.drawable.ic_done
                )
            )
        }
    }

    private fun doRegenerateSomeCache() {
        getApplication<Application>().getSharedPreferences("NameOfPackages", MODE_PRIVATE)
            .edit().clear().apply()
        clearIconCache(getApplication())
        val cacheApplicationsIcons = PreferenceManager.getDefaultSharedPreferences(
            getApplication()
        ).getBoolean("cacheApplicationsIcons", false)
        val pm = getApplication<Application>().packageManager
        val installedApplications =
            pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)
        if (installedApplications != null) {
            val size = installedApplications.size
            for (i in 0 until size) {
                val applicationInfo = installedApplications[i]
                getApplicationLabel(
                    getApplication(),
                    pm,
                    applicationInfo,
                    applicationInfo.packageName
                )
                if (cacheApplicationsIcons) {
                    getApplicationIcon(
                        getApplication<Application>(), applicationInfo.packageName,
                        applicationInfo, false, true
                    )
                }
                loadingProgress.postValue(40 + (i.toDouble() / size.toDouble() * 50).toInt())
            }
        }
        problemsList.value!!.add(
            generateHashMap(
                getApplication<Application>().getString(R.string.regenerateSomeCache),
                getApplication<Application>().getString(R.string.updateSomeData),
                "10",
                R.drawable.ic_done
            )
        )
    }

    private fun checkIfNoProblemFound() {
        if (problemsList.value!!.isEmpty()) {
            problemsList.value!!.add(
                generateHashMap(
                    getApplication<Application>().getString(R.string.noProblemsFound),
                    getApplication<Application>().getString(R.string.everySeemsAllRight),
                    "-99",
                    R.drawable.ic_done
                )
            )
        }
    }

}
