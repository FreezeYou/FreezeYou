package cf.playhi.freezeyou.storage.mmkv

import android.os.Parcelable
import com.tencent.mmkv.MMKV

interface FreezeYouMMKVStorage {
    val mMMKV: MMKV
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean): FreezeYouMMKVStorage
    fun getString(key: String, defValue: String?): String?
    fun putString(key: String, value: String?): FreezeYouMMKVStorage
    fun <T : Parcelable> getParcelable(key: String, targetCls: Class<T>, defValue: T?): T?
    fun putParcelable(key: String?, value: Parcelable?): FreezeYouMMKVStorage
    fun sync(): FreezeYouMMKVStorage {
        mMMKV.sync()
        return this
    }
}