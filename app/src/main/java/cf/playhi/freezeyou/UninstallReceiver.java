package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)||Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)){
            String pkgName = intent.getDataString();
            if (pkgName!=null){
                pkgName = pkgName.replace("package:","");
                if (ifOnekeyFreezeList(context,pkgName)){
                    SharedPreferences sharedPreferences = context.getSharedPreferences(
                            "AutoFreezeApplicationList", Context.MODE_PRIVATE);
                    String pkgNameList = sharedPreferences.getString("pkgName", "");
                    sharedPreferences.edit()
                            .putString(
                                    "pkgName",
                                    pkgNameList.replace("|" + pkgName + "|", ""))
                            .apply();
                }
            }
        }
    }

    private static boolean ifOnekeyFreezeList(Context context,String pkgName){
        String[] autoFreezePkgNameList = context.getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        for(String s: autoFreezePkgNameList){
            if(s.replaceAll("\\|","").equals(pkgName))
                return true;
        }
        return false;
    }
}
