package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
            }
        }
    }
}
