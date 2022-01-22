package cf.playhi.freezeyou

import androidx.preference.PreferenceDataStore
import com.tencent.mmkv.MMKV

class DefaultMultiProcessMMKVDataStore : PreferenceDataStore() {

    private var mMMKV: MMKV =
        MMKV.mmkvWithID("DefaultMultiProcessKV", MMKV.MULTI_PROCESS_MODE)

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mMMKV.decodeBool(key, defValue)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        mMMKV.encode(key, value)
    }
}