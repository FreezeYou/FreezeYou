package cf.playhi.freezeyou.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import net.grandcentrix.tray.AppPreferences;

import java.util.Date;

import cf.playhi.freezeyou.AppLockActivity;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static cf.playhi.freezeyou.utils.Support.checkLanguage;

public class FreezeYouBaseActivity extends AppCompatActivity {
    private static final int APP_LOCK_ACTIVITY_REQUEST_CODE = 65533;

    private String mUnlockLogoPkgName;
    private boolean mHadBeenUnlocked = false;

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        checkLanguage(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        if (activityNeedCheckAppLock() && isBiometricPromptPartAvailable()) {
            if (isLocked()) {
                mHadBeenUnlocked = false;
                startActivityForResult(
                        new Intent(this, AppLockActivity.class)
                                .putExtra("unlockLogoPkgName", mUnlockLogoPkgName),
                        APP_LOCK_ACTIVITY_REQUEST_CODE
                );
            } else {
                mHadBeenUnlocked = true;
            }
        }
    }

    @Override
    @CallSuper
    protected void onPause() {
        super.onPause();
        if (mHadBeenUnlocked) {
            resetLockWaitTime(new AppPreferences(this));
        }
    }

    @Override
    @CallSuper
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_LOCK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean activityNeedCheckAppLock() {
        return true;
    }

    protected boolean isLocked() {
        if (!isBiometricPromptPartAvailable()) return false;

        AppPreferences appPreferences = new AppPreferences(this);
        long currentTime = new Date().getTime();
        // 15 minutes900000
        if (appPreferences.getLong("lockTime", 0) < currentTime - 2000) {
            return true;
        } else {
            resetLockWaitTime(appPreferences);
            return false;
        }
    }

    protected boolean resetLockWaitTime(AppPreferences appPreferences) {
        return appPreferences.put("lockTime", new Date().getTime());
    }

    protected void setUnlockLogoPkgName(String pkgName) {
        mUnlockLogoPkgName = pkgName;
    }

    private boolean isBiometricPromptPartAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(
                BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
            default:
                return false;
        }
    }
}
