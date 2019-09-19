package cf.playhi.freezeyou.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import net.grandcentrix.tray.AppPreferences;

import java.util.Arrays;

public final class OneKeyListUtils {

    public static boolean addToOneKeyList(Context context, String key, String pkgName) {
        final AppPreferences sharedPreferences = new AppPreferences(context);
        final String pkgNames = sharedPreferences.getString(key, "");
        return existsInOneKeyList(pkgNames, pkgName) || sharedPreferences.put(key, pkgNames + pkgName + ",");
    }

    public static boolean removeFromOneKeyList(Context context, String key, String pkgName) {
        final AppPreferences sharedPreferences = new AppPreferences(context);
        final String pkgNames = sharedPreferences.getString(key, "");
        return !existsInOneKeyList(pkgNames, pkgName) || sharedPreferences.put(key, pkgNames.replace(pkgName + ",", ""));
    }

    public static boolean existsInOneKeyList(String pkgNames, String pkgName) {
        return pkgNames != null && Arrays.asList(pkgNames.split(",")).contains(pkgName);
    }

    public static boolean existsInOneKeyList(Context context, String onekeyName, String pkgName) {
        final String pkgNames = new AppPreferences(context).getString(onekeyName, "");
        return pkgNames != null && Arrays.asList(pkgNames.split(",")).contains(pkgName);
    }

    public static boolean removeUninstalledFromOneKeyList(Context context, String oneKeyName) {
        String s = new AppPreferences(context).getString(oneKeyName, "");

        if (s == null) {
            return false;
        }

        String[] strings = s.split(",");
        for (String pkgName : strings) {
            if (pkgName != null &&
                    ApplicationInfoUtils
                            .getApplicationInfoFromPkgName(pkgName, context) == null) {
                OneKeyListUtils.removeFromOneKeyList(
                        context,
                        oneKeyName,
                        pkgName
                );
            }
        }
        return true;
    }

    public static String[] decodeUserListsInPackageNames(Context context, String[] pkgs) {
        StringBuilder result = new StringBuilder();
        SQLiteDatabase userDefinedDb = context.openOrCreateDatabase("userDefinedCategories", Context.MODE_PRIVATE, null);
        for (String pkg : pkgs) {
            if ("".equals(pkg.trim())) {
                continue;
            }
            if (pkg.startsWith("@")) {
                try {
                    String labelBase64 =
                            Base64.encodeToString(
                                    Base64.decode(pkg.substring(1), Base64.DEFAULT),
                                    Base64.DEFAULT
                            );

                    userDefinedDb.execSQL(
                            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                    );
                    Cursor cursor =
                            userDefinedDb.query(
                                    "categories",
                                    new String[]{"packages"},
                                    "label = '" + labelBase64 + "'",
                                    null, null,
                                    null, null
                            );

                    if (cursor.moveToFirst()) {
                        result.append(cursor.getString(cursor.getColumnIndex("packages")));
                    }
                    cursor.close();
                    userDefinedDb.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                result.append(pkg);
            }
            if (result.length() != 0 && result.charAt(result.length() - 1) != ',') {
                result.append(",");
            }
        }
        return result.toString().split(",");
    }
}
