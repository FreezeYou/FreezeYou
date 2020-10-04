package cf.playhi.freezeyou.fuf;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils;
import cf.playhi.freezeyou.utils.FUFUtils;
import cf.playhi.freezeyou.utils.ProcessUtils;

import static cf.playhi.freezeyou.DeviceAdminReceiver.getComponentName;

public class FUFSinglePackage {

    private final Context mContext;

    private String mSinglePackageName;

    private int mActionMode;

    private int mAPIMode;

    public static final int ACTION_MODE_FREEZE = 0;
    public static final int ACTION_MODE_UNFREEZE = 1;

    public static final int ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT = 1;
    public static final int ERROR_NO_ERROR_SUCCESS = 0;
    public static final int ERROR_OTHER = -1;
    public static final int ERROR_SINGLE_PACKAGE_NAME_IS_NULL = -2;
    public static final int ERROR_DEVICE_ANDROID_VERSION_TOO_LOW = -3;
    public static final int ERROR_NO_ROOT_PERMISSION = -4;
    public static final int ERROR_DPM_EXECUTE_FAILED_FROM_SYSTEM = -5;
    public static final int ERROR_NOT_DEVICE_POLICY_MANAGER = -6;
    public static final int ERROR_NO_SUCH_API_MODE = -7;
    public static final int ERROR_NOT_SYSTEM_APP = -8;

    /**
     * 使用 FreezeYou 的 自动（免ROOT(DPM)/ROOT(DISABLE)） 模式
     * 推荐直接使用 {@link #API_FREEZEYOU_MROOT_DPM}
     * 与 {@link #API_FREEZEYOU_ROOT_DISABLE_ENABLE}
     * 与 {@link #API_FREEZEYOU_ROOT_UNHIDE_HIDE}
     */
    @Deprecated
    public static final int API_FREEZEYOU_LEGACY_AUTO = 0;

    /**
     * 使用 FreezeYou 的 免ROOT(DPM) 模式
     */
    public static final int API_FREEZEYOU_MROOT_DPM = 1;

    /**
     * 使用 FreezeYou 的 ROOT(DISABLE) 模式
     */
    public static final int API_FREEZEYOU_ROOT_DISABLE_ENABLE = 2;

    /**
     * 使用 FreezeYou 的 ROOT(UNHIDE) 模式
     */
    public static final int API_FREEZEYOU_ROOT_UNHIDE_HIDE = 3;

    /**
     * 使用 FreezeYou 的 System App (DISABLE_UNTIL_USED) 模式
     */
    public static final int API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED = 4;

    /**
     * 使用 FreezeYou 的 System App (DISABLE_USER) 模式
     */
    public static final int API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER = 5;

    /**
     * 使用 FreezeYou 的 System App (DISABLE) 模式
     */
    public static final int API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE = 6;

    public FUFSinglePackage(Context context) {
        this.mContext = context;
    }

    public FUFSinglePackage setSinglePackageName(String singlePackageName) {
        this.mSinglePackageName = singlePackageName;
        return this;
    }

    public FUFSinglePackage setActionMode(int actionMode) {
        this.mActionMode = actionMode;
        return this;
    }

    public FUFSinglePackage setAPIMode(int apiMode) {
        this.mAPIMode = apiMode;
        return this;
    }

    public int commit() {
        int returnCode;
        switch (this.mAPIMode) {
            case API_FREEZEYOU_LEGACY_AUTO:
                returnCode = pureExecuteAPIAutoAction();
                break;
            case API_FREEZEYOU_MROOT_DPM:
                returnCode = pureExecuteAPIDPMAction();
                break;
            case API_FREEZEYOU_ROOT_DISABLE_ENABLE:
                returnCode = pureExecuteAPIRootAction(false);
                break;
            case API_FREEZEYOU_ROOT_UNHIDE_HIDE:
                returnCode = pureExecuteAPIRootAction(true);
                break;
            case API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE:
                returnCode = pureExecuteAPISystemAppDisabledAction();
                break;
            case API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED:
                returnCode = pureExecuteAPISystemAppDisabledUntilUsedAction();
                break;
            case API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER:
                returnCode = pureExecuteAPISystemAppDisabledUserAction();
                break;
            default:
                returnCode = ERROR_NO_SUCH_API_MODE;
                break;
        }
        return returnCode;
    }

    private int pureExecuteAPIAutoAction() {
        int returnValue;
        if (mActionMode == ACTION_MODE_FREEZE) {
            if (Build.VERSION.SDK_INT >= 21 && DevicePolicyManagerUtils.isDeviceOwner(mContext)) {
                returnValue = pureExecuteAPIDPMAction();
            } else {
                returnValue = pureExecuteAPIRootAction();
            }
        } else {
            if (FUFUtils.checkMRootFrozen(mContext, mSinglePackageName)) {
                returnValue = pureExecuteAPIDPMAction();
            } else {
                returnValue = pureExecuteAPIRootAction();
            }
        }
        return returnValue;
    }

    private int pureExecuteAPIDPMAction() {
        if (mSinglePackageName == null)
            return ERROR_SINGLE_PACKAGE_NAME_IS_NULL;

        if (Build.VERSION.SDK_INT < 21)
            return ERROR_DEVICE_ANDROID_VERSION_TOO_LOW;

        if (!DevicePolicyManagerUtils.isDeviceOwner(mContext))
            return ERROR_NOT_DEVICE_POLICY_MANAGER;

        int returnValue = ERROR_OTHER;
        boolean hidden = mActionMode == ACTION_MODE_FREEZE;

        if (!hidden &&
                !DevicePolicyManagerUtils.getDevicePolicyManager(mContext)
                        .isApplicationHidden(getComponentName(mContext), mSinglePackageName)) {
            returnValue = ERROR_NO_ERROR_SUCCESS;
        } else {
            if ((!"cf.playhi.freezeyou".equals(mSinglePackageName))) {
                if (DevicePolicyManagerUtils.getDevicePolicyManager(mContext).setApplicationHidden(
                        getComponentName(mContext), mSinglePackageName, hidden)) {
                    returnValue = ERROR_NO_ERROR_SUCCESS;
                } else {
                    returnValue = ERROR_DPM_EXECUTE_FAILED_FROM_SYSTEM;
                }
            }
        }


        return returnValue;
    }

    private int pureExecuteAPIRootAction() {
        int returnValue = ERROR_OTHER;
        switch (this.mAPIMode) {
            case API_FREEZEYOU_LEGACY_AUTO:
            case API_FREEZEYOU_ROOT_DISABLE_ENABLE:
                returnValue = pureExecuteAPIRootAction(false);
                break;
            case API_FREEZEYOU_ROOT_UNHIDE_HIDE:
                returnValue = pureExecuteAPIRootAction(true);
                break;
            default:
                break;
        }
        return returnValue;
    }

    private int pureExecuteAPIRootAction(boolean hideMode) {
        if (mSinglePackageName == null)
            return ERROR_SINGLE_PACKAGE_NAME_IS_NULL;

        int returnValue = ERROR_OTHER;
        boolean enable = mActionMode == ACTION_MODE_UNFREEZE;
        if ((!"cf.playhi.freezeyou".equals(mSinglePackageName))) {
            try {
                final int exitValue = ProcessUtils.fAURoot(mSinglePackageName, enable, hideMode);
                if (exitValue == 0) {
                    returnValue = ERROR_NO_ERROR_SUCCESS;
                } else {
                    returnValue = ERROR_OTHER;
                }
            } catch (final Exception e) {
                e.printStackTrace();
                String eMsg = e.getMessage();
                if (eMsg != null &&
                        (eMsg.toLowerCase().contains("permission denied")
                                || eMsg.toLowerCase().contains("not found"))) {
                    returnValue = ERROR_NO_ROOT_PERMISSION;
                }
            }
        }
        return returnValue;
    }

    private int pureExecuteAPISystemAppDisabledUntilUsedAction() {
        if (mSinglePackageName == null)
            return ERROR_SINGLE_PACKAGE_NAME_IS_NULL;

        if (Build.VERSION.SDK_INT < 18)
            return ERROR_DEVICE_ANDROID_VERSION_TOO_LOW;

        if (!FUFUtils.isSystemApp(mContext))
            return ERROR_NOT_SYSTEM_APP;

        int returnValue = ERROR_OTHER;
        boolean freeze = mActionMode == ACTION_MODE_FREEZE;

        if ((!"cf.playhi.freezeyou".equals(mSinglePackageName))) {
            mContext.getPackageManager().setApplicationEnabledSetting(
                    mSinglePackageName,
                    freeze ?
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED :
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0
            );
            returnValue = ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT;
        }

        return returnValue;
    }

    private int pureExecuteAPISystemAppDisabledUserAction() {
        if (mSinglePackageName == null)
            return ERROR_SINGLE_PACKAGE_NAME_IS_NULL;

        if (!FUFUtils.isSystemApp(mContext))
            return ERROR_NOT_SYSTEM_APP;

        int returnValue = ERROR_OTHER;
        boolean freeze = mActionMode == ACTION_MODE_FREEZE;

        if ((!"cf.playhi.freezeyou".equals(mSinglePackageName))) {
            mContext.getPackageManager().setApplicationEnabledSetting(
                    mSinglePackageName,
                    freeze ?
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER :
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0
            );
            returnValue = ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT;
        }

        return returnValue;
    }

    private int pureExecuteAPISystemAppDisabledAction() {
        if (mSinglePackageName == null)
            return ERROR_SINGLE_PACKAGE_NAME_IS_NULL;

        if (!FUFUtils.isSystemApp(mContext))
            return ERROR_NOT_SYSTEM_APP;

        int returnValue = ERROR_OTHER;
        boolean freeze = mActionMode == ACTION_MODE_FREEZE;

        if ((!"cf.playhi.freezeyou".equals(mSinglePackageName))) {
            mContext.getPackageManager().setApplicationEnabledSetting(
                    mSinglePackageName,
                    freeze ?
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0
            );
            returnValue = ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT;
        }

        return returnValue;
    }

}
