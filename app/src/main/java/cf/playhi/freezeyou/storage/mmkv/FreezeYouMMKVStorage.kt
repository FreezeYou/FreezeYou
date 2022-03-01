package cf.playhi.freezeyou.storage.mmkv

interface FreezeYouMMKVStorage {
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean): FreezeYouMMKVStorage
    fun getString(key: String, defValue: String?): String?
    fun putString(key: String, value: String?): FreezeYouMMKVStorage
    fun sync(): FreezeYouMMKVStorage
}