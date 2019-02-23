package cf.playhi.freezeyou;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import static cf.playhi.freezeyou.ToastUtils.showToast;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
//        showToast(context, R.string.activated);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.disableConfirmation);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, R.string.disabled);
    }

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceAdminReceiver.class);
    }
}
