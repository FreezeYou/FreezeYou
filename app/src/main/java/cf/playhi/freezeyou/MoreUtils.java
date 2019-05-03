package cf.playhi.freezeyou;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cf.playhi.freezeyou.ToastUtils.showToast;

final class MoreUtils {

    static void requestOpenWebSite(Context context, String url) {
        Uri webPage = Uri.parse(url);
        Intent about = new Intent(Intent.ACTION_VIEW, webPage);
        if (about.resolveActivity(context.getPackageManager()) != null) {
            try {
                context.startActivity(about);
            } catch (RuntimeException e) {
                e.printStackTrace();
                about.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(about);
            }
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

    static void joinQQGroup(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D92NGzlhmCK_UFrL_oEAV7Fe6QrvFR5y_"));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            requestOpenWebSite(context, "https://shang.qq.com/wpa/qunwpa?idkey=cbc8ae71402e8a1bc9bb4c39384bcfe5b9f7d18ff1548ea9bdd842f036832f3d");
        }
    }

    static ArrayList<String> convertToList(String origin, String s) {
        return origin == null ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(origin.split(s)));
    }

    static ArrayList<String> convertToList(String[] origin) {
        return origin == null ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(origin));
    }

    static String listToString(List<String> l, String s) {

        if (l == null)
            return "";

        StringBuilder sb = new StringBuilder();
        for (String s1 : l) {
            sb.append(s1);
            sb.append(s);
        }
        return sb.toString();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static boolean canAccessUsagePermission(Context context) {

        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        int mode = -1;

        if (appOpsManager != null)
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
