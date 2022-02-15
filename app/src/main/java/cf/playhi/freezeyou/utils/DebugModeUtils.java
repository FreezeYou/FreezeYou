package cf.playhi.freezeyou.utils;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

public final class DebugModeUtils {

    public static boolean isDebugModeEnabled(Context context) {
        return new AppPreferences(context).getBoolean("debugModeEnabled", false);
    }
}
