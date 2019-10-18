package cf.playhi.freezeyou.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

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
            ClipboardUtils.copyToClipboard(context, url);
        }
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

    public static void requestOpenDonateWebSite(Context context) {
        requestOpenWebSite(
                context,
                new String(
                        Base64.decode(
                                "aHR0cHM6Ly9mcmVlemV5b3UucGxheWhpLm5ldC9zcG9uc29yc2hpcC5odG1sP2Zyb209RnJlZXplWW91",
                                Base64.DEFAULT
                        )
                )
        );
    }

}
