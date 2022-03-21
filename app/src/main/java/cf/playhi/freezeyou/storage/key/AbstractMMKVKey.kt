package cf.playhi.freezeyou.storage.key

import cf.playhi.freezeyou.storage.mmkv.FreezeYouMMKVStorage

interface AbstractMMKVKey<T> : AbstractKey<T> {
    fun sync(): FreezeYouMMKVStorage
}