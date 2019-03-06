package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.CheckBox;

public class UriFreezeActivity extends Activity {

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
        Intent intent = getIntent();
        String pkgName;
        if (intent != null) {
            if ("freezeyou".equals(intent.getScheme())) {
                Uri dataUri = intent.getData();
                if (dataUri != null) {
                    String action = dataUri.getQueryParameter("action");
                    if (action == null || "".equals(action))
                        action = "fuf";
                    pkgName = dataUri.getQueryParameter("pkgName");
                    ObsdAlertDialog dialog = new ObsdAlertDialog(this);
                    View checkBoxView = View.inflate(this, R.layout.checkbox, null);//https://stackoverflow.com/questions/9763643/how-to-add-a-check-box-to-an-alert-dialog
                    CheckBox checkBox = checkBoxView.findViewById(R.id.checkBox);
                    checkBox.setText("总是允许");
                    dialog.setView(checkBoxView);
                    dialog.setTitle("Title");
                    dialog.setMessage(pkgName);
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "允许", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastUtils.showToast(UriFreezeActivity.this, "允许");
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastUtils.showToast(UriFreezeActivity.this, "拒绝");
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastUtils.showToast(UriFreezeActivity.this, "取消");
                        }
                    });
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });
                    dialog.show();
                    Window w = dialog.getWindow();
                    if (w != null) {
                        View v = (View) w.findViewById(android.R.id.custom).getParent();
                        if (v != null)
                            v.setMinimumHeight(0);
                    }
                } else {
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

}
