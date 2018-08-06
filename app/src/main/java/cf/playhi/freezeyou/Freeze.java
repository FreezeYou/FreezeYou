package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;
import static cf.playhi.freezeyou.Support.checkMRootFrozen;
import static cf.playhi.freezeyou.Support.checkRootFrozen;
import static cf.playhi.freezeyou.Support.getApplicationLabel;
import static cf.playhi.freezeyou.Support.processAddTranslucent;
import static cf.playhi.freezeyou.Support.processSetTheme;
import static cf.playhi.freezeyou.Support.shortcutMakeDialog;
import static cf.playhi.freezeyou.Support.showToast;

public class Freeze extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        processAddTranslucent(this);
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.shortcut);
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        init();
    }

    private void init() {
        String pkgName = getIntent().getStringExtra("pkgName");
        boolean auto = getIntent().getBooleanExtra("auto", true);
        if (pkgName == null) {
            showToast(getApplicationContext(), "参数错误");
            Freeze.this.finish();
        } else if ("".equals(pkgName)) {
            showToast(getApplicationContext(), "参数错误");
            Freeze.this.finish();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setTaskDescription(new ActivityManager.TaskDescription(getApplicationLabel(this, null, null, pkgName)));
        }
        if (3 == getIntent().getIntExtra("ot", 0)) {
            processDialog(pkgName, auto, 3);
        } else if ((!checkRootFrozen(Freeze.this, pkgName)) && (!checkMRootFrozen(Freeze.this, pkgName))) {
            processDialog(pkgName, auto, 2);
        } else {
            processDialog(pkgName, auto, 1);
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void processDialog(String pkgName, boolean auto, int ot) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = getPackageManager().getApplicationInfo(pkgName, GET_UNINSTALLED_PACKAGES);
            shortcutMakeDialog(getPackageManager().getApplicationLabel(applicationInfo).toString(), getString(R.string.chooseDetailAction), Freeze.this, true, applicationInfo, pkgName, ot, auto);
        } catch (Exception e) {
            e.printStackTrace();
            shortcutMakeDialog(getString(R.string.notice), getString(R.string.chooseDetailAction), Freeze.this, true, applicationInfo, pkgName, 2, auto);
        }
    }
}
