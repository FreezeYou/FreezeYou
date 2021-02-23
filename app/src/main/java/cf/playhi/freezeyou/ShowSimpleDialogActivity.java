package cf.playhi.freezeyou;

import android.content.Intent;
import android.os.Bundle;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.AlertDialogUtils;
import cf.playhi.freezeyou.utils.ClipboardUtils;
import cf.playhi.freezeyou.utils.ToastUtils;

import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class ShowSimpleDialogActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, true);
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        final String title = intent.getStringExtra("title");
        final String text = intent.getStringExtra("text");
        AlertDialogUtils
                .buildAlertDialog(this, null, text, title)
                .setPositiveButton(R.string.okay, (dialog, which) -> finish())
                .setNeutralButton(android.R.string.copy, (dialog, which) -> {
                    if (ClipboardUtils.copyToClipboard(ShowSimpleDialogActivity.this, text)) {
                        ToastUtils.showToast(ShowSimpleDialogActivity.this, R.string.success);
                    } else {
                        ToastUtils.showToast(ShowSimpleDialogActivity.this, R.string.failed);
                    }
                    finish();
                })
                .setOnCancelListener(dialog -> finish())
                .create()
                .show();
    }

    @Override
    protected boolean activityNeedCheckAppLock() {
        return false;
    }
}