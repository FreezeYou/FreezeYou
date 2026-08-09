package cf.playhi.freezeyou.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilder
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.MoreUtils.joinQQGroup
import cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import cf.playhi.freezeyou.utils.VersionUtils

class AboutActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        setContent { FreezeYouTheme { AboutScreen() } }
    }

    @Composable
    private fun AboutScreen() {
        val entries = listOf(
            stringResource(R.string.hToUse),
            stringResource(R.string.faq),
            stringResource(R.string.helpTranslate),
            stringResource(R.string.thanksList),
            stringResource(R.string.visitWebsite),
            stringResource(R.string.contactUs),
            stringResource(R.string.update),
            stringResource(R.string.thirdPartyOpenSourceLicenses),
            "V${VersionUtils.getVersionName(applicationContext)}" +
                "(${VersionUtils.getVersionCode(applicationContext)})"
        )
        Column(Modifier.fillMaxSize().padding(5.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable { showWelcomeDialog() }
                )
                Text(
                    text = "V ${VersionUtils.getVersionCode(this@AboutActivity)}",
                    color = androidx.compose.ui.graphics.Color.Gray,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable {
                        requestOpenWebSite(
                            this@AboutActivity,
                            "https://www.zidon.net/" +
                                "${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/changelog/"
                        )
                    }
                )
            }
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(entries) { index, label ->
                    Text(
                        text = label,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onEntryClick(index) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    private fun onEntryClick(position: Int) {
        when (position) {
            0 -> requestOpenWebSite(
                this,
                "https://www.zidon.net/${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/guide/how-to-use.html"
            )
            1 -> requestOpenWebSite(
                this,
                "https://www.zidon.net/${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/faq/"
            )
            2 -> requestOpenWebSite(
                this,
                "https://github.com/FreezeYou/FreezeYou/blob/master/README_Translation.md"
            )
            3 -> requestOpenWebSite(
                this,
                "https://www.zidon.net/${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/thanks/"
            )
            4 -> requestOpenWebSite(this, "https://www.zidon.net")
            5 -> showContactDialog()
            6 -> VersionUtils.checkUpdate(this)
            7 -> requestOpenWebSite(
                this,
                "https://freezeyou.playhi.net/ThirdPartyOpenSourceLicenses.html"
            )
            8 -> showToast(
                this,
                "V${VersionUtils.getVersionName(this)}(${VersionUtils.getVersionCode(this)})"
            )
        }
    }

    private fun showContactDialog() {
        val newline = System.lineSeparator()
        FreezeYouAlertDialogBuilder(this)
            .setMessage(
                String.format(getString(R.string.email_colon), "contact@zidon.net") + newline +
                    String.format(getString(R.string.telegramGroup_colon), "t.me/FreezeYou") + newline +
                    String.format(getString(R.string.qqGroup_colon), "704086494")
            )
            .setTitle(R.string.contactUs)
            .setPositiveButton(R.string.okay, null)
            .setNegativeButton(R.string.addQQGroup) { _: DialogInterface?, _: Int ->
                joinQQGroup(this)
            }
            .setNeutralButton(R.string.more) { _: DialogInterface?, _: Int ->
                requestOpenWebSite(
                    this,
                    "https://www.zidon.net/${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/about/contactUs.html"
                )
            }
            .show()
    }

    private fun showWelcomeDialog() {
        FreezeYouAlertDialogBuilder(this)
            .setTitle(String.format(getString(R.string.welcomeToUseAppName), getString(R.string.app_name)))
            .setIcon(R.mipmap.ic_launcher_new_round)
            .setMessage(String.format(getString(R.string.welcomeToUseAppName), getString(R.string.app_name)))
            .setNegativeButton(R.string.importConfig) { _: DialogInterface?, _: Int ->
                startActivity(Intent(applicationContext, BackupMainActivity::class.java))
            }
            .setPositiveButton(R.string.quickSetup) { _: DialogInterface?, _: Int ->
                startActivity(Intent(applicationContext, FirstTimeSetupActivity::class.java))
            }
            .setNeutralButton(R.string.okay, null)
            .show()
    }
}
