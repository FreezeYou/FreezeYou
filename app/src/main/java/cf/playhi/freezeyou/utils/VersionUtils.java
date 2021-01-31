package cf.playhi.freezeyou.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

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

    public static void checkUpdate(final Context context) {
        //"https://play.google.com/store/apps/details?id=cf.playhi.freezeyou"
        //"https://freezeyou.playhi.net/checkupdate.php?v=" + getVersionCode(context)
        AlertDialogUtils.buildAlertDialog(
                context,
                R.mipmap.ic_launcher_new_round,
                R.string.plsSelect,
                R.string.notice)
                .setPositiveButton(R.string.appStore, (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details/?id=cf.playhi.freezeyou"));
                    String title = context.getString(R.string.plsSelect);
                    Intent chooser = Intent.createChooser(intent, title);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(chooser);
                    }
                })
                .setNeutralButton(R.string.visitWebsite, (dialog, i) -> {
                    String s = getVersionName(context);
                    requestOpenWebSite(context,
                            s != null && s.contains("g") ?
                                    "https://play.google.com/store/apps/details?id=cf.playhi.freezeyou" :
                                    "https://freezeyou.playhi.net/checkupdate.php?v=" + getVersionCode(context));
                })
                .create().show();
    }

}
