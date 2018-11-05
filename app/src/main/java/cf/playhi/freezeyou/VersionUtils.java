package cf.playhi.freezeyou;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import static cf.playhi.freezeyou.MoreUtils.requestOpenWebSite;

final class VersionUtils {

    static int getVersionCode(Context context) {
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

    static String getVersionName(Context context) {
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

    static void checkUpdate(Context context) {
        //"https://play.google.com/store/apps/details?id=cf.playhi.freezeyou"
        //"https://freezeyou.playhi.cf/checkupdate.php?v=" + getVersionCode(context)
        String s = getVersionName(context);
        requestOpenWebSite(context, s != null && s.contains("g") ? "https://play.google.com/store/apps/details?id=cf.playhi.freezeyou" : "https://freezeyou.playhi.net/checkupdate.php?v=" + getVersionCode(context));
    }

}
