package cf.playhi.freezeyou;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import net.grandcentrix.tray.AppPreferences;

import java.util.Date;
import java.util.concurrent.Executor;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.utils.ToastUtils.showShortToast;

public class AppLockActivity extends FreezeYouBaseActivity {
    private BiometricPrompt mBiometricPrompt;
    private BiometricPrompt.PromptInfo mPromptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_lock_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        initBiometricPromptPart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getIntent().getBooleanExtra("ignoreCurrentUnlockStatus", false)
                && !isLocked()) {
            finish();
            return;
        }

        Button unlockButton = findViewById(R.id.app_lock_main_unlock_button);
        ImageView logoImageView = findViewById(R.id.app_lock_main_logo_imageView);
        unlockButton.setOnClickListener(v -> mBiometricPrompt.authenticate(mPromptInfo));
        logoImageView.setOnClickListener(v -> mBiometricPrompt.authenticate(mPromptInfo));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ImageButton fingerprintImageButton = findViewById(R.id.app_lock_main_fingerprint_imageButton);
            fingerprintImageButton.setOnClickListener(v -> mBiometricPrompt.authenticate(mPromptInfo));
        }
        String logoPkgName = getIntent().getStringExtra("unlockLogoPkgName");
        if (logoPkgName != null) {
            logoImageView.setImageBitmap(
                    getBitmapFromDrawable(
                            getApplicationIcon(
                                    getApplicationContext(), logoPkgName,
                                    getApplicationInfoFromPkgName(logoPkgName, getApplicationContext()),
                                    false
                            )
                    )
            );
        }

        mBiometricPrompt.authenticate(mPromptInfo);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBiometricPrompt.cancelAuthentication();
    }

    private void initBiometricPromptPart() {
        Executor executor = ContextCompat.getMainExecutor(this);
        mBiometricPrompt = new BiometricPrompt(AppLockActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(
                            int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        showShortToast(
                                getApplicationContext(),
                                String.format(
                                        getString(R.string.authenticationError_colon), errString
                                )
                        );
                        setResult(RESULT_CANCELED);
                    }

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        new AppPreferences(AppLockActivity.this)
                                .put("lockTime", new Date().getTime());
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        showShortToast(getApplicationContext(), R.string.authenticationFailed);
                        setResult(RESULT_CANCELED);
                    }
                });

        mPromptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.authentication))
                .setSubtitle(getString(R.string.verifyToContinue))
                .setAllowedAuthenticators(BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .build();
    }

    @Override
    protected boolean activityNeedCheckAppLock() {
        return false;
    }
}
