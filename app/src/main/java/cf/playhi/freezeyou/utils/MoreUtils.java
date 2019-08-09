package cf.playhi.freezeyou.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cf.playhi.freezeyou.R;

import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public final class MoreUtils {

    public static void requestOpenWebSite(Context context, String url) {
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

    public static void copyToClipboard(Context context, String data) {
        ClipboardManager copy = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(data, data);
        if (copy != null) {
            copy.setPrimaryClip(clip);
            showToast(context, R.string.success);
        } else {
            showToast(context, R.string.failed);
        }
    }

    public static CharSequence getClipboardItemText(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!cm.hasPrimaryClip()) {
            return "";
        }
        ClipData clip = cm.getPrimaryClip();
        if (clip == null || clip.getItemCount() <= 0) {
            return "";
        }
        return clip.getItemAt(0).getText();
    }

    public static void joinQQGroup(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D92NGzlhmCK_UFrL_oEAV7Fe6QrvFR5y_"));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            requestOpenWebSite(context, "https://shang.qq.com/wpa/qunwpa?idkey=cbc8ae71402e8a1bc9bb4c39384bcfe5b9f7d18ff1548ea9bdd842f036832f3d");
        }
    }

    public static ArrayList<String> convertToList(String origin, String s) {
        return origin == null ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(origin.split(s)));
    }

    public static ArrayList<String> convertToList(String[] origin) {
        return origin == null ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(origin));
    }

    public static String listToString(List<String> l, String s) {

        if (l == null)
            return "";

        StringBuilder sb = new StringBuilder();
        for (String s1 : l) {
            sb.append(s1);
            sb.append(s);
        }
        return sb.toString();
    }

}
