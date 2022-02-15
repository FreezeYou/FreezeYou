package cf.playhi.freezeyou.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.splitcompat.SplitCompat;
import com.tencent.mmkv.MMKV;

import java.util.Date;
import java.util.Locale;

import cf.playhi.freezeyou.ui.AppLockActivity;

import static cf.playhi.freezeyou.utils.AuthenticationUtils.isAuthenticationEnabled;
import static cf.playhi.freezeyou.utils.AuthenticationUtils.isBiometricPromptPartAvailable;
import static cf.playhi.freezeyou.utils.Support.checkLanguage;
import static cf.playhi.freezeyou.utils.Support.getLocalString;
import static cf.playhi.freezeyou.utils.VersionUtils.isGooglePlayVersion;

public class FreezeYouBaseActivity extends AppCompatActivity {
    private static final int APP_LOCK_ACTIVITY_REQUEST_CODE = 65533;

    private String mUnlockLogoPkgName;
    private boolean mHadBeenUnlocked = false;

    @Override
    @CallSuper
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String locale = getLocalString(newBase);
            Configuration configuration = new Configuration();
            configuration.setLocale(
                    "Default".equals(locale) ? Locale.getDefault() : Locale.forLanguageTag(locale)
            );
            Context context = newBase.createConfigurationContext(configuration);
            super.attachBaseContext(context);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            checkLanguage(this);
        } else {
            if (isGooglePlayVersion(this)) {
                SplitCompat.install(this);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        if (activityNeedCheckAppLock() && isAuthenticationEnabled()
                && isBiometricPromptPartAvailable(this)) {
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
            resetLockWaitTime(
                    MMKV.mmkvWithAshmemID(
                            getApplicationContext(), "AshmemKV",
                            32, MMKV.MULTI_PROCESS_MODE, null
                    )
            );
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

    /**
     * If the activity does not need the app lock logic,
     * override the method and return false.
     *
     * @return Whether the app lock needs to be checked.
     */
    protected boolean activityNeedCheckAppLock() {
        return true;
    }

    protected boolean isLocked() {
        if (!isAuthenticationEnabled()) return false;
        if (!isBiometricPromptPartAvailable(this)) return false;

        MMKV mmkv =
                MMKV.mmkvWithAshmemID(
                        getApplicationContext(), "AshmemKV",
                        32, MMKV.MULTI_PROCESS_MODE, null);

        long currentTime = new Date().getTime();
        // 15 minutes
        if (mmkv.decodeLong("unlockTime", 0) < currentTime - 900000) {
            return true;
        } else {
            resetLockWaitTime(mmkv);
            return false;
        }
    }

    protected boolean resetLockWaitTime(MMKV mmkv) {
        return mmkv.encode("unlockTime", new Date().getTime());
    }

    protected void setUnlockLogoPkgName(String pkgName) {
        mUnlockLogoPkgName = pkgName;
    }

}
