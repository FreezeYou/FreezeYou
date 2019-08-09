package cf.playhi.freezeyou.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

public final class ApplicationInfoUtils {

    public static ApplicationInfo getApplicationInfoFromPkgName(String pkgName, Context context) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(pkgName, GET_UNINSTALLED_PACKAGES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applicationInfo;
    }

}
