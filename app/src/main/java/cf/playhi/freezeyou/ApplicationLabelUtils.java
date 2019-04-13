package cf.playhi.freezeyou;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import static android.content.Context.MODE_PRIVATE;
import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

final class ApplicationLabelUtils {

    static String getApplicationLabel(Context context, PackageManager packageManager, ApplicationInfo applicationInfo, String pkgName) {

        if (pkgName == null) {
            return "";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("NameOfPackages", MODE_PRIVATE);
        String name = sharedPreferences.getString(pkgName, "");
        if (!"".equals(name)) {
            return name;
        }
        PackageManager pm = packageManager == null ? context.getPackageManager() : packageManager;
        if (applicationInfo != null) {
            name = applicationInfo.loadLabel(pm).toString();
            sharedPreferences.edit().putString(pkgName, name).apply();
            return name;
        } else {
            try {
                name = pm.getApplicationInfo(pkgName, GET_UNINSTALLED_PACKAGES).loadLabel(pm).toString();
                sharedPreferences.edit().putString(pkgName, name).apply();
                return name;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return context.getString(R.string.uninstalled);
            } catch (Exception e) {
                e.printStackTrace();
                return pkgName;
            }
        }
    }

}
