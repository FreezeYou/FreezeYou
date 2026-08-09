package cf.playhi.freezeyou.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable
import cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.utils.ToastUtils.showShortToast
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.ui.compose.ResourceDrawableImage
import com.tencent.mmkv.MMKV
import java.util.*

class AppLockActivity : FreezeYouBaseActivity() {
    private lateinit var mBiometricPrompt: BiometricPrompt
    private lateinit var mPromptInfo: BiometricPrompt.PromptInfo
    private var logoBitmap by mutableStateOf<Bitmap?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        actionBar?.hide()
        initBiometricPromptPart()
        setContent {
            FreezeYouTheme {
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val bitmap = logoBitmap
                    if (bitmap == null) {
                        ResourceDrawableImage(
                            resourceId = R.mipmap.ic_launcher_new_round,
                            contentDescription = stringResource(R.string.unlock),
                            modifier = Modifier.size(300.dp).padding(100.dp)
                                .clickable { authenticate() }
                        )
                    } else {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.unlock),
                            modifier = Modifier.size(300.dp).padding(100.dp)
                                .clickable { authenticate() }
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(
                            onClick = ::authenticate,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(stringResource(R.string.unlock))
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!intent.getBooleanExtra("ignoreCurrentUnlockStatus", false)
            && !isLocked
        ) {
            finish()
            return
        }
        val logoPkgName = intent.getStringExtra("unlockLogoPkgName")
        if (logoPkgName != null) {
            logoBitmap = getBitmapFromDrawable(
                getApplicationIcon(
                    applicationContext, logoPkgName,
                    getApplicationInfoFromPkgName(logoPkgName, applicationContext),
                    false
                )
            )
        }
        authenticate()
    }

    override fun onPause() {
        super.onPause()
        mBiometricPrompt.cancelAuthentication()
    }

    private fun initBiometricPromptPart() {
        val executor = ContextCompat.getMainExecutor(this)
        mBiometricPrompt = BiometricPrompt(this@AppLockActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    showShortToast(
                        applicationContext,
                        String.format(
                            getString(R.string.authenticationError_colon), errString
                        )
                    )
                    setResult(RESULT_CANCELED)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    MMKV.mmkvWithAshmemID(
                        applicationContext, "AshmemKV",
                        32, MMKV.MULTI_PROCESS_MODE, null
                    )
                        .encode("unlockTime", Date().time)
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showShortToast(applicationContext, R.string.authenticationFailed)
                    setResult(RESULT_CANCELED)
                }
            })
        mPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.authentication))
            .setSubtitle(getString(R.string.verifyToContinue))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()
    }

    private fun authenticate() {
        mBiometricPrompt.authenticate(mPromptInfo)
    }

    override fun activityNeedCheckAppLock(): Boolean {
        return false
    }
}
