package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;

public class DisableApplications extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] packages = getIntent().getStringArrayExtra("packages");
        if (packages != null) {
            setResult(Activity.RESULT_OK);
            if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(DisableApplications.this)) {
                oneKeyActionMRoot(DisableApplications.this, true, packages);
                finish();
            } else {
                oneKeyActionRoot(DisableApplications.this, DisableApplications.this, true, packages, true);
            }
        }
    }
}
