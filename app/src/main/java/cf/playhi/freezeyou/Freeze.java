package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Bundle;

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
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void init() {
        String pkgName = getIntent().getStringExtra("pkgName");
        boolean auto = getIntent().getBooleanExtra("auto", true);
        if (pkgName == null) {
            showToast(getApplicationContext(), R.string.invalidArguments);
            Freeze.this.finish();
        } else if ("".equals(pkgName)) {
            showToast(getApplicationContext(), R.string.invalidArguments);
            Freeze.this.finish();
        }
        if ((!checkRootFrozen(Freeze.this, pkgName, null)) && (!checkMRootFrozen(Freeze.this, pkgName))) {
            processDialog(pkgName, auto, 2);
        } else {
            processDialog(pkgName, auto, 1);
        }
    }

    private void processDialog(String pkgName, boolean auto, int ot) {
        shortcutMakeDialog(Freeze.this, getApplicationLabel(Freeze.this, null, null, pkgName), getString(R.string.chooseDetailAction), Freeze.this, null, pkgName, ot, auto, true);
    }
}
