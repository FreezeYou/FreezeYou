package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;

public class OneKeyFreeze extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = this;
        String[] pkgNameList = getApplicationContext().getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
            oneKeyActionMRoot(activity,activity,true,pkgNameList);
            finish();
        } else {
            oneKeyActionRoot(activity,activity,true,pkgNameList,true);
        }

    }
}
