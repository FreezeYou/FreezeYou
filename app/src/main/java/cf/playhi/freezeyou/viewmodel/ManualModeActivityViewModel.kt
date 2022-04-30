package cf.playhi.freezeyou.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cf.playhi.freezeyou.fuf.FUFSinglePackage
import cf.playhi.freezeyou.utils.FUFUtils.getFUFRelatedToastString
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManualModeActivityViewModel(application: Application) : AndroidViewModel(application) {

    private var selectedMode: Int = -1
    private var selectedModeCheckedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    private var currentPackageName: String = ""

    fun setSelectedMode(mode: Int) {
        selectedMode = mode
    }

    fun getSelectedModeCheckedPosition(): LiveData<Int> {
        return selectedModeCheckedPosition
    }

    fun setSelectedModeCheckedPosition(mode: Int) {
        selectedModeCheckedPosition.value = mode
    }

    fun getCurrentPackageName(): String {
        return currentPackageName
    }

    fun setCurrentPackageName(pkgName: String) {
        currentPackageName = pkgName
    }

    fun processFUFOperation(pkgName: String, context: Context, freeze: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            showToast(
                getApplication(),
                getFUFRelatedToastString(
                    context,
                    withContext(Dispatchers.IO) {
                        FUFSinglePackage(
                            context,
                            pkgName,
                            if (freeze) FUFSinglePackage.ACTION_MODE_FREEZE else FUFSinglePackage.ACTION_MODE_UNFREEZE,
                            selectedMode
                        ).commit()
                    },
                    true
                )
            )
        }
    }

}