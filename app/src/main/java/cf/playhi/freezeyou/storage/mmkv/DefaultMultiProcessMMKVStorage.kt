package cf.playhi.freezeyou.storage.mmkv

import android.os.Parcelable
import com.tencent.mmkv.MMKV

class DefaultMultiProcessMMKVStorage : FreezeYouMMKVStorage {
    override val mMMKV: MMKV =
        MMKV.mmkvWithID("DefaultMultiProcessKV", MMKV.MULTI_PROCESS_MODE)

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return mMMKV.decodeBool(key, defValue)
    }

    override fun putBoolean(key: String, value: Boolean): DefaultMultiProcessMMKVStorage {
        mMMKV.encode(key, value)
        return this
    }

    override fun getString(key: String, defValue: String?): String? {
        return mMMKV.decodeString(key, defValue)
    }

    override fun putString(key: String, value: String?): DefaultMultiProcessMMKVStorage {
        mMMKV.encode(key, value)
        return this
    }

    override fun <T : Parcelable> getParcelable(
        key: String,
        targetCls: Class<T>,
        defValue: T?
    ): T? {
        throw UnsupportedOperationException()
    }

    override fun putParcelable(key: String?, value: Parcelable?): FreezeYouMMKVStorage {
        throw UnsupportedOperationException()
    }

}