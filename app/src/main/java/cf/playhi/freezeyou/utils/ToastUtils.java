package cf.playhi.freezeyou.utils;

import android.content.Context;
import android.widget.Toast;

public final class ToastUtils {

    public static void showToast(Context context, int id) {
        showLongToast(context, id);
    }

    public static void showToast(Context context, String string) {
        if (string != null)
            showLongToast(context, string);
    }

    public static void showLongToast(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    public static void showLongToast(Context context, String string) {
        if (string != null)
            Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_SHORT).show();
    }

    public static void showShortToast(Context context, String string) {
        if (string != null)
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

}
