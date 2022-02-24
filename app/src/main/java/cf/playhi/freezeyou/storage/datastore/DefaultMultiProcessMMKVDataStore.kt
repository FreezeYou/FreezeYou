package cf.playhi.freezeyou.storage.datastore

import androidx.preference.PreferenceDataStore
import cf.playhi.freezeyou.storage.mmkv.DefaultMultiProcessMMKVStorage

class DefaultMultiProcessMMKVDataStore : PreferenceDataStore() {

    private var storage: DefaultMultiProcessMMKVStorage = DefaultMultiProcessMMKVStorage()

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return storage.getBoolean(key, defValue)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        storage.putBoolean(key, value)
    }
}