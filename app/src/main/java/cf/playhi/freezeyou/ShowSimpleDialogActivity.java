package cf.playhi.freezeyou;

import android.content.DialogInterface;
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
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton(android.R.string.copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ClipboardUtils.copyToClipboard(ShowSimpleDialogActivity.this, text)) {
                            ToastUtils.showToast(ShowSimpleDialogActivity.this, R.string.success);
                        } else {
                            ToastUtils.showToast(ShowSimpleDialogActivity.this, R.string.failed);
                        }
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create()
                .show();
    }
}