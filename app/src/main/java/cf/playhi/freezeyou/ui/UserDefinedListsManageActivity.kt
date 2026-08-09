package cf.playhi.freezeyou.ui

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilder
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.ClipboardUtils.copyToClipboard
import cf.playhi.freezeyou.utils.GZipUtils
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class UserDefinedListsManageActivity : FreezeYouBaseActivity() {
    private data class UserList(val id: Int, val title: String, val packages: String)

    private var lists by mutableStateOf<List<UserList>>(emptyList())
    private var expandedId by mutableIntStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        loadUserDefinedLists()
        setContent {
            FreezeYouTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(lists, key = { it.id }) { item ->
                        Box {
                            Column(
                                Modifier.fillMaxWidth()
                                    .clickable { expandedId = item.id }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(item.title)
                                Text(item.packages)
                            }
                            DropdownMenu(
                                expanded = expandedId == item.id,
                                onDismissRequest = { expandedId = -1 }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.copyAlias)) },
                                    onClick = { expandedId = -1; copyId(item) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.share)) },
                                    onClick = { expandedId = -1; share(item) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete)) },
                                    onClick = { expandedId = -1; confirmDelete(item) }
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    private fun loadUserDefinedLists() {
        val database = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null)
        database.execSQL(
            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        )
        val cursor = database.query(
            "categories", arrayOf("label", "_id", "packages"),
            null, null, null, null, null
        )
        val result = mutableListOf<UserList>()
        while (cursor.moveToNext()) {
            result += UserList(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                title = String(
                    Base64.decode(cursor.getString(cursor.getColumnIndexOrThrow("label")), Base64.DEFAULT)
                ),
                packages = cursor.getString(cursor.getColumnIndexOrThrow("packages"))
            )
        }
        cursor.close()
        database.close()
        lists = result
    }

    private fun copyId(item: UserList) {
        val copied = copyToClipboard(
            applicationContext,
            Base64.encodeToString(item.title.toByteArray(), Base64.DEFAULT)
        )
        showToast(applicationContext, if (copied) R.string.success else R.string.failed)
    }

    private fun share(item: UserList) {
        try {
            val category = JSONObject().apply {
                put("label", Base64.encodeToString(item.title.toByteArray(), Base64.DEFAULT))
                put("packages", item.packages)
            }
            val output = JSONObject().put("userDefinedCategories", JSONArray().put(category))
            val shareIntent = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, GZipUtils.gzipCompress(output.toString()))
                },
                getString(R.string.share)
            )
            startActivity(shareIntent)
        } catch (exception: JSONException) {
            exception.printStackTrace()
            showToast(this, R.string.failed)
        }
    }

    private fun confirmDelete(item: UserList) {
        FreezeYouAlertDialogBuilder(this)
            .setTitle(R.string.plsConfirm)
            .setMessage(R.string.askIfDel)
            .setPositiveButton(R.string.yes) { _, _ -> deleteUserDefinedListById(item.id) }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun deleteUserDefinedListById(id: Int) {
        val database = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null)
        database.execSQL(
            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        )
        database.delete("categories", "_id = ?", arrayOf(id.toString()))
        database.close()
        loadUserDefinedLists()
    }
}
