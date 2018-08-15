package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;

import static cf.playhi.freezeyou.Support.buildAlertDialog;
import static cf.playhi.freezeyou.Support.checkAndStartApp;
import static cf.playhi.freezeyou.Support.getApplicationIcon;

public class AskRunActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String pkgName = getIntent().getStringExtra("pkgName");
        buildAlertDialog(
                this,
                getApplicationIcon(
                        this,
                        pkgName,
                        null,
                        true),
                getResources().getString(R.string.unfreezedAndAskLaunch),
                getResources().getString(R.string.notice))
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int ii) {
                        checkAndStartApp(AskRunActivity.this,  pkgName,null,false);
                        finish();
                    }
                })
                .create()
                .show();
    }
}
