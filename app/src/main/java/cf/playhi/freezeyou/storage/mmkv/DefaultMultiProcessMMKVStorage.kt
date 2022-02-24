package cf.playhi.freezeyou.storage.mmkv

import com.tencent.mmkv.MMKV

class DefaultMultiProcessMMKVStorage : FreezeYouMMKVStorage {
    private var mMMKV: MMKV =
        MMKV.mmkvWithID("DefaultMultiProcessKV", MMKV.MULTI_PROCESS_MODE)

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mMMKV.decodeBool(key, defValue)
    }

    override fun putBoolean(key: String?, value: Boolean): DefaultMultiProcessMMKVStorage {
        mMMKV.encode(key, value)
        return this
    }

    override fun sync(): FreezeYouMMKVStorage {
        mMMKV.sync()
        return this
    }
}