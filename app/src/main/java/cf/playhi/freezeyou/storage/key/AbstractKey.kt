package cf.playhi.freezeyou.storage.key

import android.content.Context

interface AbstractKey<T> {
    fun defaultValue(): T
    fun titleTextStringId(): Int
    fun category(): Int
    fun getValue(context: Context? = null): T
    fun setValue(context: Context? = null, value: T)
}