package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataOutputStream;

import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.showToast;
import static cf.playhi.freezeyou.Support.getDevicePolicyManager;
import static cf.playhi.freezeyou.Support.fAURoot;
import static cf.playhi.freezeyou.Support.destroyProcess;

public class ManualMode extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manualmode);
        final Process process = null;
        final DataOutputStream outputStream = null;
        final EditText packageNameEditText = findViewById(R.id.packageNameEditText);
        Button disable_MRoot = findViewById(R.id.disable_MRoot);
        Button disable_Root = findViewById(R.id.disable_Root);
        Button enable_MRoot = findViewById(R.id.enable_MRoot);
        Button enable_Root = findViewById(R.id.enable_Root);
        final Context activity = getApplicationContext();
        disable_MRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)) {
                    String pkgName = packageNameEditText.getText().toString();
                    if (getDevicePolicyManager(activity).setApplicationHidden(
                        DeviceAdminReceiver.getComponentName(activity), pkgName, true)){
                        showToast(ManualMode.this,R.string.success);
                    } else {
                        showToast(ManualMode.this,R.string.failed);
                    }
                } else {
                    showToast(ManualMode.this,R.string.failed);
                }
            }
        });
        enable_MRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)) {
                    String pkgName = packageNameEditText.getText().toString();
                    if (getDevicePolicyManager(activity).setApplicationHidden(
                            DeviceAdminReceiver.getComponentName(activity), pkgName, false)){
                        showToast(ManualMode.this,R.string.success);
                    } else {
                        showToast(ManualMode.this,R.string.failed);
                    }
                } else {
                    showToast(ManualMode.this,R.string.failed);
                }
            }
        });
        disable_Root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pkgName = packageNameEditText.getText().toString();
                try {
                    int exitValue = fAURoot(pkgName,false,process,outputStream);
                    if (exitValue == 0) {
                        showToast(activity, R.string.executed);
                    } else {
                        showToast(activity, R.string.mayUnrootedOrOtherEx);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(activity, activity.getString(R.string.exception) + e.getMessage());
                    if (e.getMessage().contains("Permission denied")) {
                        showToast(activity, R.string.mayUnrooted);
                    }
                    destroyProcess(false, outputStream, process, ManualMode.this);
                }
            }
        });
        enable_Root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pkgName = packageNameEditText.getText().toString();
                try {
                    int exitValue = fAURoot(pkgName,true,process,outputStream);
                    if (exitValue == 0) {
                        showToast(activity, R.string.executed);
                    } else {
                        showToast(activity, R.string.mayUnrootedOrOtherEx);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(activity, activity.getString(R.string.exception) + e.getMessage());
                    if (e.getMessage().contains("Permission denied")) {
                        showToast(activity, R.string.mayUnrooted);
                    }
                    destroyProcess(false, outputStream, process, ManualMode.this);
                }
            }
        });
    }
}
