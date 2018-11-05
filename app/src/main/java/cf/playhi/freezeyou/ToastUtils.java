package cf.playhi.freezeyou;

import android.content.Context;
import android.widget.Toast;

final class ToastUtils {

    static void showToast(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    static void showToast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

}
