package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyAction_MRoot;
import static cf.playhi.freezeyou.Support.oneKeyAction_Root;

public class OneKeyFreeze extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = this;
        String[] pkgNameList = getApplicationContext().getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
            oneKeyAction_MRoot(activity,activity,true,pkgNameList);
            finish();
        } else {
            oneKeyAction_Root(activity,activity,true,pkgNameList);
        }

    }
}
