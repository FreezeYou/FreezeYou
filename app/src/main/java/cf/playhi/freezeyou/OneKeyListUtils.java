package cf.playhi.freezeyou;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

import java.util.Arrays;

final class OneKeyListUtils {

    static boolean addToOneKeyList(Context context, String key, String pkgName) {
        final AppPreferences sharedPreferences = new AppPreferences(context);
        final String pkgNames = sharedPreferences.getString(key, "");
        return existsInOneKeyList(pkgNames, pkgName) || sharedPreferences.put(key, pkgNames + pkgName + ",");
    }

    static boolean removeFromOneKeyList(Context context, String key, String pkgName) {
        final AppPreferences sharedPreferences = new AppPreferences(context);
        final String pkgNames = sharedPreferences.getString(key, "");
        return !existsInOneKeyList(pkgNames, pkgName) || sharedPreferences.put(key, pkgNames.replace(pkgName + ",", ""));
    }

    static boolean existsInOneKeyList(String pkgNames, String pkgName) {
        return pkgNames != null && Arrays.asList(pkgNames.split(",")).contains(pkgName);
    }

    static boolean existsInOneKeyList(Context context, String onekeyName, String pkgName) {
        final String pkgNames = new AppPreferences(context).getString(onekeyName, "");
        return pkgNames != null && Arrays.asList(pkgNames.split(",")).contains(pkgName);
    }

    static boolean removeUninstalledFromOneKeyList(Context context, String oneKeyName) {
        String s = new AppPreferences(context).getString(oneKeyName, "");

        if (s == null) {
            return false;
        }

        String[] strings = s.split(",");
        for (String pkgName : strings) {
            if (pkgName != null) {
                if (ApplicationInfoUtils
                        .getApplicationInfoFromPkgName(pkgName, context) == null) {
                    OneKeyListUtils.removeFromOneKeyList(
                            context,
                            oneKeyName,
                            pkgName
                    );
                }
            }
        }
        return true;
    }

}
