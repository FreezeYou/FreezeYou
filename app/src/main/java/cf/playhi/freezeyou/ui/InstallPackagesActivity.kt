package cf.playhi.freezeyou.ui

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouAlertDialogBuilder
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.app.ObsdAlertDialog
import cf.playhi.freezeyou.service.InstallPackagesService
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.notAllowInstallWhenIsObsd
import cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.tryToAvoidUpdateWhenUsing
import cf.playhi.freezeyou.utils.*
import cf.playhi.freezeyou.ui.compose.alwaysAllowView
import cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import net.grandcentrix.tray.AppPreferences
import java.io.File
import java.util.*

/**
 * Install and uninstall
 */
open class InstallPackagesActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this, true)
        super.onCreate(savedInstanceState)
        init()
    }

    private fun clearTempFile(filePath: String) {
        InstallPackagesUtils.deleteTempFile(this, filePath, false)
    }

    private fun init() {
        val intent = intent
        val packageUri = intent.data
        if (packageUri == null) {
            showToast(this, String.format(getString(R.string.invalidUriToast), "null"))
            finish()
            return
        }
        val scheme = packageUri.scheme
        if (ContentResolver.SCHEME_FILE != scheme && ContentResolver.SCHEME_CONTENT != scheme && "package" != scheme) {
            showToast(this, String.format(getString(R.string.invalidUriToast), packageUri))
            finish()
            return
        }
        val install =
            !(Intent.ACTION_DELETE == intent.action || Intent.ACTION_UNINSTALL_PACKAGE == intent.action)
        if (ContentResolver.SCHEME_FILE == scheme) {
            // Check Storage Permission
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                val b = AlertDialogUtils
                    .buildAlertDialog(
                        this,
                        R.drawable.ic_warning,
                        R.string.needStoragePermission,
                        R.string.notice
                    )
                    .setOnCancelListener { finish() }
                    .setPositiveButton(R.string.okay) { _, _ ->
                        @Suppress("DEPRECATION")
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            301
                        )
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> finish() }
                if (!isFinishing) {
                    b.show()
                }
            } else {
                val apkFilePath = packageUri.path ?: ""
                checkAutoAndPrepareInstallDialog(install, packageUri, apkFilePath)
            }
        } else {
            val apkFileName = "package" + Date().time + "F.apk"
            val apkFilePath = externalCacheDir.toString() + File.separator + "ZDF-" + apkFileName
            checkAutoAndPrepareInstallDialog(install, packageUri, apkFilePath)
        }
    }

    private fun checkAutoAndPrepareInstallDialog(
        install: Boolean,
        packageUri: Uri,
        apkFilePath: String
    ) {
        val fromPkgLabel: String
        val fromPkgName: String
        val referrerUri = referrer
        if (referrerUri == null || "android-app" != referrerUri.scheme) {
            fromPkgLabel = ILLEGALPKGNAME
            fromPkgName = ILLEGALPKGNAME
        } else {
            fromPkgName = referrerUri.encodedSchemeSpecificPart.substring(2)
            val refererPackageLabel = getApplicationLabel(
                this@InstallPackagesActivity,
                null, null,
                fromPkgName
            )
            fromPkgLabel = if (refererPackageLabel == getString(R.string.uninstalled)) {
                ILLEGALPKGNAME
            } else {
                refererPackageLabel
            }
        }
        prepareInstallDialog(install, packageUri, apkFilePath, fromPkgLabel, fromPkgName)
    }

    private fun prepareInstallDialog(
        install: Boolean,
        packageUri: Uri,
        apkFilePath: String,
        fromPkgLabel: String,
        fromPkgName: String
    ) {
        val alertDialogMessage = StringBuilder()
        if (isFinishing) return
        @Suppress("DEPRECATION")
        val progressDialog =
            ProgressDialog.show(this, getString(R.string.plsWait), getString(R.string.loading___))
        val nl = System.getProperty("line.separator")
        if (install) {
            Thread {
                try {
                    if (apkFilePath.startsWith(externalCacheDir.toString() + File.separator + "ZDF-")) {
                        val inputStream = contentResolver.openInputStream(packageUri)
                        if (inputStream == null) {
                            finish()
                            return@Thread
                        }
                        FileUtils.copyFile(inputStream, apkFilePath)
                    }
                    val pm = packageManager
                    val packageInfo = pm.getPackageArchiveInfo(apkFilePath, 0)!!
                    val applicationInfo = packageInfo.applicationInfo!!
                    applicationInfo.sourceDir = apkFilePath
                    applicationInfo.publicSourceDir = apkFilePath

                    //Check AutoAllow
                    val sp = AppPreferences(this@InstallPackagesActivity)
                    val originData = sp.getString("installPkgs_autoAllowPkgs_allows", "")
                    if (originData != null
                        && ILLEGALPKGNAME != fromPkgLabel && MoreUtils.convertToList(
                            originData,
                            ","
                        ).contains(
                            Base64.encodeToString(fromPkgName.toByteArray(), Base64.DEFAULT)
                        )
                    ) {
                        //Allow
                        ServiceUtils.startService(
                            this@InstallPackagesActivity,
                            Intent(this@InstallPackagesActivity, InstallPackagesService::class.java)
                                .putExtra("install", true)
                                .putExtra("packageUri", packageUri)
                                .putExtra("apkFilePath", apkFilePath)
                                .putExtra("packageInfo", packageInfo)
                                .putExtra(
                                    "waitForLeaving",
                                    tryToAvoidUpdateWhenUsing.getValue(null)
                                )
                        )
                        if (isFinishing) return@Thread
                        runOnUiThread {
                            if (progressDialog.isShowing) progressDialog.cancel()
                            finish()
                        }
                    }
                    alertDialogMessage.append(getString(R.string.requestFromPackage_colon))
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(
                        if (ILLEGALPKGNAME == fromPkgLabel) getString(R.string.unknown) else fromPkgLabel
                    )
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(getString(R.string.installPackage_colon))
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(
                        String.format(
                            getString(R.string.application_colon_app),
                            pm.getApplicationLabel(applicationInfo)
                        )
                    )
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(
                        String.format(
                            getString(R.string.pkgName_colon_pkgName),
                            packageInfo.packageName
                        )
                    )
                    try {
                        val pi = packageManager.getPackageInfo(
                            packageInfo.packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES
                        )
                        alertDialogMessage.append(nl)
                        alertDialogMessage.append(
                            String.format(
                                getString(R.string.existed_colon_vN_longVC),
                                pi.versionName,
                                if (Build.VERSION.SDK_INT < 28) pi.versionCode.toString() else pi.longVersionCode.toString()
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(
                        String.format(
                            getString(R.string.version_colon_vN_longVC),
                            packageInfo.versionName,
                            if (Build.VERSION.SDK_INT < 28) packageInfo.versionCode.toString() else packageInfo.longVersionCode.toString()
                        )
                    )
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(nl)
                    alertDialogMessage.append(getString(R.string.whetherAllow))
                    if (isFinishing) return@Thread
                    runOnUiThread {
                        showInstallDialog(
                            progressDialog, 1,
                            alertDialogMessage, apkFilePath,
                            packageUri, fromPkgLabel, fromPkgName, packageInfo
                        )
                    }
                } catch (e: Exception) {
                    alertDialogMessage.append(
                        String.format(
                            getString(R.string.cannotInstall_colon_msg),
                            e.localizedMessage
                        )
                    )
                    if (isFinishing) return@Thread
                    runOnUiThread {
                        showInstallDialog(
                            progressDialog, 2,
                            alertDialogMessage, apkFilePath,
                            packageUri, fromPkgLabel, fromPkgName, null
                        )
                    }
                }
            }.start()
        } else {
            val packageName = packageUri.encodedSchemeSpecificPart
            if (packageName == null) {
                showToast(this, String.format(getString(R.string.invalidUriToast), packageUri))
                finish()
                return
            }
            alertDialogMessage.append(getString(R.string.requestFromPackage_colon))
            alertDialogMessage.append(nl)
            alertDialogMessage.append(
                if (ILLEGALPKGNAME == fromPkgLabel) getString(R.string.unknown) else fromPkgLabel
            )
            alertDialogMessage.append(nl)
            alertDialogMessage.append(nl)
            alertDialogMessage.append(getString(R.string.uninstallPackage_colon))
            alertDialogMessage.append(nl)
            alertDialogMessage.append(
                String.format(
                    getString(R.string.application_colon_app),
                    getApplicationLabel(this, null, null, packageName)
                )
            )
            alertDialogMessage.append(nl)
            alertDialogMessage.append(
                String.format(
                    getString(R.string.pkgName_colon_pkgName),
                    packageName
                )
            )
            alertDialogMessage.append(nl)
            alertDialogMessage.append(getString(R.string.whetherAllow))
            showInstallDialog(
                progressDialog, 0,
                alertDialogMessage, apkFilePath,
                packageUri, fromPkgLabel, fromPkgName, null
            )
        }
    }

    //install: 0-uninstall, 1-install, 2-failed.
    @Suppress("DEPRECATION")
    private fun showInstallDialog(
        progressDialog: ProgressDialog,
        install: Int,
        alertDialogMessage: CharSequence,
        apkFilePath: String,
        packageUri: Uri,
        fromPkgLabel: String,
        fromPkgName: String,
        processedPackageInfo: PackageInfo?
    ) {
        val installPackagesAlertDialog = ObsdAlertDialog(this)
        var autoAllowChecked = false
        if (install == 1) {
            if (fromPkgLabel != ILLEGALPKGNAME) {
                installPackagesAlertDialog.setView(
                    alwaysAllowView(
                        this,
                        String.format(getString(R.string.alwaysAllow_name), fromPkgLabel)
                    ) { autoAllowChecked = it }
                )
            }
        }
        when (install) {
            0 -> installPackagesAlertDialog.setTitle(R.string.uninstall)
            1 -> installPackagesAlertDialog.setTitle(R.string.install)
            2 -> installPackagesAlertDialog.setTitle(R.string.failed)
            else -> {}
        }
        val preDefinedTryToAvoidUpdateWhenUsing = tryToAvoidUpdateWhenUsing.getValue(null)
        installPackagesAlertDialog.setMessage(alertDialogMessage)
        installPackagesAlertDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.yes)
        ) { dialog, _ ->
            if (notAllowInstallWhenIsObsd.getValue(null)
                && installPackagesAlertDialog.isObsd()
            ) {
                AlertDialogUtils.buildAlertDialog(
                    this@InstallPackagesActivity,
                    R.drawable.ic_warning,
                    R.string.alert_isObsd,
                    R.string.dangerous
                )
                    .setPositiveButton(R.string.retry) { _, _ ->
                        showInstallDialog(
                            progressDialog, install,
                            alertDialogMessage, apkFilePath, packageUri,
                            fromPkgLabel, fromPkgName, processedPackageInfo
                        )
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        if (install != 0) clearTempFile(apkFilePath)
                        finish()
                    }
                    .setOnCancelListener {
                        showInstallDialog(
                            progressDialog, install,
                            alertDialogMessage, apkFilePath, packageUri,
                            fromPkgLabel, fromPkgName, processedPackageInfo
                        )
                    }
                    .create().show()
            } else {
                if (install == 1) {
                    if (autoAllowChecked) {
                        val sp = AppPreferences(this@InstallPackagesActivity)
                        val originData = sp.getString("installPkgs_autoAllowPkgs_allows", "")
                        val originDataList = MoreUtils.convertToList(originData, ",")
                        if (ILLEGALPKGNAME != fromPkgLabel
                            && (originData == null || !MoreUtils.convertToList(originData, ",")
                                .contains(
                                    Base64.encodeToString(
                                        fromPkgName.toByteArray(),
                                        Base64.DEFAULT
                                    )
                                ))
                        ) {
                            originDataList.add(
                                Base64.encodeToString(
                                    fromPkgName.toByteArray(),
                                    Base64.DEFAULT
                                )
                            )
                            sp.put(
                                "installPkgs_autoAllowPkgs_allows",
                                MoreUtils.listToString(originDataList, ",")
                            )
                        }
                    }
                }
                if (install == 2) {
                    clearTempFile(apkFilePath)
                    finish()
                } else {
                    if (DevicePolicyManagerUtils.isDeviceOwner(this@InstallPackagesActivity) ||
                        FUFUtils.checkRootPermission()
                    ) {
                        ServiceUtils.startService(
                            this@InstallPackagesActivity,
                            Intent(
                                this@InstallPackagesActivity,
                                InstallPackagesService::class.java
                            )
                                .putExtra("install", install == 1)
                                .putExtra("packageUri", packageUri)
                                .putExtra("apkFilePath", apkFilePath)
                                .putExtra("packageInfo", processedPackageInfo)
                                .putExtra("waitForLeaving", preDefinedTryToAvoidUpdateWhenUsing)
                        )
                        finish()
                    } else {
                        showInstallPermissionCheckFailedDialog(
                            install, apkFilePath, packageUri,
                            processedPackageInfo, preDefinedTryToAvoidUpdateWhenUsing
                        )
                    }
                }
            }
        }
        installPackagesAlertDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.no)
        ) { _, _ ->
            if (install != 0) clearTempFile(apkFilePath)
            finish()
        }
        if (!preDefinedTryToAvoidUpdateWhenUsing
            && processedPackageInfo != null
            && AccessibilityUtils.isAccessibilitySettingsOn(this)
        ) {
            installPackagesAlertDialog.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.installWhenNotUsing)
            ) { dialog, _ ->
                if (notAllowInstallWhenIsObsd.getValue(null)
                    && installPackagesAlertDialog.isObsd()
                ) {
                    AlertDialogUtils.buildAlertDialog(
                        this@InstallPackagesActivity,
                        R.drawable.ic_warning,
                        R.string.alert_isObsd,
                        R.string.dangerous
                    )
                        .setPositiveButton(R.string.retry) { _, _ ->
                            showInstallDialog(
                                progressDialog, install,
                                alertDialogMessage, apkFilePath,
                                packageUri, fromPkgLabel,
                                fromPkgName, processedPackageInfo
                            )
                        }
                        .setNegativeButton(R.string.cancel) { _, _ ->
                            if (install != 0) clearTempFile(apkFilePath)
                            finish()
                        }
                        .create().show()
                } else {
                    if (install == 1) {
                        if (autoAllowChecked) {
                            val sp = AppPreferences(this@InstallPackagesActivity)
                            val originData = sp.getString("installPkgs_autoAllowPkgs_allows", "")
                            val originDataList = MoreUtils.convertToList(originData, ",")
                            if (ILLEGALPKGNAME != fromPkgLabel
                                && (originData == null || !MoreUtils.convertToList(originData, ",")
                                    .contains(
                                        Base64.encodeToString(
                                            fromPkgName.toByteArray(),
                                            Base64.DEFAULT
                                        )
                                    ))
                            ) {
                                originDataList.add(
                                    Base64.encodeToString(
                                        fromPkgName.toByteArray(),
                                        Base64.DEFAULT
                                    )
                                )
                                sp.put(
                                    "installPkgs_autoAllowPkgs_allows",
                                    MoreUtils.listToString(originDataList, ",")
                                )
                            }
                        }
                    }
                    if (install == 2) {
                        clearTempFile(apkFilePath)
                    } else {
                        ServiceUtils.startService(
                            this@InstallPackagesActivity,
                            Intent(
                                this@InstallPackagesActivity,
                                InstallPackagesService::class.java
                            )
                                .putExtra("install", install == 1)
                                .putExtra("packageUri", packageUri)
                                .putExtra("apkFilePath", apkFilePath)
                                .putExtra("packageInfo", processedPackageInfo)
                                .putExtra("waitForLeaving", true)
                        )
                    }
                    finish()
                }
            }
        }
        installPackagesAlertDialog.setOnCancelListener {
            if (install != 0) clearTempFile(apkFilePath)
            finish()
        }
        if (progressDialog.isShowing) {
            progressDialog.cancel()
        }
        if (isFinishing) return
        installPackagesAlertDialog.show()
        val w = installPackagesAlertDialog.window
        if (w != null) {
            val v = w.findViewById<View>(android.R.id.custom)
            if (v != null) {
                val p = v.parent as View?
                p?.minimumHeight = 0
            }
        }
    }

    private fun showInstallPermissionCheckFailedDialog(
        install: Int, apkFilePath: String,
        packageUri: Uri,
        processedPackageInfo: PackageInfo?,
        preDefinedTryToAvoidUpdateWhenUsing: Boolean
    ) {
        val adbd = FreezeYouAlertDialogBuilder(this@InstallPackagesActivity)
        adbd.setMessage(R.string.installPerimisionCheckFailed_ifContinue)
        adbd.setTitle(R.string.notice)
        adbd.setPositiveButton(R.string.yes) { _, _ ->
            ServiceUtils.startService(
                this@InstallPackagesActivity,
                Intent(
                    this@InstallPackagesActivity,
                    InstallPackagesService::class.java
                )
                    .putExtra("install", install == 1)
                    .putExtra("packageUri", packageUri)
                    .putExtra("apkFilePath", apkFilePath)
                    .putExtra("packageInfo", processedPackageInfo)
                    .putExtra("waitForLeaving", preDefinedTryToAvoidUpdateWhenUsing)
            )
            finish()
        }
        adbd.setNegativeButton(R.string.no) { _, _ -> finish() }
        adbd.setNeutralButton(R.string.jumpToSysInstaller) { _, _ ->
            if (install == 0) {
                this@InstallPackagesActivity.startActivity(
                    Intent(
                        Intent.ACTION_DELETE,
                        Uri.parse("package:" + packageUri.encodedSchemeSpecificPart)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                finish()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (packageManager.canRequestPackageInstalls()) {
                        requestSysInstallPkg(apkFilePath)
                        finish()
                    } else {
                        showInstallPermissionCheckFailedDialog(
                            install, apkFilePath, packageUri,
                            processedPackageInfo, preDefinedTryToAvoidUpdateWhenUsing
                        )
                        val packageUri1 = Uri.parse("package:cf.playhi.freezeyou")
                        val intent =
                            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri1)
                        startActivity(intent)
                    }
                } else {
                    requestSysInstallPkg(apkFilePath)
                    finish()
                }
            }
        }
        adbd.setOnCancelListener { finish() }
        adbd.show()
    }

    private fun requestSysInstallPkg(filePath: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val file = File(filePath)
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "cf.playhi.freezeyou.fileprovider", file)
        } else {
            @Suppress("DEPRECATION")
            Uri.fromFile(file)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        val chooser = Intent.createChooser(intent, getString(R.string.plsSelect))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 301) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                finish()
            }
        }
    }

    companion object {
        private const val ILLEGALPKGNAME = "Fy^&IllegalPN*@!128`+=：:,.["
    }
}
