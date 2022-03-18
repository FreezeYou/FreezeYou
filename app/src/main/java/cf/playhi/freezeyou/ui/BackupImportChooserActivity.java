package cf.playhi.freezeyou.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.adapter.BackupImportChooserActivitySwitchSimpleAdapter;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.storage.key.AbstractKey;
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys;
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys;
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys;
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys;
import cf.playhi.freezeyou.utils.BackupUtils;
import cf.playhi.freezeyou.utils.ToastUtils;

import static cf.playhi.freezeyou.utils.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;

public class BackupImportChooserActivity extends FreezeYouBaseActivity {

    private final HashMap<String, String> keyToStringIdValuePair = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getSupportActionBar());
        setContentView(R.layout.bica_main);

        onCreateInit();
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
        bicaCancelButton.setOnClickListener(v -> finish());
        bicaFinishButton.setOnClickListener(v -> {
            final ListView mainListView = findViewById(R.id.bica_main_listView);
            BackupImportChooserActivitySwitchSimpleAdapter adapter =
                    (BackupImportChooserActivitySwitchSimpleAdapter) mainListView.getAdapter();
            BackupUtils.importContents(
                    getApplicationContext(),
                    BackupImportChooserActivity.this, adapter.getFinalList());
            ToastUtils.showToast(BackupImportChooserActivity.this, R.string.finish);
            finish();
        });
    }

    private void generateKeyToStringIdValuePair() {
        // Int 开始
        keyToStringIdValuePair.put("onClickFunctionStatus", getString(R.string.onClickFunctionStatus));
        keyToStringIdValuePair.put("sortMethodStatus", getString(R.string.sortMethodStatus));
        // Int 结束
        // 一键冻结、一键解冻、离开冻结列表 开始
        keyToStringIdValuePair.put("okff", getString(R.string.oneKeyFreezeList));
        keyToStringIdValuePair.put("okuf", getString(R.string.oneKeyUFList));
        keyToStringIdValuePair.put("foq", getString(R.string.freezeOnceQuitList));
        // 一键冻结、一键解冻、离开冻结列表 结束
        // 安装应用请求、URI 请求白名单 开始
        keyToStringIdValuePair.put("uriAutoAllowPkgs_allows", getString(R.string.uriAutoAllowList));
        keyToStringIdValuePair.put("installPkgs_autoAllowPkgs_allows", getString(R.string.ipaAutoAllowList));
        // 安装应用请求、URI 请求白名单 结束
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
                // 用户自定分类（我的列表） 开始
                case "userDefinedCategories":
                    generateUserDefinedCategoriesList(jsonObject, list);
                    break;
                // 用户自定分类（我的列表） 结束
                // URI 请求白名单 开始
                case "uriAutoAllowPkgs_allows":
                    generateUriAutoAllowPkgsList(jsonObject, list);
                    break;
                // URI 请求白名单 结束
                // 安装应用请求白名单 开始
                case "installPkgs_autoAllowPkgs_allows":
                    generateInstallPkgsAutoAllowPkgsList(jsonObject, list);
                    break;
                // 安装应用请求白名单 结束
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

            if (s == null) continue;

            if (DefaultSharedPreferenceStorageBooleanKeys.firstIconEnabled.name().equals(s)
                    || DefaultSharedPreferenceStorageBooleanKeys.secondIconEnabled.name().equals(s)
                    || DefaultSharedPreferenceStorageBooleanKeys.thirdIconEnabled.name().equals(s)
                    || DefaultMultiProcessMMKVStorageBooleanKeys.enableAuthentication.name().equals(s)) {
                continue;
            }

            AbstractKey<Boolean> key = null;

            try {
                key = DefaultSharedPreferenceStorageBooleanKeys.valueOf(s);
            } catch (IllegalArgumentException ignored) {
            }

            if (key == null) {
                try {
                    key = DefaultMultiProcessMMKVStorageBooleanKeys.valueOf(s);
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (key == null) continue;

            HashMap<String, String> keyValuePair = new HashMap<>();
            keyValuePair.put(
                    "title",
                    String.format(moreSettingsDashLineLabel, getString(key.titleTextStringId()))
            );
            keyValuePair.put("spKey", s);
            keyValuePair.put("category", "generalSettings_boolean");
            list.add(keyValuePair);
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

            if (s == null) continue;

            AbstractKey<String> key = null;

            try {
                key = DefaultSharedPreferenceStorageStringKeys.valueOf(s);
            } catch (IllegalArgumentException ignored) {
            }

            if (key == null) {
                try {
                    key = DefaultMultiProcessMMKVStorageStringKeys.valueOf(s);
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (key == null) continue;

            HashMap<String, String> keyValuePair = new HashMap<>();
            keyValuePair.put(
                    "title",
                    String.format(moreSettingsDashLineLabel, getString(key.titleTextStringId()))
            );
            keyValuePair.put("spKey", s);
            keyValuePair.put("category", "generalSettings_string");
            list.add(keyValuePair);
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
            try {
                oneUserTimeScheduledTaskJSONObject.put("i", Integer.toString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            try {
                oneUserTriggerScheduledTaskJSONObject.put("i", Integer.toString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            keyValuePair.put("spKey", Integer.toString(i));
            keyValuePair.put("category", "userTriggerScheduledTasks");
            list.add(keyValuePair);
        }
    }

    private void generateUserDefinedCategoriesList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("userDefinedCategories");
        if (array == null) {
            return;
        }

        JSONObject oneUserDefinedCategoriesJSONObject;
        String myCustomizationDashLineLabel = getString(R.string.myCustomizationDashLineLabel);
        String defaultLabel = "";
        for (int i = 0; i < array.length(); ++i) {
            oneUserDefinedCategoriesJSONObject = array.optJSONObject(i);
            if (oneUserDefinedCategoriesJSONObject == null) {
                continue;
            }
            HashMap<String, String> keyValuePair = new HashMap<>();
            keyValuePair.put("title",
                    String.format(myCustomizationDashLineLabel,
                            new String(
                                    Base64.decode(
                                            oneUserDefinedCategoriesJSONObject.optString(
                                                    "label", defaultLabel),
                                            Base64.DEFAULT
                                    )
                            )
                    )
            );
            try {
                oneUserDefinedCategoriesJSONObject.put("i", Integer.toString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            keyValuePair.put("spKey", Integer.toString(i));
            keyValuePair.put("category", "userDefinedCategories");
            list.add(keyValuePair);
        }
    }

    private void generateUriAutoAllowPkgsList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("uriAutoAllowPkgs_allows");
        if (array == null) {
            return;
        }

        JSONObject jObj = array.optJSONObject(0);
        if (jObj == null) {
            return;
        }

        HashMap<String, String> keyValuePair = new HashMap<>();
        keyValuePair.put(
                "title",
                keyToStringIdValuePair.containsKey("uriAutoAllowPkgs_allows") ?
                        keyToStringIdValuePair.get("uriAutoAllowPkgs_allows") :
                        "uriAutoAllowPkgs_allows"
        );
        keyValuePair.put("spKey", "uriAutoAllowPkgs_allows");
        keyValuePair.put("category", "uriAutoAllowPkgs_allows");
        list.add(keyValuePair);
    }

    private void generateInstallPkgsAutoAllowPkgsList(JSONObject jsonObject, ArrayList<HashMap<String, String>> list) {
        JSONArray array = jsonObject.optJSONArray("installPkgs_autoAllowPkgs_allows");
        if (array == null) {
            return;
        }

        JSONObject jObj = array.optJSONObject(0);
        if (jObj == null) {
            return;
        }

        HashMap<String, String> keyValuePair = new HashMap<>();
        keyValuePair.put(
                "title",
                keyToStringIdValuePair.containsKey("installPkgs_autoAllowPkgs_allows") ?
                        keyToStringIdValuePair.get("installPkgs_autoAllowPkgs_allows") :
                        "installPkgs_autoAllowPkgs_allows"
        );
        keyValuePair.put("spKey", "installPkgs_autoAllowPkgs_allows");
        keyValuePair.put("category", "installPkgs_autoAllowPkgs_allows");
        list.add(keyValuePair);
    }

}
