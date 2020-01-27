package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_ROOT_UNHIDE_HIDE;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.getDevicePolicyManager;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner;
import static cf.playhi.freezeyou.utils.FUFUtils.processAction;
import static cf.playhi.freezeyou.utils.FUFUtils.processRootAction;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class ManualMode extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manualmode);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final EditText packageNameEditText = findViewById(R.id.packageNameEditText);
        Button disableMRootDPM = findViewById(R.id.disable_MRoot_DPM);
        Button disableRootDisable = findViewById(R.id.disable_Root_disable);
        Button disableRootHide = findViewById(R.id.disable_Root_hide);
        Button enableMRootDPM = findViewById(R.id.enable_MRoot_DPM);
        Button enableRootEnable = findViewById(R.id.enable_Root_enable);
        Button enableRootUnhide = findViewById(R.id.enable_Root_unhide);
        final Context context = getApplicationContext();
        disableMRootDPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processMRootOperation(packageNameEditText.getText().toString(), context, true);
            }
        });
        enableMRootDPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processMRootOperation(packageNameEditText.getText().toString(), context, false);
            }
        });
        disableRootHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processAction(
                        packageNameEditText.getText().toString(),
                        null,
                        null,
                        context,
                        false,
                        false,
                        false,
                        null,
                        false,
                        API_FREEZEYOU_ROOT_UNHIDE_HIDE
                );
            }
        });
        enableRootUnhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processAction(
                        packageNameEditText.getText().toString(),
                        null,
                        null,
                        context,
                        true,
                        false,
                        false,
                        null,
                        false,
                        API_FREEZEYOU_ROOT_UNHIDE_HIDE
                );
            }
        });
        disableRootDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processRootAction(
                        packageNameEditText.getText().toString(),
                        null,
                        null,
                        context,
                        false,
                        false,
                        false,
                        null,
                        false);
            }
        });
        enableRootEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processRootAction(
                        packageNameEditText.getText().toString(),
                        null,
                        null,
                        context,
                        true,
                        false,
                        false,
                        null,
                        false);
            }
        });
    }

    private void processMRootOperation(String pkgName, Context context, boolean hidden) {
        if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
            if (getDevicePolicyManager(context).setApplicationHidden(
                    DeviceAdminReceiver.getComponentName(context), pkgName, hidden)) {
                showToast(context, R.string.success);
            } else {
                showToast(context, R.string.failed);
            }
        } else {
            showToast(context, R.string.failed);
        }
    }
}
