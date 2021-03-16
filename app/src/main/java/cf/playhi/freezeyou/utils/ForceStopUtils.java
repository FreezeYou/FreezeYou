package cf.playhi.freezeyou.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import net.grandcentrix.tray.AppPreferences;

import java.io.DataOutputStream;

import cf.playhi.freezeyou.AskRunActivity;
import cf.playhi.freezeyou.DeviceAdminReceiver;
import cf.playhi.freezeyou.FUFService;
import cf.playhi.freezeyou.MainApplication;
import cf.playhi.freezeyou.MyNotificationListenerService;
import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.fuf.FUFSinglePackage;

import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ACTION_MODE_FREEZE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ACTION_MODE_UNFREEZE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_LEGACY_AUTO;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_MROOT_DPM;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_ROOT_DISABLE_ENABLE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_ROOT_UNHIDE_HIDE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_DEVICE_ANDROID_VERSION_TOO_LOW;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_DPM_EXECUTE_FAILED_FROM_SYSTEM;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_NOT_DEVICE_POLICY_MANAGER;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_NOT_SYSTEM_APP;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_NO_ERROR_SUCCESS;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_NO_ROOT_PERMISSION;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_NO_SUCH_API_MODE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_OTHER;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ERROR_SINGLE_PACKAGE_NAME_IS_NULL;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.getDevicePolicyManager;
import static cf.playhi.freezeyou.utils.NotificationUtils.createFUFQuickNotification;
import static cf.playhi.freezeyou.utils.NotificationUtils.deleteNotification;
import static cf.playhi.freezeyou.utils.ProcessUtils.destroyProcess;
import static cf.playhi.freezeyou.utils.ServiceUtils.startService;
import static cf.playhi.freezeyou.utils.TasksUtils.onFApplications;
import static cf.playhi.freezeyou.utils.TasksUtils.onUFApplications;
import static cf.playhi.freezeyou.utils.TasksUtils.runTask;
import static cf.playhi.freezeyou.utils.ToastUtils.showShortToast;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public final class ForceStopUtils {

    public static void forceStop(
            Context context, String[] pkgNameList) {
        if (pkgNameList != null) {
            String currentPackage = " ";
            if (new AppPreferences(context)
                    .getBoolean("avoidFreezeForegroundApplications", false)) {
                currentPackage = MainApplication.getCurrentPackage();
            }
            if (currentPackage == null) currentPackage = " ";
            Process process = null;
            DataOutputStream outputStream = null;
            try {
                process = Runtime.getRuntime().exec("su");
                outputStream = new DataOutputStream(process.getOutputStream());
                for (String aPkgNameList : pkgNameList) {
                    if ((!"cf.playhi.freezeyou".equals(aPkgNameList))) {
                        if (FUFUtils.isAvoidFreezeNotifyingApplicationsEnabledAndAppStillNotifying(context, aPkgNameList)) {
                            FUFUtils.checkAndShowAppStillNotifyingToast(context, aPkgNameList);
                        } else if (currentPackage.equals(aPkgNameList)) {
                            FUFUtils.checkAndShowAppIsForegroundApplicationToast(context, aPkgNameList);
                        } else {
                            try {
                                outputStream.writeBytes("am force-stop " + aPkgNameList + "\n");
                            } catch (Exception e) {
                                e.printStackTrace();
//                                    if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
//                                        showToast(context, R.string.plsRemoveUninstalledApplications);
//                                    }
                            }
                        }
                    }
                }

                outputStream.writeBytes("exit\n");
                outputStream.flush();
                int exitValue = process.waitFor();
                if (exitValue == 0) {
                    if (!(new AppPreferences(context).getBoolean("lesserToast", false))) {
                        showToast(context, R.string.executed);
                    }
                } else {
                    showToast(context, R.string.mayUnrootedOrOtherEx);
                }
                destroyProcess(outputStream, process);
            } catch (Exception e) {
                e.printStackTrace();
                showToast(context, context.getString(R.string.exception) + e.getMessage());
                if (e.getMessage().toLowerCase().contains("permission denied") || e.getMessage().toLowerCase().contains("not found")) {
                    showToast(context, R.string.mayUnrooted);
                }
                destroyProcess(outputStream, process);
            }
        }
    }

}
