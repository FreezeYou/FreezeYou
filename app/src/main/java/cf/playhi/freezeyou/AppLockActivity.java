package cf.playhi.freezeyou;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

public class AppLockActivity extends FreezeYouBaseActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_lock_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        initBiometricPromptPart();

        Button unlockButton = findViewById(R.id.app_lock_main_unlock_button);
        ImageView logoImageView = findViewById(R.id.app_lock_main_logo_imageView);
        unlockButton.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
        logoImageView.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
        String logoPkgName = getIntent().getStringExtra("unlockLogoPkgName");
        if (logoPkgName != null) {
            logoImageView.setImageBitmap(
                    getBitmapFromDrawable(
                            getApplicationIcon(
                                    getApplicationContext(), logoPkgName,
                                    null, false
                            )
                    )
            );
        }

        biometricPrompt.authenticate(promptInfo);
    }

    private void initBiometricPromptPart() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(AppLockActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(
                            int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        new AppPreferences(AppLockActivity.this)
                                .put("lockTime", new Date().getTime());
                        setResult(-2);
                        finish();
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
                        Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("身份验证")
                .setSubtitle("验证以继续")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .build();
    }

    @Override
    protected boolean activityNeedCheckAppLock() {
        return false;
    }
}
