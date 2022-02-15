package cf.playhi.freezeyou.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ShowLogcatViewModel : ViewModel() {

    private val mutex = Mutex()
    private val mLog: MutableLiveData<String> = MutableLiveData()

    fun loadLog() {
        viewModelScope.launch(Dispatchers.IO) {
            if (mutex.tryLock("ShowLogcatViewModelLoadingLog")) {
                val sb = StringBuilder()
                try {
                    val process = Runtime.getRuntime().exec("logcat -d")
                    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                    var logLine: String?
                    val rn = System.getProperty("line.separator")
                    while (bufferedReader.readLine().also { logLine = it } != null) {
                        sb.append(logLine).append(rn)
                    }
                    bufferedReader.close()
                    process.destroy()
                } catch (e: IOException) {
                    e.printStackTrace()
                    sb.append(e.localizedMessage)
                }
                mLog.postValue(sb.toString())

                mutex.unlock("ShowLogcatViewModelLoadingLog")
            }
        }
    }

    fun getLog(): LiveData<String> {
        return mLog
    }
}