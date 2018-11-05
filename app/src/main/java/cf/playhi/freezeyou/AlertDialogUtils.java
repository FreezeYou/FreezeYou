package cf.playhi.freezeyou;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

final class AlertDialogUtils {

    static AlertDialog.Builder buildAlertDialog(Context context, int icon, int message, int title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(icon);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    static AlertDialog.Builder buildAlertDialog(Context context, Drawable icon, CharSequence message, CharSequence title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(icon);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

}
