package cf.playhi.freezeyou.ui;

import android.os.Bundle;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.ApplicationInfoUtils;

import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.FUFUtils.checkAndStartApp;

public class AskRunActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, true);
        super.onCreate(savedInstanceState);
        final String pkgName = getIntent().getStringExtra("pkgName");
        final String target = getIntent().getStringExtra("target");
        final String tasks = getIntent().getStringExtra("tasks");
        buildAlertDialog(
                this,
                getApplicationIcon(
                        this,
                        pkgName,
                        ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, this),
                        true),
                getResources().getString(R.string.unfreezedAndAskLaunch),
                getResources().getString(R.string.notice))
                .setNegativeButton(R.string.no, (dialogInterface, i) -> finish())
                .setPositiveButton(R.string.yes, (dialogInterface, ii) -> {
                    checkAndStartApp(
                            AskRunActivity.this,
                            pkgName,
                            target,
                            tasks,
                            null,
                            false
                    );
                    finish();
                })
                .setOnCancelListener(dialog -> finish())
                .create()
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
