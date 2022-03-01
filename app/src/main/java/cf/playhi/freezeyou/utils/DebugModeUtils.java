package cf.playhi.freezeyou.utils;

import cf.playhi.freezeyou.storage.mmkv.DefaultMultiProcessMMKVStorage;

import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.debugModeEnabled;

public final class DebugModeUtils {

    public static boolean isDebugModeEnabled() {
        return new DefaultMultiProcessMMKVStorage()
                .getBoolean(debugModeEnabled.name(), debugModeEnabled.defaultValue());
    }
}
