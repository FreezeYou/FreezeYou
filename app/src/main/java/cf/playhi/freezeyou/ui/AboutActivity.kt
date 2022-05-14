package cf.playhi.freezeyou.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.MoreUtils.joinQQGroup
import cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import cf.playhi.freezeyou.utils.VersionUtils.*

class AboutActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)
        processActionBar(supportActionBar)

        val aboutSlogan = findViewById<TextView>(R.id.about_slogan)
        val aboutListView = findViewById<ListView>(R.id.about_listView)
        val aboutAppName = findViewById<TextView>(R.id.about_appName)

        aboutListView.adapter =
            ArrayAdapter(
                this@AboutActivity,
                android.R.layout.simple_list_item_1,
                arrayOf(
                    resources.getString(R.string.hToUse),
                    resources.getString(R.string.faq),
                    resources.getString(R.string.helpTranslate),
                    resources.getString(R.string.thanksList),
                    resources.getString(R.string.visitWebsite),
                    resources.getString(R.string.contactUs),
                    resources.getString(R.string.update),
                    resources.getString(R.string.thirdPartyOpenSourceLicenses),
                    "V${getVersionName(applicationContext)}(${getVersionCode(applicationContext)})"
                )
            )

        aboutListView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                when (position) {
                    0 -> requestOpenWebSite(
                        this@AboutActivity,
                        "https://www.zidon.net/${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/guide/how-to-use.html"
                    )
                    1 -> requestOpenWebSite(
                        this@AboutActivity, String.format(
                            "https://www.zidon.net/%1\$s/faq/",
                            getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                        )
                    )
                    2 -> requestOpenWebSite(
                        this@AboutActivity,
                        "https://github.com/FreezeYou/FreezeYou/blob/master/README_Translation.md"
                    )
                    3 -> requestOpenWebSite(
                        this@AboutActivity, String.format(
                            "https://www.zidon.net/%1\$s/thanks/",
                            getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                        )
                    )
                    4 -> requestOpenWebSite(this@AboutActivity, "https://www.zidon.net")
                    5 -> {
                        AlertDialog.Builder(this@AboutActivity)
                            .setMessage(
                                String.format(
                                    getString(R.string.email_colon),
                                    "contact@zidon.net"
                                ) + System.getProperty("line.separator")
                                        + String.format(
                                    getString(R.string.telegramGroup_colon),
                                    "t.me/FreezeYou"
                                ) + System.getProperty("line.separator")
                                        + String.format(
                                    getString(R.string.qqGroup_colon),
                                    "704086494"
                                )
                            )
                            .setTitle(R.string.contactUs)
                            .setPositiveButton(R.string.okay, null)
                            .setNegativeButton(
                                R.string.addQQGroup
                            ) { _: DialogInterface?, _: Int ->
                                joinQQGroup(this@AboutActivity)
                            }
                            .setNeutralButton(
                                R.string.more
                            ) { _: DialogInterface?, _: Int ->
                                requestOpenWebSite(
                                    this@AboutActivity,
                                    String.format(
                                        "https://www.zidon.net/%1\$s/about/contactUs.html",
                                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                                    )
                                )
                            }
                            .show()
                    }
                    6 -> checkUpdate(this@AboutActivity)
                    7 -> requestOpenWebSite(
                        this@AboutActivity,
                        "https://freezeyou.playhi.net/ThirdPartyOpenSourceLicenses.html"
                    )
                    8 -> showToast(
                        this@AboutActivity,
                        "V" + getVersionName(this@AboutActivity) + "(" + getVersionCode(
                            this@AboutActivity
                        ) + ")"
                    )
                }
            }

        aboutSlogan.text = String.format("V %s", getVersionCode(this@AboutActivity))
        aboutSlogan.setOnClickListener {
            requestOpenWebSite(
                this@AboutActivity, String.format(
                    "https://www.zidon.net/%1\$s/changelog/",
                    getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)
                )
            )
        }

        aboutAppName.setOnClickListener {
            AlertDialog.Builder(this@AboutActivity)
                .setTitle(
                    String.format(
                        getString(R.string.welcomeToUseAppName),
                        getString(R.string.app_name)
                    )
                )
                .setIcon(R.mipmap.ic_launcher_new_round)
                .setMessage(
                    String.format(
                        getString(R.string.welcomeToUseAppName),
                        getString(R.string.app_name)
                    )
                )
                .setNegativeButton(
                    R.string.importConfig
                ) { _: DialogInterface?, _: Int ->
                    startActivity(
                        Intent(applicationContext, BackupMainActivity::class.java)
                    )
                }
                .setPositiveButton(
                    R.string.quickSetup
                ) { _: DialogInterface?, _: Int ->
                    startActivity(
                        Intent(applicationContext, FirstTimeSetupActivity::class.java)
                    )
                }
                .setNeutralButton(R.string.okay, null)
                .show()
        }
    }
}