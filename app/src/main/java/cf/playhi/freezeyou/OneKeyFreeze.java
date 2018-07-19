package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import static cf.playhi.freezeyou.Support.buildAlertDialog;
import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;
import static cf.playhi.freezeyou.Support.openDevicePolicyManager;
import static cf.playhi.freezeyou.Support.processAddTranslucent;
import static cf.playhi.freezeyou.Support.processSetTheme;
import static cf.playhi.freezeyou.Support.showToast;

public class OneKeyFreeze extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        processAddTranslucent(this);
        super.onCreate(savedInstanceState);
        boolean auto = getIntent().getBooleanExtra("autoCheckAndLockScreen",true);
        Activity activity = this;
        String[] pkgNameList = getApplicationContext().getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
            oneKeyActionMRoot(activity,activity,true,pkgNameList);
            checkAuto(auto,activity);
        } else {
            oneKeyActionRoot(activity,activity,true,pkgNameList,false);
            checkAuto(auto,activity);
        }
    }

    private void checkAndLockScreen(final Context context){
        switch (PreferenceManager.getDefaultSharedPreferences(context).getString("shortCutOneKeyFreezeAdditionalOptions","nothing")){
            case "nothing":
                doFinish();
                break;
            case "askLockScreen":
                buildAlertDialog(context,R.mipmap.ic_launcher_new_round,R.string.askIfLockScreen,R.string.notice)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doLockScreen(context);
                                doFinish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doFinish();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                doFinish();
                            }
                        })
                        .create().show();
                break;
            case "lockScreenImmediately":
                doLockScreen(context);
                doFinish();
                break;
            default:
                doFinish();
                break;
        }
    }

    private void doFinish(){
        finish();
    }

    private void doLockScreen(Context context){
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, DeviceAdminReceiver.class);
        if (devicePolicyManager!=null){
            if (devicePolicyManager.isAdminActive(componentName)){
                devicePolicyManager.lockNow();
            } else {
                openDevicePolicyManager(context);
            }
        } else {
            showToast(context,R.string.devicePolicyManagerNotFound);
        }
    }

    private void checkAuto(boolean auto,Context context){
        if (auto){
            checkAndLockScreen(context);
        } else {
            doFinish();
        }
    }
}
