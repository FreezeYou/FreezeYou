package cf.playhi.freezeyou.utils;

import android.content.Context;

import java.io.DataOutputStream;

import cf.playhi.freezeyou.MainApplication;
import cf.playhi.freezeyou.R;

import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.lesserToast;
import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.avoidFreezeForegroundApplications;
import static cf.playhi.freezeyou.utils.ProcessUtils.destroyProcess;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public final class ForceStopUtils {

    public static void forceStop(
            Context context, String[] pkgNameList) {
        if (pkgNameList != null) {
            String currentPackage = " ";
            if (avoidFreezeForegroundApplications.getValue(null)) {
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
                    if (!lesserToast.getValue(null)) {
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
