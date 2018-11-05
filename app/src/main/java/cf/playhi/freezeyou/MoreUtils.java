package cf.playhi.freezeyou;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import static cf.playhi.freezeyou.ToastUtils.showToast;

final class MoreUtils {

    static void requestOpenWebSite(Context context, String url) {
        Uri webPage = Uri.parse(url);
        Intent about = new Intent(Intent.ACTION_VIEW, webPage);
        if (about.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(about);
        } else {
            showToast(context, context.getString(R.string.plsVisit) + " " + url);
            copyToClipboard(context, url);
        }
    }

    static void copyToClipboard(Context context, String data) {
        ClipboardManager copy = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(data, data);
        if (copy != null) {
            copy.setPrimaryClip(clip);
            showToast(context, R.string.success);
        } else {
            showToast(context, R.string.failed);
        }
    }

}
