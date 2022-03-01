package cf.playhi.freezeyou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import cf.playhi.freezeyou.R;

import static android.content.Context.MODE_PRIVATE;

//导入导出整体结构复杂度高，处理流程复杂度高，待有可用的优化方案时需要进行优化（提示：后续导出时可修改导出时的 version，用以导入时区分方案）
public final class BackupUtils {

    public static String convertSharedPreference(
            SharedPreferences sharedPreferences, String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    public static String convertSharedPreference(
            AppPreferences appPreferences, String key, String defValue) {
        return appPreferences.getString(key, defValue);
    }

    public static boolean convertSharedPreference(
            SharedPreferences sharedPreferences, String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static boolean convertSharedPreference(
            AppPreferences appPreferences, String key, boolean defValue) {
        return appPreferences.getBoolean(key, defValue);
    }

    private static void importStringSharedPreference(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences sp, String key) {
        sp.edit().putString(key, jsonObject.optString(key, "")).apply();
        SettingsUtils.syncAndCheckSharedPreference(context, activity, sp, key);
    }

    private static void importBooleanSharedPreference(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences sp, String key) {
        sp.edit().putBoolean(key, jsonObject.optBoolean(key, false)).apply();
        SettingsUtils.syncAndCheckSharedPreference(context, activity, sp, key);
    }

    private static void importIntSharedPreference(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences sp, String key) {
        sp.edit().putInt(key, jsonObject.optInt(key, 0)).apply();
        SettingsUtils.syncAndCheckSharedPreference(context, activity, sp, key);
    }

    private static boolean importUserTimeTasksJSONArray(Context context, JSONObject jsonObject) {
        JSONArray userTimeScheduledTasksJSONArray =
                jsonObject.optJSONArray("userTimeScheduledTasks");
        if (userTimeScheduledTasksJSONArray == null) {
            return false;
        }

        SQLiteDatabase db = context.openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );

        boolean isCompletelySuccess = true;
        JSONObject oneUserTimeScheduledTaskJSONObject;
        for (int i = 0; i < userTimeScheduledTasksJSONArray.length(); ++i) {
            try {
                oneUserTimeScheduledTaskJSONObject = userTimeScheduledTasksJSONArray.optJSONObject(i);
                if (oneUserTimeScheduledTaskJSONObject == null) {
                    isCompletelySuccess = false;
                    continue;
                }
                if (oneUserTimeScheduledTaskJSONObject.optBoolean("doNotImport", false)) {
                    continue;
                }
                db.execSQL(
                        "insert into tasks(_id,hour,minutes,repeat,enabled,label,task,column1,column2) values(null,"
                                + oneUserTimeScheduledTaskJSONObject.getInt("hour") + ","
                                + oneUserTimeScheduledTaskJSONObject.getInt("minutes") + ","
                                + oneUserTimeScheduledTaskJSONObject.getString("repeat") + ","
                                + oneUserTimeScheduledTaskJSONObject.getInt("enabled") + ","
                                + "'" + oneUserTimeScheduledTaskJSONObject.getString("label") + "'" + ","
                                + "'" + oneUserTimeScheduledTaskJSONObject.getString("task") + "'" + ",'','')"
                );
            } catch (JSONException e) {
                isCompletelySuccess = false;
            }
        }

        db.close();
        TasksUtils.checkTimeTasks(context);

        return isCompletelySuccess;
    }

    private static boolean importUserTriggerTasksJSONArray(Context context, JSONObject jsonObject) {
        JSONArray userTriggerScheduledTasksJSONArray =
                jsonObject.optJSONArray("userTriggerScheduledTasks");
        if (userTriggerScheduledTasksJSONArray == null) {
            return false;
        }

        SQLiteDatabase db = context.openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );

        JSONObject oneUserTriggerScheduledTaskJSONObject;
        boolean isCompletelySuccess = true;
        for (int i = 0; i < userTriggerScheduledTasksJSONArray.length(); ++i) {
            try {
                oneUserTriggerScheduledTaskJSONObject = userTriggerScheduledTasksJSONArray.optJSONObject(i);
                if (oneUserTriggerScheduledTaskJSONObject == null) {
                    isCompletelySuccess = false;
                    continue;
                }
                if (oneUserTriggerScheduledTaskJSONObject.optBoolean("doNotImport", false)) {
                    continue;
                }
                db.execSQL(
                        "insert into tasks(_id,tg,tgextra,enabled,label,task,column1,column2) VALUES (null,"
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("tg") + "'" + ","
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("tgextra") + "'" + ","
                                + oneUserTriggerScheduledTaskJSONObject.getInt("enabled") + ","
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("label") + "'" + ","
                                + "'" + oneUserTriggerScheduledTaskJSONObject.getString("task") + "'" + ",'','')"
                );
            } catch (JSONException e) {
                isCompletelySuccess = false;
            }
        }

        db.close();
        TasksUtils.checkTriggerTasks(context);

        return isCompletelySuccess;
    }

    private static boolean importUserDefinedCategoriesJSONArray(Context context, JSONObject jsonObject) {
        JSONArray userDefinedCategoriesJSONArray =
                jsonObject.optJSONArray("userDefinedCategories");
        if (userDefinedCategoriesJSONArray == null) {
            return false;
        }

        SQLiteDatabase db = context.openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        );

        ArrayList<String> existedLabels = new ArrayList<>();
        Cursor cursor = db.query("categories", new String[]{"label"}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                existedLabels.add(cursor.getString(cursor.getColumnIndexOrThrow("label")));
                cursor.moveToNext();
            }
        }
        cursor.close();

        JSONObject oneUserDefinedCategoriesJSONObject;
        boolean isCompletelySuccess = true;
        for (int i = 0; i < userDefinedCategoriesJSONArray.length(); ++i) {
            try {
                oneUserDefinedCategoriesJSONObject = userDefinedCategoriesJSONArray.optJSONObject(i);
                if (oneUserDefinedCategoriesJSONObject == null) {
                    isCompletelySuccess = false;
                    continue;
                }
                if (oneUserDefinedCategoriesJSONObject.optBoolean("doNotImport", false)) {
                    continue;
                }
                String label = oneUserDefinedCategoriesJSONObject.getString("label");
                if (existedLabels.contains(label)) {
                    db.execSQL(
                            "update categories set packages = '"
                                    + oneUserDefinedCategoriesJSONObject.getString("packages")
                                    + "' where label = '" + label + "';"
                    );
                } else {
                    db.execSQL(
                            "insert into categories(_id,label,packages) VALUES ( "
                                    + null + ",'"
                                    + label + "','"
                                    + oneUserDefinedCategoriesJSONObject.getString("packages") + "');"
                    );
                }
            } catch (JSONException e) {
                isCompletelySuccess = false;
            }
        }

        db.close();

        return isCompletelySuccess;
    }

    private static boolean importOneKeyLists(Context context, JSONObject jsonObject, AppPreferences appPreferences) {
        JSONArray array = jsonObject.optJSONArray("oneKeyList");
        if (array == null) {
            return false;
        }
        JSONObject oneKeyListJSONObject = array.optJSONObject(0);
        if (oneKeyListJSONObject == null) {
            return false;
        }

        importOneKeyListsFromProcessedJSONObject(
                context, oneKeyListJSONObject, appPreferences);

        return true;
    }

    private static void importOneKeyListsFromProcessedJSONObject(
            Context context, JSONObject oneKeyListJSONObject, AppPreferences appPreferences) {
        Iterator<String> it = oneKeyListJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "okff":
                    appPreferences.put(
                            context.getString(R.string.sAutoFreezeApplicationList),
                            oneKeyListJSONObject.optString("okff"));
                    break;
                case "okuf":
                    appPreferences.put(
                            context.getString(R.string.sOneKeyUFApplicationList),
                            oneKeyListJSONObject.optString("okuf"));
                    break;
                case "foq":
                    appPreferences.put(
                            context.getString(R.string.sFreezeOnceQuit),
                            oneKeyListJSONObject.optString("foq"));
                    break;
                default:
                    break;
            }
        }
    }


    private static boolean importIntSharedPreferences(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        // Int 开始
        JSONArray array = jsonObject.optJSONArray("generalSettings_int");
        if (array == null) {
            return false;
        }
        JSONObject generalSettingsIntJSONObject = array.optJSONObject(0);
        if (generalSettingsIntJSONObject == null) {
            return false;
        }

        importIntSharedPreferencesFromProcessedJSONObject(
                context, activity, generalSettingsIntJSONObject, defSP, appPreferences);
        // Int 结束
        return true;
    }

    private static void importIntSharedPreferencesFromProcessedJSONObject(
            Context context, Activity activity, JSONObject generalSettingsIntJSONObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        Iterator<String> it = generalSettingsIntJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "onClickFunctionStatus":
                case "sortMethodStatus":
                    importIntSharedPreference(
                            context, activity, generalSettingsIntJSONObject, defSP, s);
                    break;
                default:
                    break;
            }
        }
    }

    private static boolean importStringSharedPreferences(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        // String 开始
        JSONArray array = jsonObject.optJSONArray("generalSettings_string");
        if (array == null) {
            return false;
        }
        JSONObject generalSettingsStringJSONObject = array.optJSONObject(0);
        if (generalSettingsStringJSONObject == null) {
            return false;
        }
        importStringSharedPreferencesFromProcessedJSONObject(
                context, activity, generalSettingsStringJSONObject, defSP, appPreferences);
        // String 结束
        return true;
    }

    private static void importStringSharedPreferencesFromProcessedJSONObject(
            Context context, Activity activity, JSONObject generalSettingsStringJSONObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        Iterator<String> it = generalSettingsStringJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "onClickFuncChooseActionStyle":
                case "uiStyleSelection":
                case "launchMode":
                case "organizationName":
                case "shortCutOneKeyFreezeAdditionalOptions":
                    importStringSharedPreference(
                            context, activity, generalSettingsStringJSONObject, defSP, s);
                    break;
                default:
                    break;
            }
        }
    }

    private static boolean importBooleanSharedPreferences(
            Context context, Activity activity, JSONObject jsonObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        // boolean 开始
        JSONArray array = jsonObject.optJSONArray("generalSettings_boolean");
        if (array == null) {
            return false;
        }
        JSONObject generalSettingsBooleanJSONObject = array.optJSONObject(0);
        if (generalSettingsBooleanJSONObject == null) {
            return false;
        }

        importBooleanSharedPreferencesFromProcessedJSONObject(
                context, activity, generalSettingsBooleanJSONObject, defSP, appPreferences);
        // boolean 结束
        return true;
    }

    private static void importBooleanSharedPreferencesFromProcessedJSONObject(
            Context context, Activity activity, JSONObject generalSettingsBooleanJSONObject,
            SharedPreferences defSP, AppPreferences appPreferences) {
        Iterator<String> it = generalSettingsBooleanJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "allowEditWhenCreateShortcut":
                case "noCaution":
                case "saveOnClickFunctionStatus":
                case "saveSortMethodStatus":
                case "cacheApplicationsIcons":
                case "showInRecents":
                case "lesserToast":
                case "notificationBarFreezeImmediately":
                case "notificationBarDisableSlideOut":
                case "notificationBarDisableClickDisappear":
                case "onekeyFreezeWhenLockScreen":
                case "freezeOnceQuit":
                case "avoidFreezeForegroundApplications":
                case "tryToAvoidUpdateWhenUsing":
                case "avoidFreezeNotifyingApplications":
                case "openImmediately":
                case "openAndUFImmediately":
                case "shortcutAutoFUF":
                case "needConfirmWhenFreezeUseShortcutAutoFUF":
                case "openImmediatelyAfterUnfreezeUseShortcutAutoFUF":
                case "enableInstallPkgFunc":
                case "tryDelApkAfterInstalled":
                case "useForegroundService":
                case "debugModeEnabled":
                case "notAllowInstallWhenIsObsd":
                    importBooleanSharedPreference(
                            context, activity, generalSettingsBooleanJSONObject, defSP, s);
                    break;
                default:
                    break;
            }
        }
    }

    private static boolean importUriAutoAllowPkgsJSONArray(JSONObject jsonObject, AppPreferences ap) {
        JSONArray array = jsonObject.optJSONArray("uriAutoAllowPkgs_allows");
        if (array == null) {
            return false;
        }
        JSONObject jObj = array.optJSONObject(0);
        if (jObj == null) {
            return false;
        }
        return ap.put("uriAutoAllowPkgs_allows", jObj.optString("lists"));
    }

    private static boolean importInstallPkgsAutoAllowPkgsJSONArray(JSONObject jsonObject, AppPreferences ap) {
        JSONArray array = jsonObject.optJSONArray("installPkgs_autoAllowPkgs_allows");
        if (array == null) {
            return false;
        }
        JSONObject jObj = array.optJSONObject(0);
        if (jObj == null) {
            return false;
        }
        return ap.put("installPkgs_autoAllowPkgs_allows", jObj.optString("lists"));
    }

    public static void importContents(Context context, Activity activity, JSONObject jsonObject) {
        final SharedPreferences defSP = PreferenceManager.getDefaultSharedPreferences(context);
        final AppPreferences appPreferences = new AppPreferences(context);

        Iterator<String> jsonKeysIterator = jsonObject.keys();
        while (jsonKeysIterator.hasNext()) {
            switch (jsonKeysIterator.next()) {
                // 通用设置转入（更多设置 中的选项，不转移图标选择相关设置） 开始
                case "generalSettings_boolean":
                    importBooleanSharedPreferences(context, activity, jsonObject, defSP, appPreferences);
                    break;
                case "generalSettings_string":
                    importStringSharedPreferences(context, activity, jsonObject, defSP, appPreferences);
                    break;
                case "generalSettings_int":
                    importIntSharedPreferences(context, activity, jsonObject, defSP, appPreferences);
                    break;
                // 通用设置转出 结束
                // 一键冻结、一键解冻、离开冻结列表 开始
                case "oneKeyList":
                    importOneKeyLists(context, jsonObject, appPreferences);
                    break;
                // 一键冻结、一键解冻、离开冻结列表 结束
                // 计划任务 - 时间 开始
                case "userTimeScheduledTasks":
                    importUserTimeTasksJSONArray(activity, jsonObject);
                    break;
                // 计划任务 - 时间 结束
                // 计划任务 - 触发器 开始
                case "userTriggerScheduledTasks":
                    importUserTriggerTasksJSONArray(activity, jsonObject);
                    break;
                // 计划任务 - 触发器 结束
                // 用户自定分类（我的列表） 开始
                case "userDefinedCategories":
                    importUserDefinedCategoriesJSONArray(activity, jsonObject);
                    break;
                // 用户自定分类（我的列表） 结束
                // URI 请求白名单 开始
                case "uriAutoAllowPkgs_allows":
                    importUriAutoAllowPkgsJSONArray(jsonObject, appPreferences);
                    break;
                // URI 请求白名单 结束
                // 安装应用请求白名单 开始
                case "installPkgs_autoAllowPkgs_allows":
                    importInstallPkgsAutoAllowPkgsJSONArray(jsonObject, appPreferences);
                    break;
                // 安装应用请求白名单 结束
                default:
                    break;
            }
        }

    }

}
