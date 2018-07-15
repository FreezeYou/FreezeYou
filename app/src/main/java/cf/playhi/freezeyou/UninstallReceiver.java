package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static cf.playhi.freezeyou.Support.removeFromOneKeyList;

public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)){
            String pkgName = intent.getDataString();
            if (pkgName!=null){
                pkgName = pkgName.replace("package:","");
                removeFromOneKeyList(context,"AutoFreezeApplicationList",pkgName);
                removeFromOneKeyList(context,"OneKeyUFApplicationList",pkgName);
//                if (checkOnekeyList(context,pkgName,"AutoFreezeApplicationList")){
//                    SharedPreferences sharedPreferences = context.getSharedPreferences(
//                            "AutoFreezeApplicationList", Context.MODE_PRIVATE);
//                    String pkgNameList = sharedPreferences.getString("pkgName", "");
//                    sharedPreferences.edit()
//                            .putString(
//                                    "pkgName",
//                                    pkgNameList.replace("|" + pkgName + "|", ""))
//                            .apply();
//                }
//                if (checkOnekeyList(context,pkgName,"OneKeyUFApplicationList")){
//                    SharedPreferences sharedPreferences = context.getSharedPreferences(
//                            "OneKeyUFApplicationList", Context.MODE_PRIVATE);
//                    String pkgNameList = sharedPreferences.getString("pkgName", "");
//                    sharedPreferences.edit()
//                            .putString(
//                                    "pkgName",
//                                    pkgNameList.replace("|" + pkgName + "|", ""))
//                            .apply();
//                }
            }
        }
    }
//
//    private boolean checkOnekeyList(Context context,String pkgName,String UFF){
//        String[] autoFreezePkgNameList = context.getSharedPreferences(
//                UFF, Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
//        for(String s: autoFreezePkgNameList){
//            if(s.replaceAll("\\|","").equals(pkgName))
//                return true;
//        }
//        return false;
//    }
}
