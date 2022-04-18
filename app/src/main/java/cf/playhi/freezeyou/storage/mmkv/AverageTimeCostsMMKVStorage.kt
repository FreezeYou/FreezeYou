package cf.playhi.freezeyou.storage.mmkv

import android.os.Parcelable
import com.tencent.mmkv.MMKV

class AverageTimeCostsMMKVStorage : FreezeYouMMKVStorage {
    override val mMMKV: MMKV =
        MMKV.mmkvWithID("AverageTimeCostsKV", MMKV.MULTI_PROCESS_MODE)

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        throw UnsupportedOperationException()
    }

    override fun putBoolean(key: String, value: Boolean): FreezeYouMMKVStorage {
        throw UnsupportedOperationException()
    }

    override fun getString(key: String, defValue: String?): String? {
        throw UnsupportedOperationException()
    }

    override fun putString(key: String, value: String?): FreezeYouMMKVStorage {
        throw UnsupportedOperationException()
    }

    override fun <T : Parcelable> getParcelable(
        key: String,
        targetCls: Class<T>,
        defValue: T?
    ): T? {
        return mMMKV.decodeParcelable(key, targetCls, defValue)
    }

    override fun putParcelable(key: String?, value: Parcelable?): FreezeYouMMKVStorage {
        mMMKV.encode(key, value)
        return this
    }

}