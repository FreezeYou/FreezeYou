package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import static cf.playhi.freezeyou.utils.DebugModeUtils.isDebugModeEnabled;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner;
import static cf.playhi.freezeyou.utils.FUFUtils.oneKeyActionMRoot;
import static cf.playhi.freezeyou.utils.FUFUtils.oneKeyActionRoot;

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
public class DisableApplications extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if (intent != null) {
            String[] packages = intent.getStringArrayExtra("packages");

            if (isDebugModeEnabled(this)) {
                Log.e("DebugModeLogcat", "Intent toString:" + intent.toString());
                for (String p : packages) {
                    Log.e("DebugModeLogcat", "Intent packages:" + p);
                }
            }

            if (packages != null) {
                setResult(Activity.RESULT_OK);
                if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(DisableApplications.this)) {
                    oneKeyActionMRoot(DisableApplications.this, true, packages);
                    finish();
                } else {
                    oneKeyActionRoot(DisableApplications.this, true, packages);
                    finish();
                }
            }
        } else {
            if (isDebugModeEnabled(this)) {
                Log.e("DebugModeLogcat", "Intent: null");
            }
        }
    }
}
