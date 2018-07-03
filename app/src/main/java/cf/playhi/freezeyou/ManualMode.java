package cf.playhi.freezeyou;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
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
        ActionBar actionBar = getActionBar();
        if (actionBar!= null){
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final Process process = null;
        final DataOutputStream outputStream = null;
        final EditText packageNameEditText = findViewById(R.id.packageNameEditText);
        Button disable_MRoot = findViewById(R.id.disable_MRoot);
        Button disable_Root = findViewById(R.id.disable_Root);
        Button enable_MRoot = findViewById(R.id.enable_MRoot);
        Button enable_Root = findViewById(R.id.enable_Root);
        final Context context = getApplicationContext();
        disable_MRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processMRootOperation(packageNameEditText.getText().toString(),context,true);
            }
        });
        enable_MRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processMRootOperation(packageNameEditText.getText().toString(),context,false);
            }
        });
        disable_Root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processRootedOperation(
                        packageNameEditText.getText().toString(),
                        false,
                        false,
                        process,
                        outputStream,
                        context,
                        ManualMode.this);
            }
        });
        enable_Root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processRootedOperation(
                        packageNameEditText.getText().toString(),
                        true,
                        false,
                        process,
                        outputStream,
                        context,
                        ManualMode.this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void processRootedOperation(String pkgName,boolean enable,boolean finish,Process process,DataOutputStream outputStream,Context context,Activity activity) {
        try {
            int exitValue = fAURoot(pkgName, enable, process, outputStream);
            if (exitValue == 0) {
                showToast(context, R.string.executed);
            } else {
                showToast(context, R.string.mayUnrootedOrOtherEx);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, context.getString(R.string.exception) + e.getMessage());
            if (e.getMessage().contains("Permission denied")) {
                showToast(context, R.string.mayUnrooted);
            }
            destroyProcess(finish, outputStream, process, activity);
        }
    }

    private void processMRootOperation(String pkgName,Context context,boolean hidden){
        if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(context)) {
            if (getDevicePolicyManager(context).setApplicationHidden(
                    DeviceAdminReceiver.getComponentName(context), pkgName, hidden)){
                showToast(context,R.string.success);
            } else {
                showToast(context,R.string.failed);
            }
        } else {
            showToast(context,R.string.failed);
        }
    }
}
