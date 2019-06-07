package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import static cf.playhi.freezeyou.NotificationUtils.deleteNotifying;
import static cf.playhi.freezeyou.OneKeyListUtils.removeFromOneKeyList;

public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
            String pkgName = intent.getDataString();
            if (pkgName != null) {
                pkgName = pkgName.replace("package:", "");
                if (ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context) == null) {
                    removeFromOneKeyList(context, context.getString(R.string.sAutoFreezeApplicationList), pkgName);
                    removeFromOneKeyList(context, context.getString(R.string.sOneKeyUFApplicationList), pkgName);
                    removeFromOneKeyList(context, context.getString(R.string.sFreezeOnceQuit), pkgName);
                    //清理被卸载应用程序的图标数据
                    File file = new File(context.getFilesDir() + "/icon/" + pkgName + ".png");
                    if (file.exists() && file.isFile()) {
                        file.delete();
                    }
                    File file2 = new File(context.getCacheDir() + "/icon/" + pkgName + ".png");
                    if (file2.exists() && file2.isFile()) {
                        file2.delete();
                    }
                    //清理被卸载应用程序的名称
                    context.getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE)
                            .edit().remove(pkgName).apply();
                    //清理可能存在的通知栏提示重新显示数据
                    deleteNotifying(context, pkgName);
                }
            }
        }
    }
}
