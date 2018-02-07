package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import java.io.DataOutputStream;

public class OneKeyFreeze extends Activity {
    private static Process process = null;
    private static DataOutputStream outputStream = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = this;
//        if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
//            if (getDevicePolicyManager(activity).setApplicationHidden(
//                    DeviceAdminReceiver.getComponentName(activity),pkgName,true)){
//                makeToast(activity,"Success!");
//            } else {
//                makeToast(activity, "Failed!");
//            }
//        } else {
        try {
            process = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(process.getOutputStream());
            String[] pkgNameList = getApplicationContext().getSharedPreferences(
                    "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
            for (String aPkgNameList : pkgNameList) {
                outputStream.writeBytes(
                        "pm disable " + aPkgNameList.replaceAll("\\|", "") + "\n");
            }
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                Toast.makeText(activity,R.string.executed,Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity,R.string.mayUnrootedOrOtherEx,Toast.LENGTH_LONG).show();
            }
            Support.destroyProcess(true,outputStream,process,activity);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(activity,getString(R.string.exception)+e.getMessage(),Toast.LENGTH_LONG).show();
            if (e.getMessage().contains("Permission denied")){
                Toast.makeText(activity,R.string.mayUnrooted,Toast.LENGTH_SHORT).show();
            }
            Support.destroyProcess(true,outputStream,process,activity);
        }
    }

//    private static void destroyProcess(Boolean finish, DataOutputStream dataOutputStream, Process process1, Activity activity){
//        try {
//            if (dataOutputStream != null) {
//                dataOutputStream.close();
//            }
//            process1.destroy();
//            if (finish){
//                activity.finish();
//            }
//        } catch (Exception e) {
//            if (finish){
//                activity.finish();
//            }
//        }
//    }
}
