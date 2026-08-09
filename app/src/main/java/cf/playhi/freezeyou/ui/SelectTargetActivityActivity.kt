package cf.playhi.freezeyou.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable
import cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.ui.compose.AppListItem
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import java.util.*

class SelectTargetActivityActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        init()
    }

    private fun init() {
        val arrayList = ArrayList<MutableMap<String, Any?>>()
        val intent = intent
        if (intent == null) {
            finish()
        } else {
            val pkgName = intent.getStringExtra("pkgName")
            if (pkgName == null) {
                finish()
            } else {
                val hm: MutableMap<String, Any?> = HashMap()
                hm["Img"] = getApplicationIcon(
                    this,
                    pkgName,
                    getApplicationInfoFromPkgName(pkgName, this),
                    false
                )
                hm["Name"] = getString(R.string.launch)
                hm["Label"] = getApplicationLabel(
                    this, packageManager,
                    getApplicationInfoFromPkgName(pkgName, this), pkgName
                )
                arrayList.add(hm)
                val hm2: MutableMap<String, Any?> = HashMap()
                hm2["Img"] = getApplicationIcon(
                    this,
                    pkgName,
                    getApplicationInfoFromPkgName(pkgName, this),
                    false
                )
                hm2["Name"] = getString(R.string.onlyUnfreeze)
                hm2["Label"] = getApplicationLabel(
                    this, packageManager,
                    getApplicationInfoFromPkgName(pkgName, this), pkgName
                )
                arrayList.add(hm2)
                try {
                    val pm = packageManager
                    val activityInfos =
                        pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES).activities
                    if (activityInfos != null) {
                        for (activityInfo in activityInfos) {
                            val ais = activityInfo.name
                            if (ais != null && activityInfo.exported) {
                                val hashMap: MutableMap<String, Any?> = HashMap()
                                hashMap["Img"] = activityInfo.loadIcon(pm)
                                hashMap["Name"] = ais
                                hashMap["Label"] = activityInfo.loadLabel(pm).toString()
                                arrayList.add(hashMap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                setContent {
                    FreezeYouTheme {
                        LazyColumn {
                            items(arrayList) { item ->
                                val name = item["Name"] as String? ?: ""
                                val label = item["Label"] as String? ?: ""
                                val drawable = item["Img"] as Drawable?
                                AppListItem(drawable, label, name) {
                                    val icon = drawable?.let(::getBitmapFromDrawable)
                                    setResult(
                                        RESULT_OK,
                                        Intent()
                                            .putExtra("name", name)
                                            .putExtra("icon", icon)
                                            .putExtra("label", label)
                                            .putExtra("id", "FreezeYou!$pkgName $name")
                                    )
                                    finish()
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
