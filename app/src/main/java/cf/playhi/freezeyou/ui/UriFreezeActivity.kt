package cf.playhi.freezeyou.ui

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.CheckBox
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.app.ObsdAlertDialog
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.notAllowInstallWhenIsObsd
import cf.playhi.freezeyou.utils.*
import cf.playhi.freezeyou.ui.compose.alwaysAllowView
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import net.grandcentrix.tray.AppPreferences
import java.util.*

class UriFreezeActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this, true)
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    private fun init() {
        val intent = intent
        if (intent != null) {
            if ("freezeyou" == intent.scheme) {
                val dataUri = intent.data
                if (dataUri != null) {
                    var action = ""
                    var pkgName = ""
                    try {
                        action = dataUri.getQueryParameter("action") ?: ""
                    } catch (ignored: NullPointerException) {
                    }
                    if (action.isEmpty()) action = "fuf"
                    try {
                        pkgName = dataUri.getQueryParameter("pkgName") ?: ""
                    } catch (ignored: NullPointerException) {
                    }
                    if (pkgName.isEmpty()) {
                        finish()
                        return
                    }
                    when (action.lowercase(Locale.getDefault())) {
                        "freeze" -> checkAndCreateUserCheckDialog(intent, pkgName, MODE_FREEZE)
                        "unfreeze" -> checkAndCreateUserCheckDialog(intent, pkgName, MODE_UNFREEZE)
                        "unfreezeandrun" ->  //unFreezeAndRun
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_UNFREEZEANDRUN)
                        "fuf" -> checkAndCreateUserCheckDialog(intent, pkgName, MODE_FUF)
                        else ->  //按照 fuf 方案执行
                            checkAndCreateUserCheckDialog(intent, pkgName, MODE_FUF)
                    }
                } else {
                    finish()
                }
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private fun checkAndCreateUserCheckDialog(intent: Intent, pkgName: String, mode: Int) {
        val suitableForAutoAllow = mode != MODE_FUF
        val isFrozen = FUFUtils.realGetFrozenStatus(this, pkgName, packageManager)
        val obsdAlertDialog = ObsdAlertDialog(this)
        var autoAllowChecked = false
        var refererPackageLabel: String
        val refererPackage: String
        @Suppress("DEPRECATION")
        if (intent.getParcelableExtra<Uri>(Intent.EXTRA_REFERRER) == null
            && intent.getStringExtra(Intent.EXTRA_REFERRER_NAME) == null
        ) {
            val referrerUri = referrer
            if (referrerUri != null && "android-app" == referrerUri.scheme) {
                refererPackage = referrerUri.encodedSchemeSpecificPart.substring(2)
                refererPackageLabel = getApplicationLabel(
                    this@UriFreezeActivity,
                    null,
                    ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, this),
                    refererPackage
                )
                if (refererPackageLabel == getString(R.string.uninstalled)) {
                    refererPackageLabel = ILLEGALPKGNAME
                }
            } else {
                refererPackageLabel = ILLEGALPKGNAME
                refererPackage = ILLEGALPKGNAME
            }
        } else {
            refererPackageLabel = ILLEGALPKGNAME
            refererPackage = ILLEGALPKGNAME
        }
        if (suitableForAutoAllow) {
            //Check AutoAllow
            val sp = AppPreferences(this)
            val originData = sp.getString("uriAutoAllowPkgs_allows", "")
            if (originData != null && ILLEGALPKGNAME != refererPackage && MoreUtils.convertToList(
                    originData,
                    ","
                )
                    .contains(
                        Base64.encodeToString(
                            refererPackage.toByteArray(),
                            Base64.DEFAULT
                        )
                    )
            ) {
                doSuitableForAutoAllowAllow(mode, pkgName, isFrozen)
            }
            if (refererPackageLabel != ILLEGALPKGNAME) {
                obsdAlertDialog.setView(
                    alwaysAllowView(
                        this,
                        String.format(getString(R.string.alwaysAllow_name), refererPackageLabel)
                    ) { autoAllowChecked = it }
                )
            }
        }
        obsdAlertDialog.setTitle(R.string.plsConfirm)
        val message = StringBuilder()
        val nl = System.getProperty("line.separator")
        message.append(getString(R.string.target_colon))
        message.append(nl)
        message.append(
            String.format(
                getString(R.string.application_colon_app),
                getApplicationLabel(
                    this, null,
                    null, pkgName
                )
            )
        )
        message.append(nl)
        message.append(
            String.format(
                getString(R.string.pkgName_colon_pkgName),
                pkgName
            )
        )
        message.append(nl)
        message.append(nl)
        message.append(getString(R.string.execute_colon))
        message.append(nl)
        when (mode) {
            MODE_FREEZE -> message.append(getString(R.string.freeze))
            MODE_UNFREEZE -> message.append(getString(R.string.unfreeze))
            MODE_UNFREEZEANDRUN -> message.append(getString(R.string.openImmediatelyAfterUF))
            else -> message.append(getString(R.string.plsSelect))
        }
        obsdAlertDialog.setMessage(message)
        obsdAlertDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(
                if (suitableForAutoAllow) R.string.allow else if (isFrozen) R.string.unfreeze else R.string.launch
            )
        ) { dialog, _ ->
            if ((dialog as ObsdAlertDialog).isObsd()) {
                AlertDialogUtils.buildAlertDialog(
                    this@UriFreezeActivity,
                    R.drawable.ic_warning,
                    R.string.alert_isObsd,
                    R.string.dangerous
                )
                    .setNegativeButton(R.string.cancel) { _, _ -> finish() }
                    .setPositiveButton(R.string.retry) { _, _ ->
                        checkAndCreateUserCheckDialog(intent, pkgName, mode)
                    }
                    .create().show()
            } else {
                if (autoAllowChecked) {
                    val sp = AppPreferences(this@UriFreezeActivity)
                    val originData = sp.getString("uriAutoAllowPkgs_allows", "")
                    val originDataList = MoreUtils.convertToList(originData, ",")
                    if (ILLEGALPKGNAME != refererPackage
                        && (originData == null || !originDataList.contains(
                            Base64.encodeToString(
                                refererPackage.toByteArray(), Base64.DEFAULT
                            )
                        ))
                    ) {
                        originDataList.add(
                            Base64.encodeToString(
                                refererPackage.toByteArray(),
                                Base64.DEFAULT
                            )
                        )
                        sp.put(
                            "uriAutoAllowPkgs_allows",
                            MoreUtils.listToString(originDataList, ",")
                        )
                    }
                }
                if (suitableForAutoAllow) {
                    doSuitableForAutoAllowAllow(mode, pkgName, isFrozen)
                } else {
                    if (isFrozen) {
                        FUFUtils.processUnfreezeAction(
                            this@UriFreezeActivity,
                            pkgName,
                            null,
                            null,
                            true,
                            false,
                            this@UriFreezeActivity,
                            true
                        )
                    } else {
                        FUFUtils.checkAndStartApp(
                            this@UriFreezeActivity,
                            pkgName,
                            null,
                            null,
                            this@UriFreezeActivity,
                            true
                        )
                    }
                }
            }
        }
        obsdAlertDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(
                if (suitableForAutoAllow) R.string.reject else R.string.freeze
            )
        ) { _, _ ->
            if (suitableForAutoAllow) {
                finish()
            } else {
                FUFUtils.processFreezeAction(
                    this@UriFreezeActivity,
                    pkgName,
                    null,
                    null,
                    false,
                    this@UriFreezeActivity,
                    true
                )
            }
        }
        obsdAlertDialog.setButton(
            DialogInterface.BUTTON_NEUTRAL, getString(R.string.cancel)
        ) { _, _ -> finish() }
        obsdAlertDialog.setOnCancelListener { finish() }
        obsdAlertDialog.show()
        val w = obsdAlertDialog.window
        if (w != null) {
            val v = w.findViewById<View>(android.R.id.custom)
            if (v != null) {
                val p = v.parent as View?
                p?.minimumHeight = 0
            }
        }
    }

    private fun doSuitableForAutoAllowAllow(mode: Int, pkgName: String, isFrozen: Boolean) {
        when (mode) {
            MODE_FREEZE -> if (!isFrozen) FUFUtils.processFreezeAction(
                this@UriFreezeActivity,
                pkgName,
                null,
                null,
                false,
                this@UriFreezeActivity,
                true
            ) else finish()
            MODE_UNFREEZE -> if (isFrozen) FUFUtils.processUnfreezeAction(
                this@UriFreezeActivity,
                pkgName,
                null,
                null,
                false,
                false,
                this@UriFreezeActivity,
                true
            ) else finish()
            MODE_UNFREEZEANDRUN -> if (isFrozen) FUFUtils.processUnfreezeAction(
                this@UriFreezeActivity,
                pkgName,
                null,
                null,
                true,
                true,
                this@UriFreezeActivity,
                true
            ) else FUFUtils.checkAndStartApp(
                this,
                pkgName,
                null,
                null,
                this,
                true
            )
            else -> finish()
        }
    }

    companion object {
        private const val MODE_FREEZE = 11
        private const val MODE_UNFREEZE = 21
        private const val MODE_FUF = 31
        private const val MODE_UNFREEZEANDRUN = 22
        private const val ILLEGALPKGNAME = "Fy^&IllegalPN*@!1024`+=：:"
    }
}
