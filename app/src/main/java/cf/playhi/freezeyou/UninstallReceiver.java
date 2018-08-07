package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import static cf.playhi.freezeyou.Support.deleteNotifying;
import static cf.playhi.freezeyou.Support.removeFromOneKeyList;

public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)) {
            String pkgName = intent.getDataString();
            if (pkgName != null) {
                pkgName = pkgName.replace("package:", "");
                removeFromOneKeyList(context, "AutoFreezeApplicationList", pkgName);
                removeFromOneKeyList(context, "OneKeyUFApplicationList", pkgName);
                removeFromOneKeyList(context, "FreezeOnceQuit", pkgName);
                //清理被卸载应用程序的图标数据
                File file = new File(context.getFilesDir() + "/icon/" + pkgName + ".png");
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
                //清理被卸载应用程序的名称
                context.getSharedPreferences("NameOfPackages", Context.MODE_PRIVATE).edit().remove(pkgName).apply();
                //清理可能存在的通知栏提示重新显示数据
                deleteNotifying(context,pkgName);
            }
        }
    }
}
