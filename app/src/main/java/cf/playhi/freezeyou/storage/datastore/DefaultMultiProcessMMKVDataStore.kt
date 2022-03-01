package cf.playhi.freezeyou.storage.datastore

import androidx.preference.PreferenceDataStore
import cf.playhi.freezeyou.storage.mmkv.DefaultMultiProcessMMKVStorage

class DefaultMultiProcessMMKVDataStore : PreferenceDataStore() {

    private var storage: DefaultMultiProcessMMKVStorage = DefaultMultiProcessMMKVStorage()

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return if (key == null) false else storage.getBoolean(key, defValue)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        if (key != null) storage.putBoolean(key, value)
    }

    override fun getString(key: String?, defValue: String?): String? {
        return if (key == null) null else storage.getString(key, defValue)
    }

    override fun putString(key: String?, value: String?) {
        if (key != null) storage.putString(key, value)
    }
}