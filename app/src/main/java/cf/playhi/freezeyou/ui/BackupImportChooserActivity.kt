package cf.playhi.freezeyou.ui

import android.os.Bundle
import android.util.Base64
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys
import cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageStringKeys
import cf.playhi.freezeyou.utils.BackupUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import org.json.JSONException
import org.json.JSONObject

class BackupImportChooserActivity : FreezeYouBaseActivity() {
    private val keyToStringIdValuePair = HashMap<String, String>()
    private val selectedItems = mutableStateMapOf<Int, Boolean>()
    private var importObject: JSONObject? = null
    private var importItems: List<MutableMap<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        onCreateInit()
    }

    private fun onCreateInit() {
        val intent = intent
        if (intent == null) {
            finish()
            return
        }
        val titleAndSpKeyArrayList = ArrayList<MutableMap<String, String>>()
        generateKeyToStringIdValuePair()
        val jsonContentString = intent.getStringExtra("jsonObjectString")
        var jsonObject: JSONObject? = null
        if (jsonContentString == null) {
            val keyValuePair = HashMap<String, String>()
            keyValuePair["title"] = getString(R.string.failed)
            keyValuePair["spKey"] = "Failed!"
            keyValuePair["category"] = "Failed!"
            titleAndSpKeyArrayList.add(keyValuePair)
        } else {
            try {
                jsonObject = JSONObject(jsonContentString)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (jsonObject == null) {
                val keyValuePair = HashMap<String, String>()
                keyValuePair["title"] = getString(R.string.parseFailed)
                keyValuePair["spKey"] = "Failed!"
                keyValuePair["category"] = "Failed!"
                titleAndSpKeyArrayList.add(keyValuePair)
            } else {
                generateList(jsonObject, titleAndSpKeyArrayList)
                if (titleAndSpKeyArrayList.size == 0) {
                    val keyValuePair = HashMap<String, String>()
                    keyValuePair["title"] = getString(R.string.nothing)
                    keyValuePair["spKey"] = "Failed!"
                    keyValuePair["category"] = "Failed!"
                    titleAndSpKeyArrayList.add(keyValuePair)
                }
            }
        }
        importObject = jsonObject?.let { JSONObject(it.toString()) }
        importItems = titleAndSpKeyArrayList
        titleAndSpKeyArrayList.indices.forEach { selectedItems[it] = true }
        setContent {
            FreezeYouTheme {
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                        itemsIndexed(importItems) { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item["title"].orEmpty(), Modifier.weight(1f))
                                Switch(
                                    checked = selectedItems[index] != false,
                                    enabled = item["category"] != "Failed!",
                                    onCheckedChange = { selectedItems[index] = it }
                                )
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(10.dp)) {
                        Button(onClick = { finish() }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(
                            onClick = {
                                BackupUtils.importContents(
                                    applicationContext,
                                    this@BackupImportChooserActivity,
                                    getFinalImportObject()
                                )
                                ToastUtils.showToast(this@BackupImportChooserActivity, R.string.finish)
                                finish()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.finish))
                        }
                    }
                }
            }
        }
    }

    private fun getFinalImportObject(): JSONObject {
        val jsonObject = importObject ?: return JSONObject()
        importItems.forEachIndexed { index, itemData ->
            if (selectedItems[index] != false) return@forEachIndexed
            val category = itemData["category"] ?: return@forEachIndexed
            val key = itemData["spKey"] ?: return@forEachIndexed
            val array = jsonObject.optJSONArray(category) ?: return@forEachIndexed
            when (category) {
                "generalSettings_boolean", "generalSettings_string",
                "generalSettings_int", "oneKeyList" -> array.optJSONObject(0)?.remove(key)
                "userTimeScheduledTasks", "userTriggerScheduledTasks",
                "userDefinedCategories" -> {
                    for (arrayIndex in 0 until array.length()) {
                        val value = array.optJSONObject(arrayIndex) ?: continue
                        if (key == value.optString("i", "-1")) value.put("doNotImport", true)
                    }
                }
                "uriAutoAllowPkgs_allows", "installPkgs_autoAllowPkgs_allows" -> {
                    jsonObject.remove(category)
                }
            }
        }
        return jsonObject
    }

    private fun generateKeyToStringIdValuePair() {
        // Int 开始
        keyToStringIdValuePair["onClickFunctionStatus"] = getString(R.string.onClickFunctionStatus)
        keyToStringIdValuePair["sortMethodStatus"] = getString(R.string.sortMethodStatus)
        // Int 结束
        // 一键冻结、一键解冻、离开冻结列表 开始
        keyToStringIdValuePair["okff"] = getString(R.string.oneKeyFreezeList)
        keyToStringIdValuePair["okuf"] = getString(R.string.oneKeyUFList)
        keyToStringIdValuePair["foq"] = getString(R.string.freezeOnceQuitList)
        // 一键冻结、一键解冻、离开冻结列表 结束
        // 安装应用请求、URI 请求白名单 开始
        keyToStringIdValuePair["uriAutoAllowPkgs_allows"] = getString(R.string.uriAutoAllowList)
        keyToStringIdValuePair["installPkgs_autoAllowPkgs_allows"] =
            getString(R.string.ipaAutoAllowList)
        // 安装应用请求、URI 请求白名单 结束
    }

    private fun generateList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val jsonKeysIterator = jsonObject.keys()
        while (jsonKeysIterator.hasNext()) {
            when (jsonKeysIterator.next()) {
                "generalSettings_boolean" -> generateGeneralSettingsBooleanList(jsonObject, list)
                "generalSettings_string" -> generateGeneralSettingsStringList(jsonObject, list)
                "generalSettings_int" -> generateGeneralSettingsIntList(jsonObject, list)
                "oneKeyList" -> generateOneKeyList(jsonObject, list)
                "userTimeScheduledTasks" -> generateUserTimeScheduledTasksList(jsonObject, list)
                "userTriggerScheduledTasks" -> generateUserTriggerScheduledTasksList(
                    jsonObject,
                    list
                )
                "userDefinedCategories" -> generateUserDefinedCategoriesList(jsonObject, list)
                "uriAutoAllowPkgs_allows" -> generateUriAutoAllowPkgsList(jsonObject, list)
                "installPkgs_autoAllowPkgs_allows" -> generateInstallPkgsAutoAllowPkgsList(
                    jsonObject,
                    list
                )
                else -> {}
            }
        }
    }

    private fun generateGeneralSettingsBooleanList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("generalSettings_boolean") ?: return
        val generalSettingsBooleanJSONObject = array.optJSONObject(0) ?: return
        val it = generalSettingsBooleanJSONObject.keys()
        val moreSettingsDashLineLabel = getString(R.string.moreSettingsDashLineLabel)
        var s: String?
        while (it.hasNext()) {
            s = it.next() ?: continue
            if (DefaultSharedPreferenceStorageBooleanKeys.firstIconEnabled.name == s || DefaultSharedPreferenceStorageBooleanKeys.secondIconEnabled.name == s || DefaultSharedPreferenceStorageBooleanKeys.thirdIconEnabled.name == s || DefaultMultiProcessMMKVStorageBooleanKeys.enableAuthentication.name == s) {
                continue
            }
            var key: cf.playhi.freezeyou.storage.key.AbstractKey<Boolean>? = null
            try {
                key = DefaultSharedPreferenceStorageBooleanKeys.valueOf(s)
            } catch (ignored: IllegalArgumentException) {
            }
            if (key == null) {
                try {
                    key = DefaultMultiProcessMMKVStorageBooleanKeys.valueOf(s)
                } catch (ignored: IllegalArgumentException) {
                }
            }
            if (key == null) continue
            val keyValuePair = HashMap<String, String>()
            keyValuePair["title"] = String.format(moreSettingsDashLineLabel, getString(key.titleTextStringId()))
            keyValuePair["spKey"] = s
            keyValuePair["category"] = "generalSettings_boolean"
            list.add(keyValuePair)
        }
    }

    private fun generateGeneralSettingsIntList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("generalSettings_int") ?: return
        val generalSettingsIntJSONObject = array.optJSONObject(0) ?: return
        val it = generalSettingsIntJSONObject.keys()
        var s: String
        while (it.hasNext()) {
            s = it.next()
            when (s) {
                "onClickFunctionStatus", "sortMethodStatus" -> {
                    val keyValuePair = HashMap<String, String>()
                    keyValuePair["title"] =
                        if (keyToStringIdValuePair.containsKey(s)) keyToStringIdValuePair[s]!! else s
                    keyValuePair["spKey"] = s
                    keyValuePair["category"] = "generalSettings_int"
                    list.add(keyValuePair)
                }
                else -> {}
            }
        }
    }

    private fun generateGeneralSettingsStringList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("generalSettings_string") ?: return
        val generalSettingsStringJSONObject = array.optJSONObject(0) ?: return
        val moreSettingsDashLineLabel = getString(R.string.moreSettingsDashLineLabel)
        val it = generalSettingsStringJSONObject.keys()
        var s: String?
        while (it.hasNext()) {
            s = it.next() ?: continue
            var key: cf.playhi.freezeyou.storage.key.AbstractKey<String?>? = null
            try {
                key = DefaultSharedPreferenceStorageStringKeys.valueOf(s)
            } catch (ignored: IllegalArgumentException) {
            }
            if (key == null) {
                try {
                    key = DefaultMultiProcessMMKVStorageStringKeys.valueOf(s)
                } catch (ignored: IllegalArgumentException) {
                }
            }
            if (key == null) continue
            val keyValuePair = HashMap<String, String>()
            keyValuePair["title"] = String.format(moreSettingsDashLineLabel, getString(key.titleTextStringId()))
            keyValuePair["spKey"] = s
            keyValuePair["category"] = "generalSettings_string"
            list.add(keyValuePair)
        }
    }

    private fun generateOneKeyList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("oneKeyList") ?: return
        val generalOneKeyJSONObject = array.optJSONObject(0) ?: return
        val it = generalOneKeyJSONObject.keys()
        var s: String
        while (it.hasNext()) {
            s = it.next()
            when (s) {
                "okff", "okuf", "foq" -> {
                    val keyValuePair = HashMap<String, String>()
                    keyValuePair["title"] =
                        if (keyToStringIdValuePair.containsKey(s)) keyToStringIdValuePair[s]!! else s
                    keyValuePair["spKey"] = s
                    keyValuePair["category"] = "oneKeyList"
                    list.add(keyValuePair)
                }
                else -> {}
            }
        }
    }

    private fun generateUserTimeScheduledTasksList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("userTimeScheduledTasks") ?: return
        var oneUserTimeScheduledTaskJSONObject: JSONObject?
        val scheduledTaskDashLineLabel = getString(R.string.scheduledTaskDashLineLabel)
        val defaultLabel = getString(R.string.label)
        for (i in 0 until array.length()) {
            oneUserTimeScheduledTaskJSONObject = array.optJSONObject(i)
            if (oneUserTimeScheduledTaskJSONObject == null) {
                continue
            }
            val keyValuePair = HashMap<String, String>()
            keyValuePair["title"] = String.format(
                scheduledTaskDashLineLabel,
                oneUserTimeScheduledTaskJSONObject.optString("label", defaultLabel)
            )
            try {
                oneUserTimeScheduledTaskJSONObject.put("i", i.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            keyValuePair["spKey"] = i.toString()
            keyValuePair["category"] = "userTimeScheduledTasks"
            list.add(keyValuePair)
        }
    }

    private fun generateUserTriggerScheduledTasksList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("userTriggerScheduledTasks") ?: return
        var oneUserTriggerScheduledTaskJSONObject: JSONObject?
        val scheduledTaskDashLineLabel = getString(R.string.scheduledTaskDashLineLabel)
        val defaultLabel = getString(R.string.label)
        for (i in 0 until array.length()) {
            oneUserTriggerScheduledTaskJSONObject = array.optJSONObject(i)
            if (oneUserTriggerScheduledTaskJSONObject == null) {
                continue
            }
            val keyValuePair = HashMap<String, String>()
            keyValuePair["title"] = String.format(
                scheduledTaskDashLineLabel,
                oneUserTriggerScheduledTaskJSONObject.optString("label", defaultLabel)
            )
            try {
                oneUserTriggerScheduledTaskJSONObject.put("i", i.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            keyValuePair["spKey"] = i.toString()
            keyValuePair["category"] = "userTriggerScheduledTasks"
            list.add(keyValuePair)
        }
    }

    private fun generateUserDefinedCategoriesList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("userDefinedCategories") ?: return
        var oneUserDefinedCategoriesJSONObject: JSONObject?
        val myCustomizationDashLineLabel = getString(R.string.myCustomizationDashLineLabel)
        val defaultLabel = ""
        for (i in 0 until array.length()) {
            oneUserDefinedCategoriesJSONObject = array.optJSONObject(i)
            if (oneUserDefinedCategoriesJSONObject == null) {
                continue
            }
            val keyValuePair = HashMap<String, String>()
            keyValuePair["title"] = String.format(
                myCustomizationDashLineLabel,
                String(
                    Base64.decode(
                        oneUserDefinedCategoriesJSONObject.optString(
                            "label", defaultLabel
                        ),
                        Base64.DEFAULT
                    )
                )
            )
            try {
                oneUserDefinedCategoriesJSONObject.put("i", i.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            keyValuePair["spKey"] = i.toString()
            keyValuePair["category"] = "userDefinedCategories"
            list.add(keyValuePair)
        }
    }

    private fun generateUriAutoAllowPkgsList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("uriAutoAllowPkgs_allows") ?: return
        val jObj = array.optJSONObject(0) ?: return
        val keyValuePair = HashMap<String, String>()
        keyValuePair["title"] = if (keyToStringIdValuePair.containsKey("uriAutoAllowPkgs_allows"))
            keyToStringIdValuePair["uriAutoAllowPkgs_allows"]!!
        else
            "uriAutoAllowPkgs_allows"
        keyValuePair["spKey"] = "uriAutoAllowPkgs_allows"
        keyValuePair["category"] = "uriAutoAllowPkgs_allows"
        list.add(keyValuePair)
    }

    private fun generateInstallPkgsAutoAllowPkgsList(
        jsonObject: JSONObject,
        list: ArrayList<MutableMap<String, String>>
    ) {
        val array = jsonObject.optJSONArray("installPkgs_autoAllowPkgs_allows") ?: return
        val jObj = array.optJSONObject(0) ?: return
        val keyValuePair = HashMap<String, String>()
        keyValuePair["title"] =
            if (keyToStringIdValuePair.containsKey("installPkgs_autoAllowPkgs_allows"))
                keyToStringIdValuePair["installPkgs_autoAllowPkgs_allows"]!!
            else
                "installPkgs_autoAllowPkgs_allows"
        keyValuePair["spKey"] = "installPkgs_autoAllowPkgs_allows"
        keyValuePair["category"] = "installPkgs_autoAllowPkgs_allows"
        list.add(keyValuePair)
    }
}
