package cf.playhi.freezeyou.utils;

import cf.playhi.freezeyou.storage.mmkv.DefaultMultiProcessMMKVStorage;
import cf.playhi.freezeyou.storage.mmkv.DefaultMultiProcessMMKVStorageBooleanFalseKeys;

public final class DebugModeUtils {

    public static boolean isDebugModeEnabled() {
        return new DefaultMultiProcessMMKVStorage()
                .getBoolean(
                        DefaultMultiProcessMMKVStorageBooleanFalseKeys.DebugModeEnabled.name(),
                        false
                );
    }
}
