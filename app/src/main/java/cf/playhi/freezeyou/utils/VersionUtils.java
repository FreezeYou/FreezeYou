package cf.playhi.freezeyou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.Date;

import cf.playhi.freezeyou.R;

import static cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite;

final public class VersionUtils {

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = 0;
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = 0;
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return "";
    }

    public static boolean isGooglePlayVersion(Context context) {
        return getVersionName(context).contains("gp");
    }

    public static void checkUpdate(final Activity activity) {
        //"https://play.google.com/store/apps/details?id=cf.playhi.freezeyou"
        //"https://freezeyou.playhi.net/checkupdate.php?v=" + getVersionCode(context)
        AlertDialogUtils.buildAlertDialog(
                        activity,
                        R.mipmap.ic_launcher_new_round,
                        R.string.plsSelect,
                        R.string.notice)
                .setPositiveButton(R.string.appStore, (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details/?id=cf.playhi.freezeyou"));
                    String title = activity.getString(R.string.plsSelect);
                    Intent chooser = Intent.createChooser(intent, title);
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivity(chooser);
                    }
                })
                .setNeutralButton(R.string.visitWebsite, (dialog, i) -> requestOpenWebSite(activity,
                        isGooglePlayVersion(activity) ?
                                "https://play.google.com/store/apps/details?id=cf.playhi.freezeyou" :
                                "https://freezeyou.playhi.net/checkupdate.php?v=" + getVersionCode(activity)))
                .show();
    }

    /**
     * @return Whether need to ask for checking update.
     */
    public static boolean isOutdated(Context context) {
        return isOutdated(context.getSharedPreferences("Ver", Context.MODE_PRIVATE));
    }

    /**
     * @param sp SharedPreferences, name Ver.
     * @return Whether need to ask for checking update.
     */
    public static boolean isOutdated(SharedPreferences sp) {
        return (new Date().getTime() - sp.getLong("Time", 0L)) > 5184000000L;
    }

}
