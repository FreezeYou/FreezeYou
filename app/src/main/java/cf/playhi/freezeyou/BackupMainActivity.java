package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class BackupMainActivity extends Activity {

//    Camera mCamera = null; 先把文本方式稳定下来，再做 QRCode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getActionBar());
        setContentView(R.layout.bma_main);

        onCreateInit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onCreateInit() {
        initButtons();
    }

    private void initButtons() {
        Button bma_main_export_button = findViewById(R.id.bma_main_export_button);
        Button bma_main_import_button = findViewById(R.id.bma_main_import_button);
        Button bma_main_copy_button = findViewById(R.id.bma_main_copy_button);
        Button bma_main_paste_button = findViewById(R.id.bma_main_paste_button);

        bma_main_export_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
                editText.setText(processExportContent());
                editText.selectAll();
            }
        });

        bma_main_import_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
                if (processImportContent(GZipUtils.gzipDecompress(editText.getText().toString()))) {
                    ToastUtils.showToast(BackupMainActivity.this, R.string.success);
                } else {
                    ToastUtils.showToast(BackupMainActivity.this, R.string.failed);
                }
            }
        });

        bma_main_copy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
                MoreUtils.copyToClipboard(getApplicationContext(), editText.getText().toString());
            }
        });

        bma_main_paste_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
                editText.setText(MoreUtils.getClipboardItemText(getApplicationContext()));
            }
        });

    }

    private boolean processImportContent(String jsonContent) {
        final SharedPreferences defSP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final AppPreferences appPreferences = new AppPreferences(getApplicationContext());
        try {
            JSONObject jsonObject = new JSONObject(jsonContent);

            // 通用设置转入（更多设置 中的选项，不转移图标选择相关设置） 开始

            // boolean 开始
            JSONObject generalSettingsBooleanJSONObject =
                    jsonObject.getJSONArray("generalSettings_boolean").getJSONObject(0);

            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "allowEditWhenCreateShortcut");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "noCaution");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "saveOnClickFunctionStatus");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "cacheApplicationsIcons");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "showInRecents");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "lesserToast");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "notificationBarFreezeImmediately");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "notificationBarDisableSlideOut");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "notificationBarDisableClickDisappear");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "onekeyFreezeWhenLockScreen");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "freezeOnceQuit");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "avoidFreezeForegroundApplications");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "avoidFreezeNotifyingApplications");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "openImmediately");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "openAndUFImmediately");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "shortcutAutoFUF");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "needConfirmWhenFreezeUseShortcutAutoFUF");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "openImmediatelyAfterUnfreezeUseShortcutAutoFUF");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "enableInstallPkgFunc");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "tryDelApkAfterInstalled");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "useForegroundService");
            importBooleanSharedPreference(
                    generalSettingsBooleanJSONObject, defSP, appPreferences, "debugModeEnabled");
            // boolean 结束

            // String 开始
            JSONObject generalSettingsStringJSONObject =
                    jsonObject.getJSONArray("generalSettings_string").getJSONObject(0);
            importStringSharedPreference(
                    generalSettingsStringJSONObject, defSP, appPreferences, "onClickFuncChooseActionStyle");
            importStringSharedPreference(
                    generalSettingsStringJSONObject, defSP, appPreferences, "uiStyleSelection");
            importStringSharedPreference(
                    generalSettingsStringJSONObject, defSP, appPreferences, "launchMode");
            importStringSharedPreference(
                    generalSettingsStringJSONObject, defSP, appPreferences, "organizationName");
            importStringSharedPreference(
                    generalSettingsStringJSONObject, defSP, appPreferences, "shortCutOneKeyFreezeAdditionalOptions");
            // String 结束

            // Int 开始
            JSONObject generalSettingsIntJSONObject =
                    jsonObject.getJSONArray("generalSettings_int").getJSONObject(0);
            importIntSharedPreference(
                    generalSettingsIntJSONObject, defSP, appPreferences, "onClickFunctionStatus");
            // Int 结束

            // 通用设置转出 结束

            // 一键冻结、一键解冻、离开冻结列表 开始
            JSONObject oneKeyListJSONObject =
                    jsonObject.getJSONArray("oneKeyList").getJSONObject(0);
            appPreferences.put(
                    getString(R.string.sAutoFreezeApplicationList),
                    oneKeyListJSONObject.getString("okff"));
            appPreferences.put(
                    getString(R.string.sOneKeyUFApplicationList),
                    oneKeyListJSONObject.getString("okuf"));
            appPreferences.put(
                    getString(R.string.sFreezeOnceQuit),
                    oneKeyListJSONObject.getString("foq"));
            // 一键冻结、一键解冻、离开冻结列表 结束

            // 计划任务 - 时间 开始
            importUserTimeTasksJSONArray(jsonObject);
            // 计划任务 - 时间 结束

            // 计划任务 - 触发器 开始
            importUserTriggerTasksJSONArray(jsonObject);
            // 计划任务 - 触发器 结束

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String processExportContent() {
        final AppPreferences appPreferences = new AppPreferences(getApplicationContext());
        final SharedPreferences defSP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        JSONObject finalOutputJsonObject = new JSONObject();

        try {
            // 标记转出格式版本 开始
            JSONArray formatVersionJSONArray = new JSONArray();
            JSONObject formatVersionJSONObject = new JSONObject();
            formatVersionJSONObject.put("version", 1); // 标记备份文件格式版本
            formatVersionJSONObject.put("generateTime", new Date().getTime()); // 标记备份文件格式版本
            formatVersionJSONArray.put(formatVersionJSONObject);
            finalOutputJsonObject.put("format_version", formatVersionJSONArray);
            // 标记转出格式版本 结束

            // 通用设置转出（更多设置 中的选项，不转移图标选择相关设置） 开始

            // boolean 开始
            JSONArray generalSettingsBooleanJSONArray = new JSONArray();
            JSONObject generalSettingsBooleanJSONObject = new JSONObject();
            generalSettingsBooleanJSONObject.put(
                    "allowEditWhenCreateShortcut",
                    convertSharedPreference(defSP, "allowEditWhenCreateShortcut", true)
            );
            generalSettingsBooleanJSONObject.put(
                    "noCaution",
                    convertSharedPreference(defSP, "noCaution", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "saveOnClickFunctionStatus",
                    convertSharedPreference(defSP, "saveOnClickFunctionStatus", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "cacheApplicationsIcons",
                    convertSharedPreference(defSP, "cacheApplicationsIcons", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "showInRecents",
                    convertSharedPreference(appPreferences, "showInRecents", true)
            );
            generalSettingsBooleanJSONObject.put(
                    "lesserToast",
                    convertSharedPreference(appPreferences, "lesserToast", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "notificationBarFreezeImmediately",
                    convertSharedPreference(appPreferences, "notificationBarFreezeImmediately", true)
            );
            generalSettingsBooleanJSONObject.put(
                    "notificationBarDisableSlideOut",
                    convertSharedPreference(appPreferences, "notificationBarDisableSlideOut", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "notificationBarDisableClickDisappear",
                    convertSharedPreference(appPreferences, "notificationBarDisableClickDisappear", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "onekeyFreezeWhenLockScreen",
                    convertSharedPreference(appPreferences, "onekeyFreezeWhenLockScreen", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "freezeOnceQuit",
                    convertSharedPreference(appPreferences, "freezeOnceQuit", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "avoidFreezeForegroundApplications",
                    convertSharedPreference(appPreferences, "avoidFreezeForegroundApplications", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "avoidFreezeNotifyingApplications",
                    convertSharedPreference(appPreferences, "avoidFreezeNotifyingApplications", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "openImmediately",
                    convertSharedPreference(appPreferences, "openImmediately", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "openAndUFImmediately",
                    convertSharedPreference(appPreferences, "openAndUFImmediately", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "shortcutAutoFUF",
                    convertSharedPreference(defSP, "shortcutAutoFUF", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "needConfirmWhenFreezeUseShortcutAutoFUF",
                    convertSharedPreference(defSP, "needConfirmWhenFreezeUseShortcutAutoFUF", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "openImmediatelyAfterUnfreezeUseShortcutAutoFUF",
                    convertSharedPreference(defSP, "openImmediatelyAfterUnfreezeUseShortcutAutoFUF", true)
            );
            generalSettingsBooleanJSONObject.put(
                    "enableInstallPkgFunc",
                    convertSharedPreference(defSP, "enableInstallPkgFunc", true)
            );
            generalSettingsBooleanJSONObject.put(
                    "tryDelApkAfterInstalled",
                    convertSharedPreference(appPreferences, "tryDelApkAfterInstalled", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "useForegroundService",
                    convertSharedPreference(appPreferences, "useForegroundService", false)
            );
            generalSettingsBooleanJSONObject.put(
                    "debugModeEnabled",
                    convertSharedPreference(appPreferences, "debugModeEnabled", false)
            );
            generalSettingsBooleanJSONArray.put(generalSettingsBooleanJSONObject);
            finalOutputJsonObject.put("generalSettings_boolean", generalSettingsBooleanJSONArray);
            // boolean 结束

            // String 开始
            JSONArray generalSettingsStringJSONArray = new JSONArray();
            JSONObject generalSettingsStringJSONObject = new JSONObject();
            generalSettingsStringJSONObject.put(
                    "onClickFuncChooseActionStyle",
                    convertSharedPreference(defSP, "onClickFuncChooseActionStyle", "1")
            );
            generalSettingsStringJSONObject.put(
                    "uiStyleSelection",
                    convertSharedPreference(defSP, "uiStyleSelection", "default")
            );
            generalSettingsStringJSONObject.put(
                    "launchMode",
                    convertSharedPreference(defSP, "launchMode", "all")
            );
            generalSettingsStringJSONObject.put(
                    "organizationName",
                    convertSharedPreference(defSP, "organizationName", getString(R.string.app_name))
            );
            generalSettingsStringJSONObject.put(
                    "shortCutOneKeyFreezeAdditionalOptions",
                    convertSharedPreference(appPreferences, "shortCutOneKeyFreezeAdditionalOptions", "nothing")
            );
            generalSettingsStringJSONArray.put(generalSettingsStringJSONObject);
            finalOutputJsonObject.put("generalSettings_string", generalSettingsStringJSONArray);
            // String 结束

            // Int 开始
            JSONArray generalSettingsIntJSONArray = new JSONArray();
            JSONObject generalSettingsIntJSONObject = new JSONObject();
            generalSettingsIntJSONObject.put(
                    "onClickFunctionStatus",
                    defSP.getInt("onClickFunctionStatus", 0)
            );
            generalSettingsIntJSONArray.put(generalSettingsIntJSONObject);
            finalOutputJsonObject.put("generalSettings_int", generalSettingsIntJSONArray);
            // Int 结束

            // 通用设置转出 结束

            // 一键冻结、一键解冻、离开冻结列表 开始
            JSONArray oneKeyListJSONArray = new JSONArray();
            JSONObject oneKeyListJSONObject = new JSONObject();
            oneKeyListJSONObject.put(
                    "okff",
                    appPreferences.getString(getString(R.string.sAutoFreezeApplicationList), "")
            );
            oneKeyListJSONObject.put(
                    "okuf",
                    appPreferences.getString(getString(R.string.sOneKeyUFApplicationList), "")
            );
            oneKeyListJSONObject.put(
                    "foq",
                    appPreferences.getString(getString(R.string.sFreezeOnceQuit), "")
            );
            oneKeyListJSONArray.put(oneKeyListJSONObject);
            finalOutputJsonObject.put("oneKeyList", oneKeyListJSONArray);
            // 一键冻结、一键解冻、离开冻结列表 结束

            // 计划任务 - 时间 开始
            finalOutputJsonObject.put("userTimeScheduledTasks", generateUserTimeTasksJSONArray());
            // 计划任务 - 时间 结束

            // 计划任务 - 触发器 开始
            finalOutputJsonObject.put("userTriggerScheduledTasks", generateUserTriggerTasksJSONArray());
            // 计划任务 - 触发器 结束

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return GZipUtils.gzipCompress(finalOutputJsonObject.toString());
    }

    private JSONArray generateUserTimeTasksJSONArray() throws JSONException {
        JSONArray userTimeScheduledTasksJSONArray = new JSONArray();
        SQLiteDatabase db = openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        Cursor cursor =
                db.query("tasks", null, null, null,
                        null, null, null);
        if (cursor.moveToFirst()) {
            boolean ifContinue = true;
            while (ifContinue) {
                JSONObject oneUserTimeScheduledTaskJSONObject = new JSONObject();
                oneUserTimeScheduledTaskJSONObject.put("hour",
                        cursor.getInt(cursor.getColumnIndex("hour")));
                oneUserTimeScheduledTaskJSONObject.put("minutes",
                        cursor.getInt(cursor.getColumnIndex("minutes")));
                oneUserTimeScheduledTaskJSONObject.put("enabled",
                        cursor.getInt(cursor.getColumnIndex("enabled")));
                oneUserTimeScheduledTaskJSONObject.put("label",
                        cursor.getString(cursor.getColumnIndex("label")));
                oneUserTimeScheduledTaskJSONObject.put("task",
                        cursor.getString(cursor.getColumnIndex("task")));
                oneUserTimeScheduledTaskJSONObject.put("repeat",
                        cursor.getString(cursor.getColumnIndex("repeat")));
                userTimeScheduledTasksJSONArray.put(oneUserTimeScheduledTaskJSONObject);
                ifContinue = cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return userTimeScheduledTasksJSONArray;
    }

    private JSONArray generateUserTriggerTasksJSONArray() throws JSONException {
        JSONArray userTriggerScheduledTasksJSONArray = new JSONArray();
        SQLiteDatabase db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        Cursor cursor =
                db.query("tasks", null, null, null,
                        null, null, null);
        if (cursor.moveToFirst()) {
            boolean ifContinue = true;
            while (ifContinue) {
                JSONObject oneUserTriggerScheduledTaskJSONObject = new JSONObject();
                oneUserTriggerScheduledTaskJSONObject.put("tgextra",
                        cursor.getString(cursor.getColumnIndex("tgextra")));
                oneUserTriggerScheduledTaskJSONObject.put("enabled",
                        cursor.getInt(cursor.getColumnIndex("enabled")));
                oneUserTriggerScheduledTaskJSONObject.put("label",
                        cursor.getString(cursor.getColumnIndex("label")));
                oneUserTriggerScheduledTaskJSONObject.put("task",
                        cursor.getString(cursor.getColumnIndex("task")));
                oneUserTriggerScheduledTaskJSONObject.put("tg",
                        cursor.getString(cursor.getColumnIndex("tg")));
                userTriggerScheduledTasksJSONArray.put(oneUserTriggerScheduledTaskJSONObject);
                ifContinue = cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return userTriggerScheduledTasksJSONArray;
    }

    private void importUserTimeTasksJSONArray(JSONObject jsonObject) throws JSONException {
        JSONArray userTimeScheduledTasksJSONArray =
                jsonObject.getJSONArray("userTimeScheduledTasks");
        SQLiteDatabase db = openOrCreateDatabase("scheduledTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,hour integer(2),minutes integer(2),repeat varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        JSONObject oneUserTimeScheduledTaskJSONObject;
        for (int i = 0; i < userTimeScheduledTasksJSONArray.length(); ++i) {
            oneUserTimeScheduledTaskJSONObject = userTimeScheduledTasksJSONArray.getJSONObject(i);
            db.execSQL(
                    "insert into tasks(_id,hour,minutes,repeat,enabled,label,task,column1,column2) values(null,"
                            + oneUserTimeScheduledTaskJSONObject.getInt("hour") + ","
                            + oneUserTimeScheduledTaskJSONObject.getInt("minutes") + ","
                            + oneUserTimeScheduledTaskJSONObject.getString("repeat") + ","
                            + oneUserTimeScheduledTaskJSONObject.getInt("enabled") + ","
                            + "'" + oneUserTimeScheduledTaskJSONObject.getString("label") + "'" + ","
                            + "'" + oneUserTimeScheduledTaskJSONObject.getString("task") + "'" + ",'','')"
            );
        }
        db.close();
        TasksUtils.checkTimeTasks(this);
    }

    private void importUserTriggerTasksJSONArray(JSONObject jsonObject) throws JSONException {
        JSONArray userTriggerScheduledTasksJSONArray =
                jsonObject.getJSONArray("userTriggerScheduledTasks");
        SQLiteDatabase db = openOrCreateDatabase("scheduledTriggerTasks", MODE_PRIVATE, null);
        db.execSQL(
                "create table if not exists tasks(_id integer primary key autoincrement,tg varchar,tgextra varchar,enabled integer(1),label varchar,task varchar,column1 varchar,column2 varchar)"
        );
        JSONObject oneUserTriggerScheduledTaskJSONObject;
        for (int i = 0; i < userTriggerScheduledTasksJSONArray.length(); ++i) {
            oneUserTriggerScheduledTaskJSONObject = userTriggerScheduledTasksJSONArray.getJSONObject(i);
            db.execSQL(
                    "insert into tasks(_id,tg,tgextra,enabled,label,task,column1,column2) VALUES (null,"
                            + "'" + oneUserTriggerScheduledTaskJSONObject.getString("tg") + "'" + ","
                            + "'" + oneUserTriggerScheduledTaskJSONObject.getString("tgextra") + "'" + ","
                            + oneUserTriggerScheduledTaskJSONObject.getInt("enabled") + ","
                            + "'" + oneUserTriggerScheduledTaskJSONObject.getString("label") + "'" + ","
                            + "'" + oneUserTriggerScheduledTaskJSONObject.getString("task") + "'" + ",'','')"
            );
        }
        db.close();
        TasksUtils.checkTriggerTasks(this);
    }

    private String convertSharedPreference(
            SharedPreferences sharedPreferences, String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    private String convertSharedPreference(
            AppPreferences appPreferences, String key, String defValue) {
        return appPreferences.getString(key, defValue);
    }

    private boolean convertSharedPreference(
            SharedPreferences sharedPreferences, String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    private boolean convertSharedPreference(
            AppPreferences appPreferences, String key, boolean defValue) {
        return appPreferences.getBoolean(key, defValue);
    }

    private void importStringSharedPreference(
            JSONObject jsonObject, SharedPreferences sp, AppPreferences ap, String key) throws JSONException {
        sp.edit().putString(key, jsonObject.getString(key)).apply();
        SettingsUtils.syncAndCheckSharedPreference(getApplicationContext(), this, sp, key, ap);
    }

    private void importBooleanSharedPreference(
            JSONObject jsonObject, SharedPreferences sp, AppPreferences ap, String key) throws JSONException {
        sp.edit().putBoolean(key, jsonObject.getBoolean(key)).apply();
        SettingsUtils.syncAndCheckSharedPreference(getApplicationContext(), this, sp, key, ap);
    }

    private void importIntSharedPreference(
            JSONObject jsonObject, SharedPreferences sp, AppPreferences ap, String key) throws JSONException {
        sp.edit().putInt(key, jsonObject.getInt(key)).apply();
        SettingsUtils.syncAndCheckSharedPreference(getApplicationContext(), this, sp, key, ap);
    }

//    先把文本方式稳定下来，再决定是否做 QRCode
//    private void processQRCodeAndQRCodeImageView(String qrContent) {
//        ImageView bma_main_qrCode_imageView = findViewById(R.id.bma_main_qrCode_imageView);
//        FrameLayout bma_main_qrCode_frameLayout = findViewById(R.id.bma_main_qrCode_frameLayout);
//
//        int width = getWindowManager().getDefaultDisplay().getWidth();
//        if (width <= 0) {
//            width = 300;
//        }
//
//        int wh = (int) (width * 0.6);
//        int padding = (width - wh) / 2;
//
//        int frontColor;
//        if (Build.VERSION.SDK_INT < 21) {
//            frontColor = Color.BLACK;
//        } else {
//            // https://stackoverflow.com/questions/27611173/how-to-get-accent-color-programmatically
//            TypedValue typedValue = new TypedValue();
//            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.colorAccent});
//            frontColor = a.getColor(0, 0);
//            a.recycle();
//        }
//
//        final Bitmap qrCode = QRCodeUtil.createQRCodeBitmap(
//                qrContent == null ? "" : qrContent,
//                wh, wh, "L", "1", frontColor, Color.TRANSPARENT);
//
//        bma_main_qrCode_imageView.setImageBitmap(qrCode);
//
//        bma_main_qrCode_frameLayout.setPadding(padding, padding, padding, padding);
//        bma_main_qrCode_frameLayout.setMinimumWidth(wh);
//        bma_main_qrCode_frameLayout.setMinimumHeight(wh);
//
//        bma_main_qrCode_imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String imgPath = getCacheDir() + File.separator + new Date().toString();
//                ApplicationIconUtils.writeBitmapToFile(imgPath, qrCode);
//                startActivity(
//                        new Intent(BackupMainActivity.this, FullScreenImageViewerActivity.class)
//                                .putExtra("imgPath", imgPath)
//                );
//            }
//        });
//    }
//
//    // https://developer.android.google.cn/guide/topics/media/camera.html
//    private boolean checkCameraHardware(Context context) {
//        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
//    }
//
//    // https://developer.android.google.cn/guide/topics/media/camera.html
//
//    /**
//     * A safe way to get an instance of the Camera object.
//     */
//    public static Camera getCameraInstance() {
//        Camera c = null;
//        try {
//            c = Camera.open(); // attempt to get a Camera instance
//        } catch (Exception e) {
//            // Camera is not available (in use or does not exist)
//        }
//        return c; // returns null if camera is unavailable
//    }
//
//}
//
//
//class QRCodeUtil {
//    //部分来自 https://www.jianshu.com/p/b275e818de6a
//
//    /**
//     * 创建二维码位图
//     *
//     * @param content 字符串内容(支持中文)
//     * @param width   位图宽度(单位:px)
//     * @param height  位图高度(单位:px)
//     * @return Bitmap
//     */
//    static Bitmap createQRCodeBitmap(String content, int width, int height) {
//        return createQRCodeBitmap(content, width, height, "UTF-8", "M", "1", Color.BLACK, Color.WHITE);
//    }
//
//    /**
//     * @param content    字符串内容(支持中文)
//     * @param width      位图宽度(单位:px)
//     * @param height     位图高度(单位:px)
//     * @param ec         容错级别
//     * @param margin     空白边距
//     * @param front      前景色的自定义颜色值
//     * @param background 背景色的自定义颜色值
//     * @return Bitmap
//     */
//    static Bitmap createQRCodeBitmap(String content, int width, int height, String ec, String margin, int front, int background) {
//        return createQRCodeBitmap(content, width, height, "UTF-8", ec, margin, front, background);
//    }
//
//    /**
//     * 创建二维码位图 (支持自定义配置和自定义样式)
//     *
//     * @param content          字符串内容
//     * @param width            位图宽度,要求>=0(单位:px)
//     * @param height           位图高度,要求>=0(单位:px)
//     * @param character_set    字符集/字符转码格式。传null时,zxing源码默认使用 "ISO-8859-1"
//     * @param error_correction 容错级别。传null时,zxing源码默认使用 "L"
//     * @param margin           空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"。
//     * @param color_black      黑色色块的自定义颜色值
//     * @param color_white      白色色块的自定义颜色值
//     * @return Bitmap
//     */
//    private static Bitmap createQRCodeBitmap(String content, int width, int height,
//                                             String character_set, String error_correction, String margin,
//                                             int color_black, int color_white) {
//
//        /* 1.参数合法性判断 */
//        if (TextUtils.isEmpty(content)) { // 字符串内容判空
//            return null;
//        }
//
//        if (width < 0 || height < 0) { // 宽和高都需要>=0
//            return null;
//        }
//
//        try {
//            /* 2.设置二维码相关配置,生成BitMatrix(位矩阵)对象 */
//            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
//
//            if (!TextUtils.isEmpty(character_set)) {
//                hints.put(EncodeHintType.CHARACTER_SET, character_set); // 字符转码格式设置
//            }
//
//            if (!TextUtils.isEmpty(error_correction)) {
//                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction); // 容错级别设置
//            }
//
//            if (!TextUtils.isEmpty(margin)) {
//                hints.put(EncodeHintType.MARGIN, margin); // 空白边距设置
//            }
//            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
//
//            /* 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
//            int[] pixels = new int[width * height];
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    if (bitMatrix.get(x, y)) {
//                        pixels[y * width + x] = color_black; // 黑色色块像素设置
//                    } else {
//                        pixels[y * width + x] = color_white; // 白色色块像素设置
//                    }
//                }
//            }
//
//            /* 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象 */
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//            return bitmap;
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    static String decodeQRCodeBitmap(Bitmap qrCode) {
//
//        try {
//
//            int width = qrCode.getWidth();
//            int height = qrCode.getHeight();
//
//            int[] qrCodeImgData = new int[width * height];
//            qrCode.getPixels(qrCodeImgData, 0, width, 0, 0, width, height);
//
//            RGBLuminanceSource rgbLuminanceSource =
//                    new RGBLuminanceSource(width, height, qrCodeImgData);
//
//            Result qrCodeReaderResult =
//                    new QRCodeReader().decode(
//                            new BinaryBitmap(
//                                    new HybridBinarizer(rgbLuminanceSource)
//                            )
//                    );
//
//            return qrCodeReaderResult.getText();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return "";
//    }
}

class GZipUtils {
    //参考 http://www.cnblogs.com/whoislcj/p/5473806.html

    /**
     * @param unGzipStr 被压缩字符串
     * @return 压缩后字符串，失败返回 String s=""
     */
    static String gzipCompress(String unGzipStr) {

        if (TextUtils.isEmpty(unGzipStr)) {
            return "";
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream);
            gzip.write(unGzipStr.getBytes());
            gzip.close();
            byte[] b = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return Base64.encodeToString(b, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * @param gzipStr 已压缩过的 String
     * @return 解压缩后的 String
     */
    static String gzipDecompress(String gzipStr) {
        if (TextUtils.isEmpty(gzipStr)) {
            return "";
        }
        try {
            byte[] t = Base64.decode(gzipStr, Base64.DEFAULT);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(t);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
