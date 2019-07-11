package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class BackupImportChooserActivity extends Activity {

    HashMap<String, String> keyToStringIdValuePair = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getActionBar());
        setContentView(R.layout.bica_main);

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

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        final ListView mainListView = findViewById(R.id.bica_main_listView);
        final ArrayList<HashMap<String, String>> titleAndSpKeyArrayList = new ArrayList<>();

        generateKeyToStringIdValuePair();

        String jsonContentString = intent.getStringExtra("jsonObjectString");
        JSONObject jsonObject = null;
        if (jsonContentString == null) {
            HashMap<String, String> keyValuePair = new HashMap<>();
            keyValuePair.put("title", getString(R.string.failed));
            keyValuePair.put("spKey", "Failed!");
            keyValuePair.put("category", "Failed!");
            titleAndSpKeyArrayList.add(keyValuePair);
        } else {
            try {
                jsonObject = new JSONObject(jsonContentString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsonObject == null) {
                HashMap<String, String> keyValuePair = new HashMap<>();
                keyValuePair.put("title", getString(R.string.parseFailed));
                keyValuePair.put("spKey", "Failed!");
                keyValuePair.put("category", "Failed!");
                titleAndSpKeyArrayList.add(keyValuePair);
            } else {
                generateList(jsonObject, titleAndSpKeyArrayList);
                if (titleAndSpKeyArrayList.size() == 0) {
                    HashMap<String, String> keyValuePair = new HashMap<>();
                    keyValuePair.put("title", getString(R.string.nothing));
                    keyValuePair.put("spKey", "Failed!");
                    keyValuePair.put("category", "Failed!");
                    titleAndSpKeyArrayList.add(keyValuePair);
                }
            }
        }

        final BackupImportChooserActivitySwitchSimpleAdapter adapter =
                new BackupImportChooserActivitySwitchSimpleAdapter(
                        this,
                        jsonObject,
                        titleAndSpKeyArrayList,
                        R.layout.bica_list_item,
                        new String[]{"title"},
                        new int[]{R.id.bica_list_item_switch});

        mainListView.setAdapter(adapter);

        processButtons();
    }

    private void processButtons() {
        final Button bicaFinishButton = findViewById(R.id.bica_finish_button);
        final Button bicaCancelButton = findViewById(R.id.bica_cancel_button);
        bicaCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bicaFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListView mainListView = findViewById(R.id.bica_main_listView);
                BackupImportChooserActivitySwitchSimpleAdapter adapter =
                        (BackupImportChooserActivitySwitchSimpleAdapter) mainListView.getAdapter();
                if (BackupUtils.importContents(
                        getApplicationContext(),
                        BackupImportChooserActivity.this,
                        adapter.getFinalList())) {
                    ToastUtils.showToast(BackupImportChooserActivity.this, R.string.success);
                } else {
                    ToastUtils.showToast(BackupImportChooserActivity.this, R.string.failed);
                }
                finish();
            }
        });
    }

    private void generateKeyToStringIdValuePair() {
        // String 开始
        keyToStringIdValuePair.put("onClickFuncChooseActionStyle", getString(R.string.onClickFuncChooseActionStyle));
        keyToStringIdValuePair.put("uiStyleSelection", getString(R.string.uiStyle));
        keyToStringIdValuePair.put("launchMode", getString(R.string.launchMode));
        keyToStringIdValuePair.put("organizationName", getString(R.string.organizationName));
        keyToStringIdValuePair.put("shortCutOneKeyFreezeAdditionalOptions", getString(R.string.shortCutOneKeyFreezeAdditionalOptions));
        // String 结束
        // boolean 开始
        keyToStringIdValuePair.put("allowEditWhenCreateShortcut", getString(R.string.allowEditWhCreateShortcut));
        keyToStringIdValuePair.put("noCaution", getString(R.string.nSCaution));
        keyToStringIdValuePair.put("saveOnClickFunctionStatus", getString(R.string.saveOnClickFunctionStatus));
        keyToStringIdValuePair.put("saveSortMethodStatus", getString(R.string.saveSortMethodStatus));
        keyToStringIdValuePair.put("cacheApplicationsIcons", getString(R.string.cacheApplicationsIcons));
        keyToStringIdValuePair.put("showInRecents", getString(R.string.showInRecents));
        keyToStringIdValuePair.put("lesserToast", getString(R.string.lesserToast));
        keyToStringIdValuePair.put("notificationBarFreezeImmediately", getString(R.string.notificationBarFreezeImmediately));
        keyToStringIdValuePair.put("notificationBarDisableSlideOut", getString(R.string.disableSlideOut));
        keyToStringIdValuePair.put("notificationBarDisableClickDisappear", getString(R.string.disableClickDisappear));
        keyToStringIdValuePair.put("onekeyFreezeWhenLockScreen", getString(R.string.freezeAfterScreenLock));
        keyToStringIdValuePair.put("freezeOnceQuit", getString(R.string.freezeOnceQuit));
        keyToStringIdValuePair.put("avoidFreezeForegroundApplications", getString(R.string.avoidFreezeForegroundApplications));
        keyToStringIdValuePair.put("avoidFreezeNotifyingApplications", getString(R.string.avoidFreezeNotifyingApplications));
        keyToStringIdValuePair.put("openImmediately", getString(R.string.openImmediately));
        keyToStringIdValuePair.put("openAndUFImmediately", getString(R.string.openAndUFImmediately));
        keyToStringIdValuePair.put("shortcutAutoFUF", getString(R.string.shortcutAutoFUF));
        keyToStringIdValuePair.put("needConfirmWhenFreezeUseShortcutAutoFUF", getString(R.string.needCfmWhenFreeze));
        keyToStringIdValuePair.put("openImmediatelyAfterUnfreezeUseShortcutAutoFUF", getString(R.string.openImmediatelyAfterUF));
        keyToStringIdValuePair.put("enableInstallPkgFunc", getString(R.string.enableInstallPkgFunc));
        keyToStringIdValuePair.put("tryDelApkAfterInstalled", getString(R.string.tryDelApkAfterInstalled));
        keyToStringIdValuePair.put("useForegroundService", getString(R.string.useForegroundService));
        keyToStringIdValuePair.put("debugModeEnabled", getString(R.string.debugMode));
        // boolean 结束
        // Int 开始
        keyToStringIdValuePair.put("onClickFunctionStatus", getString(R.string.onClickFunctionStatus));
        keyToStringIdValuePair.put("sortMethodStatus", getString(R.string.sortMethodStatus));
        // Int 结束
        // 一键冻结、一键解冻、离开冻结列表 开始
        keyToStringIdValuePair.put("okff", getString(R.string.oneKeyFreezeList));
        keyToStringIdValuePair.put("okuf", getString(R.string.oneKeyUFList));
        keyToStringIdValuePair.put("foq", getString(R.string.freezeOnceQuitList));
        // 一键冻结、一键解冻、离开冻结列表 结束
    }

    private void generateList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {

        final Iterator<String> jsonKeysIterator = jsonObject.keys();
        while (jsonKeysIterator.hasNext()) {
            switch (jsonKeysIterator.next()) {
                // 通用设置转入（更多设置 中的选项，不转移图标选择相关设置） 开始
                case "generalSettings_boolean":
                    generateGeneralSettingsBooleanList(jsonObject, list);
                    break;
                case "generalSettings_string":
                    generateGeneralSettingsStringList(jsonObject, list);
                    break;
                case "generalSettings_int":
                    generateGeneralSettingsIntList(jsonObject, list);
                    break;
                // 通用设置转出 结束
                // 一键冻结、一键解冻、离开冻结列表 开始
                case "oneKeyList":
                    generateOneKeyList(jsonObject, list);
                    break;
                // 一键冻结、一键解冻、离开冻结列表 结束
                // 计划任务 - 时间 开始
                case "userTimeScheduledTasks":
                    generateUserTimeScheduledTasksList(jsonObject, list);
                    break;
                // 计划任务 - 时间 结束
                // 计划任务 - 触发器 开始
                case "userTriggerScheduledTasks":
                    generateUserTriggerScheduledTasksList(jsonObject, list);
                    break;
                // 计划任务 - 触发器 结束
                default:
                    break;
            }
        }
    }

    private void generateGeneralSettingsBooleanList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("generalSettings_boolean");
        if (array == null) {
            return;
        }
        JSONObject generalSettingsBooleanJSONObject = array.optJSONObject(0);
        if (generalSettingsBooleanJSONObject == null) {
            return;
        }
        Iterator<String> it = generalSettingsBooleanJSONObject.keys();
        String moreSettingsDashLineLabel = getString(R.string.moreSettingsDashLineLabel);
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
                    HashMap<String, String> keyValuePair = new HashMap<>();
                    keyValuePair.put("title",
                            String.format(moreSettingsDashLineLabel,
                                    keyToStringIdValuePair.containsKey(s) ?
                                            keyToStringIdValuePair.get(s) : s));
                    keyValuePair.put("spKey", s);
                    keyValuePair.put("category", "generalSettings_boolean");
                    list.add(keyValuePair);
                    break;
                default:
                    break;
            }
        }
    }

    private void generateGeneralSettingsIntList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("generalSettings_int");
        if (array == null) {
            return;
        }
        JSONObject generalSettingsIntJSONObject = array.optJSONObject(0);
        if (generalSettingsIntJSONObject == null) {
            return;
        }
        Iterator<String> it = generalSettingsIntJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "onClickFunctionStatus":
                case "sortMethodStatus":
                    HashMap<String, String> keyValuePair = new HashMap<>();
                    keyValuePair.put("title",
                            keyToStringIdValuePair.containsKey(s) ?
                                    keyToStringIdValuePair.get(s) : s);
                    keyValuePair.put("spKey", s);
                    keyValuePair.put("category", "generalSettings_int");
                    list.add(keyValuePair);
                    break;
                default:
                    break;
            }
        }
    }

    private void generateGeneralSettingsStringList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("generalSettings_string");
        if (array == null) {
            return;
        }
        JSONObject generalSettingsStringJSONObject = array.optJSONObject(0);
        if (generalSettingsStringJSONObject == null) {
            return;
        }
        String moreSettingsDashLineLabel = getString(R.string.moreSettingsDashLineLabel);
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
                    HashMap<String, String> keyValuePair = new HashMap<>();
                    keyValuePair.put("title",
                            String.format(moreSettingsDashLineLabel,
                                    keyToStringIdValuePair.containsKey(s) ?
                                            keyToStringIdValuePair.get(s) : s));
                    keyValuePair.put("spKey", s);
                    keyValuePair.put("category", "generalSettings_string");
                    list.add(keyValuePair);
                    break;
                default:
                    break;
            }
        }
    }

    private void generateOneKeyList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("oneKeyList");
        if (array == null) {
            return;
        }
        JSONObject generalOneKeyJSONObject = array.optJSONObject(0);
        if (generalOneKeyJSONObject == null) {
            return;
        }
        Iterator<String> it = generalOneKeyJSONObject.keys();
        String s;
        while (it.hasNext()) {
            s = it.next();
            switch (s) {
                case "okff":
                case "okuf":
                case "foq":
                    HashMap<String, String> keyValuePair = new HashMap<>();
                    keyValuePair.put("title",
                            keyToStringIdValuePair.containsKey(s) ?
                                    keyToStringIdValuePair.get(s) : s);
                    keyValuePair.put("spKey", s);
                    keyValuePair.put("category", "oneKeyList");
                    list.add(keyValuePair);
                    break;
                default:
                    break;
            }
        }
    }

    private void generateUserTimeScheduledTasksList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("userTimeScheduledTasks");
        if (array == null) {
            return;
        }

        JSONObject oneUserTimeScheduledTaskJSONObject;
        String scheduledTaskDashLineLabel = getString(R.string.scheduledTaskDashLineLabel);
        String defaultLabel = getString(R.string.label);
        for (int i = 0; i < array.length(); ++i) {
            oneUserTimeScheduledTaskJSONObject = array.optJSONObject(i);
            if (oneUserTimeScheduledTaskJSONObject == null) {
                continue;
            }
            HashMap<String, String> keyValuePair = new HashMap<>();
            keyValuePair.put("title",
                    String.format(scheduledTaskDashLineLabel,
                            oneUserTimeScheduledTaskJSONObject.optString("label", defaultLabel)));
            keyValuePair.put("spKey", Integer.toString(i));
            keyValuePair.put("category", "userTimeScheduledTasks");
            list.add(keyValuePair);
        }
    }

    private void generateUserTriggerScheduledTasksList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("userTriggerScheduledTasks");
        if (array == null) {
            return;
        }

        JSONObject oneUserTriggerScheduledTaskJSONObject;
        String scheduledTaskDashLineLabel = getString(R.string.scheduledTaskDashLineLabel);
        String defaultLabel = getString(R.string.label);
        for (int i = 0; i < array.length(); ++i) {
            oneUserTriggerScheduledTaskJSONObject = array.optJSONObject(i);
            if (oneUserTriggerScheduledTaskJSONObject == null) {
                continue;
            }
            HashMap<String, String> keyValuePair = new HashMap<>();
            keyValuePair.put("title",
                    String.format(scheduledTaskDashLineLabel,
                            oneUserTriggerScheduledTaskJSONObject.optString("label", defaultLabel)));
            keyValuePair.put("spKey", Integer.toString(i));
            keyValuePair.put("category", "userTriggerScheduledTasks");
            list.add(keyValuePair);
        }
    }

}
