package cf.playhi.freezeyou;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

final class DebugModeUtils {

    static boolean isDebugModeEnabled(Context context) {
        return new AppPreferences(context).getBoolean("debugModeEnabled", false);
    }
}
