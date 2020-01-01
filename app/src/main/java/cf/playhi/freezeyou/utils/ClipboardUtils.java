package cf.playhi.freezeyou.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public final class ClipboardUtils {

    public static boolean copyToClipboard(Context context, String data) {
        boolean success = false;
        ClipboardManager copy = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(data, data);
        if (copy != null) {
            copy.setPrimaryClip(clip);
            success = true;
        }
        return success;
    }

    public static CharSequence getClipboardItemText(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null || !cm.hasPrimaryClip()) {
            return "";
        }
        ClipData clip = cm.getPrimaryClip();
        if (clip == null || clip.getItemCount() <= 0) {
            return "";
        }
        return clip.getItemAt(0).getText();
    }

}
