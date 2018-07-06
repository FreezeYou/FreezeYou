package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.io.DataOutputStream;

import static cf.playhi.freezeyou.Support.checkFrozen;
import static cf.playhi.freezeyou.Support.createNotification;
import static cf.playhi.freezeyou.Support.deleteNotification;
import static cf.playhi.freezeyou.Support.getDevicePolicyManager;
import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.showToast;

public class OneKeyUF extends Activity {
    private static Process process = null;
    private static DataOutputStream outputStream = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = this;
        String[] pkgNameList = getApplicationContext().getSharedPreferences(
                "OneKeyUFApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
            for (String aPkgNameList : pkgNameList) {
                String tmp = aPkgNameList.replaceAll("\\|", "");
                try {
                    if (checkFrozen(activity, tmp)) {
                        if (!getDevicePolicyManager(activity).setApplicationHidden(
                                DeviceAdminReceiver.getComponentName(activity), tmp, false)) {
                            showToast(activity, tmp + " " + getString(R.string.failed) + getString(R.string.mayUnrootedOrOtherEx));
                        } else {
                            createNotification(activity,tmp,R.drawable.ic_notification);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(activity, "发生了点异常，操作仍将继续:" + e.getLocalizedMessage());
                }
            }
            showToast(activity,R.string.executed);
            finish();
        } else {
            try {
                process = Runtime.getRuntime().exec("su");
                outputStream = new DataOutputStream(process.getOutputStream());
                for (String aPkgNameList : pkgNameList) {
                    int tmp = getPackageManager().getApplicationEnabledSetting(aPkgNameList.replaceAll("\\|", ""));
                    if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                        outputStream.writeBytes(
                                "pm enable " + aPkgNameList.replaceAll("\\|", "") + "\n");
                    }
                }
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                int exitValue = process.waitFor();
                if (exitValue == 0) {
                    for (String aPkgNameList : pkgNameList) {
                        createNotification(activity,aPkgNameList.replaceAll("\\|", ""),R.drawable.ic_notification);
                    }
                    showToast(activity,R.string.executed);
                } else {
                    showToast(activity,R.string.mayUnrootedOrOtherEx);
                }
                Support.destroyProcess(true,outputStream,process,activity);
            } catch (Exception e){
                e.printStackTrace();
                showToast(activity,getString(R.string.exception)+e.getMessage());
                if (e.getMessage().toLowerCase().contains("permission denied")||e.getMessage().toLowerCase().contains("not found")){
                    showToast(activity,R.string.mayUnrooted);
                }
                Support.destroyProcess(true,outputStream,process,activity);
            }
        }

    }
}
