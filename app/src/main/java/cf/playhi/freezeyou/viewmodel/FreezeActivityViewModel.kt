package cf.playhi.freezeyou.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.ACTION_MODE_FREEZE
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.ACTION_MODE_UNFREEZE
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.ERROR_NO_ERROR_SUCCESS
import cf.playhi.freezeyou.fuf.FreezeYouFUFSinglePackage
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.*
import cf.playhi.freezeyou.storage.mmkv.AverageTimeCostsMMKVStorage
import cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.*

class FreezeActivityViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var mStartedIntent: Intent
    private var mIsFromShortcut = false
    private var mTarget: String? = null
    private var mTasks: String? = null
    private var mPkgName: MutableLiveData<String> =
        MutableLiveData(getApplication<Application>().packageName)
    private var mToastStringId: MutableLiveData<Int> = MutableLiveData()
    private var mFinishMe: MutableLiveData<Boolean> = MutableLiveData(false)
    private var mShowDialog: MutableLiveData<DialogData?> = MutableLiveData()
    private var mPlayAnimator: MutableLiveData<PlayAnimatorData?> = MutableLiveData()
    private var mExecuteResult: MutableLiveData<ExecuteResult> = MutableLiveData()
    private var mAverageTimeCosts: Long = 500

    fun getPkgName(): LiveData<String> {
        return mPkgName
    }

    fun getToastStringId(): LiveData<Int> {
        return mToastStringId
    }

    fun getFinishMe(): LiveData<Boolean> {
        return mFinishMe
    }

    fun getShowDialog(): LiveData<DialogData?> {
        return mShowDialog
    }

    fun getPlayAnimator(): LiveData<PlayAnimatorData?> {
        return mPlayAnimator
    }

    fun getExecuteResult(): LiveData<ExecuteResult> {
        return mExecuteResult
    }

    fun getAverageTimeCosts(): Long {
        return mAverageTimeCosts
    }

    fun loadStartedIntentAndPkgName(startedIntent: Intent) {
        mStartedIntent = startedIntent
        if ("freezeyou" == mStartedIntent.scheme) {
            val dataUri = mStartedIntent.data
            dataUri?.run { getQueryParameter("pkgName")?.let { mPkgName.value = it } }
            mIsFromShortcut = false
        } else {
            mStartedIntent.getStringExtra("pkgName")?.let { mPkgName.value = it }
            mIsFromShortcut = mStartedIntent.getBooleanExtra("fromShortcut", true)
        }
        mTarget = mStartedIntent.getStringExtra("target")
        mTasks = mStartedIntent.getStringExtra("tasks")
    }

    fun go() {
        mPkgName.value.let {
            val target = mTarget
            val tasks = mTasks
            if (it.isNullOrEmpty() || it == getApplication<Application>().packageName) {
                mToastStringId.value = R.string.invalidArguments
                mFinishMe.value = true
                return
            }
            val frozen = realGetFrozenStatus(getApplication(), it, null)
            mAverageTimeCosts = AverageTimeCostsMMKVStorage().getParcelable(
                if (frozen) "Unfreeze" else "Freeze",
                AverageTime::class.java,
                AverageTime()
            )?.averageTimeCost?.let { cost -> if (cost < 200L) 200 else cost } ?: 500

            if (mIsFromShortcut && shortcutAutoFUF.getValue()) {
                if (frozen) {
                    fufAction(
                        it,
                        target,
                        tasks,
                        openImmediatelyAfterUnfreezeUseShortcutAutoFUF.getValue(),
                        true
                    )
                } else {
                    if (needConfirmWhenFreezeUseShortcutAutoFUF.getValue()) {
                        checkOpenAndUFImmediately(
                            it,
                            target,
                            tasks,
                            frozen = false,
                            ignoreAutoRun = true
                        )
                    } else {
                        fufAction(it, target, tasks, runImmediately = false, frozen = false)
                    }
                }
            } else {
                checkOpenAndUFImmediately(
                    it,
                    target,
                    tasks,
                    frozen,
                    !mIsFromShortcut
                )
            }
        }
    }

    private fun checkOpenAndUFImmediately(
        pkgName: String,
        target: String?,
        tasks: String?,
        frozen: Boolean,
        ignoreAutoRun: Boolean
    ) {
        if (!ignoreAutoRun && openAndUFImmediately.getValue()) {
            if (frozen) {
                fufAction(pkgName, target, tasks, runImmediately = true, frozen = true)
            } else {
                checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(pkgName, target, tasks)
                mFinishMe.postValue(true)
            }
        } else {
            mShowDialog.value = DialogData(pkgName, target, tasks, frozen, true)
        }
    }

    fun fufAction(
        pkgName: String,
        target: String?,
        tasks: String?,
        runImmediately: Boolean,
        frozen: Boolean
    ) {
        mPlayAnimator.value = PlayAnimatorData(pkgName, !frozen)
        viewModelScope.launch(Dispatchers.IO) {
            val startTime: Long = Date().time
            val freezeYouFUFSinglePackage = FreezeYouFUFSinglePackage(
                getApplication(),
                pkgName,
                if (frozen) ACTION_MODE_UNFREEZE else ACTION_MODE_FREEZE,
                needAskRun = frozen,
                runImmediately = runImmediately,
                tasks = tasks,
                target = target
            )
            val result = freezeYouFUFSinglePackage.commit()
            when (result) {
                ERROR_NO_ERROR_SUCCESS, ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT ->
                    recordTimeCost((Date().time - startTime), frozen)
            }
            mExecuteResult.postValue(ExecuteResult(result, freezeYouFUFSinglePackage))
            mFinishMe.postValue(true)
        }
    }

    private fun recordTimeCost(cost: Long, frozen: Boolean) {
        val key = if (frozen) "Unfreeze" else "Freeze"
        AverageTimeCostsMMKVStorage().getParcelable(
            key,
            AverageTime::class.java,
            AverageTime()
        )?.let {
            AverageTimeCostsMMKVStorage().putParcelable(
                key,
                AverageTime(
                    it.timeCosts.let { costs ->
                        if (costs.size >= 5) {
                            costs.removeFirst()
                        }
                        costs.addLast(cost)
                        costs
                    },
                    it.timeCosts.average().toLong()
                )
            )
        }
    }

    fun checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(data: DialogData): Int {
        return checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(
            data.pkgName,
            data.target,
            data.tasks
        )
    }

    private fun checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(
        pkgName: String,
        target: String?,
        tasks: String?
    ): Int {
        return FreezeYouFUFSinglePackage.checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(
            getApplication(), pkgName, target, tasks
        )
    }

}

data class DialogData(
    val pkgName: String,
    val target: String?,
    val tasks: String?,
    val frozen: Boolean,
    var show: Boolean
)

data class PlayAnimatorData(
    val pkgName: String,
    val freezing: Boolean
)

data class ExecuteResult(
    val result: Int,
    val freezeYouFUFSinglePackage: FreezeYouFUFSinglePackage
)

@Parcelize
data class AverageTime(
    val timeCosts: LinkedList<Long> = LinkedList(),
    val averageTimeCost: Long = 500
) : Parcelable
