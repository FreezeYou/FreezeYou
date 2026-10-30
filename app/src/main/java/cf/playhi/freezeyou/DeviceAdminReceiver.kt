package cf.playhi.freezeyou

import android.app.admin.DeviceAdminReceiver as FrameworkDeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import cf.playhi.freezeyou.utils.ToastUtils.showToast

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
// The framework type is aliased. An explicit import of the same simple name
// makes DeviceAdminReceiver::class resolve to android.app.admin.DeviceAdminReceiver,
// so DevicePolicyManager rejects the component as "No active admin".
open class DeviceAdminReceiver : FrameworkDeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
//        showToast(context, R.string.activated);
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return context.getString(R.string.disableConfirmation)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        showToast(context, R.string.disabled)
    }

    companion object {
        @JvmStatic
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, DeviceAdminReceiver::class.java)
        }
    }
}
