package cf.playhi.freezeyou.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import net.grandcentrix.tray.AppPreferences;

import java.util.Date;

import cf.playhi.freezeyou.AppLockActivity;

import static cf.playhi.freezeyou.utils.Support.checkLanguage;

public class FreezeYouBaseActivity extends AppCompatActivity {
    private String mUnlockLogoPkgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLanguage(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityNeedCheckAppLock() && isLocked()) {
            startActivity(
                    new Intent(this, AppLockActivity.class)
                            .putExtra("unlockLogoPkgName", mUnlockLogoPkgName)
            );
        }
    }

    @Override
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
        AppPreferences appPreferences = new AppPreferences(this);
        long currentTime = new Date().getTime();
        // 15 minutes
        if (appPreferences.getLong("lockTime", 0) < currentTime - 900000) {
            return true;
        } else {
            appPreferences.put("lockTime", new Date().getTime());
            return false;
        }
    }

    protected void setUnlockLogoPkgName(String pkgName) {
        mUnlockLogoPkgName = pkgName;
    }

}
