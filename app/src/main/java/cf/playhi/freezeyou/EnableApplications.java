package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import static cf.playhi.freezeyou.DebugModeUtils.isDebugModeEnabled;
import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;
import static cf.playhi.freezeyou.ToastUtils.showToast;

public class EnableApplications extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (isDebugModeEnabled(this)) {
            showToast(this, intent == null ? "null" : intent.toString());
        }

        if (intent != null) {
            String[] packages = intent.getStringArrayExtra("packages");
            if (packages != null) {
                setResult(Activity.RESULT_OK);
                if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(EnableApplications.this)) {
                    oneKeyActionMRoot(EnableApplications.this, false, packages);
                    finish();
                } else {
                    oneKeyActionRoot(EnableApplications.this, false, packages);
                    finish();
                }
            }
        }
    }
}
