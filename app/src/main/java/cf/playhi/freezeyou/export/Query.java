package cf.playhi.freezeyou.export;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.Objects;

import cf.playhi.freezeyou.utils.ApplicationInfoUtils;
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils;
import cf.playhi.freezeyou.utils.FUFUtils;

import static cf.playhi.freezeyou.export.FUFMode.MODE_DPM;
import static cf.playhi.freezeyou.export.FUFMode.MODE_LEGACY_AUTO;
import static cf.playhi.freezeyou.export.FUFMode.MODE_PROFILE_OWNER;
import static cf.playhi.freezeyou.export.FUFMode.MODE_ROOT_DISABLE_ENABLE;
import static cf.playhi.freezeyou.export.FUFMode.MODE_ROOT_HIDE_UNHIDE;
import static cf.playhi.freezeyou.export.FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE;
import static cf.playhi.freezeyou.export.FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED;
import static cf.playhi.freezeyou.export.FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE_USER;
import static cf.playhi.freezeyou.export.FUFMode.MODE_UNKNOWN;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_LEGACY_AUTO;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_MROOT_DPM;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_MROOT_PROFILE_OWNER;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_ROOT_DISABLE_ENABLE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_ROOT_UNHIDE_HIDE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED;
import static cf.playhi.freezeyou.fuf.FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER;
import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageStringKeys.selectFUFMode;

public class Query extends ContentProvider {

    private static final String QUERY_MODE = "QUERY_MODE";
    private static final String QUERY_FREEZE_STATUS = "QUERY_FREEZE_STATUS";
    private static final String QUERY_IF_CAN_INSTALL_APPLICATIONS_STATUS = "QUERY_IF_CAN_INSTALL_APPLICATIONS_STATUS";
    private static final String QUERY_MODE_V2 = "QUERY_MODE_V2";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(
            @NonNull Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder
    ) {
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Bundle call(@NonNull String method, String arg, Bundle extras) {
        Context context = getContext();
        Bundle bundle = new Bundle();
        if (method == null || extras == null) {
            return bundle;
        }
        String queryPkg = extras.getString("packageName");
        switch (method) {
            case QUERY_MODE:
                if (context != null && DevicePolicyManagerUtils.isDeviceOwner(context)) {
                    bundle.putString("currentMode", "dpm");
                } else if (FUFUtils.checkRootPermission()) {
                    bundle.putString("currentMode", "root");
                } else {
                    bundle.putString("currentMode", "unavailable");
                }
                return bundle;
            case QUERY_MODE_V2:
                if (context == null) return bundle;

                switch (Integer.parseInt(Objects.requireNonNull(selectFUFMode.getValue(null)))) {
                    case API_FREEZEYOU_MROOT_DPM:
                        bundle.putString("currentMode", MODE_DPM);
                        break;
                    case API_FREEZEYOU_ROOT_DISABLE_ENABLE:
                        bundle.putString("currentMode", MODE_ROOT_DISABLE_ENABLE);
                        break;
                    case API_FREEZEYOU_ROOT_UNHIDE_HIDE:
                        bundle.putString("currentMode", MODE_ROOT_HIDE_UNHIDE);
                        break;
                    case API_FREEZEYOU_LEGACY_AUTO:
                        bundle.putString("currentMode", MODE_LEGACY_AUTO);
                        break;
                    case API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED:
                        bundle.putString("currentMode", MODE_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED);
                        break;
                    case API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER:
                        bundle.putString("currentMode", MODE_SYSTEM_APP_ENABLE_DISABLE_USER);
                        break;
                    case API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE:
                        bundle.putString("currentMode", MODE_SYSTEM_APP_ENABLE_DISABLE);
                        break;
                    case API_FREEZEYOU_MROOT_PROFILE_OWNER:
                        bundle.putString("currentMode", MODE_PROFILE_OWNER);
                        break;
                    default:
                        bundle.putString("currentMode", MODE_UNKNOWN);
                        break;
                }
                return bundle;
            case QUERY_FREEZE_STATUS:
                if (context == null) {
                    bundle.putInt("status", -1);
                } else if (queryPkg == null) {
                    bundle.putInt("status", -2);
                } else {
                    if (ApplicationInfoUtils.getApplicationInfoFromPkgName(queryPkg, context) == null) {
                        bundle.putInt("status", 998);
                    } else {
                        boolean dpmFrozen = FUFUtils.checkMRootFrozen(context, queryPkg);
                        boolean rootFrozen = FUFUtils.checkRootFrozen(context, queryPkg, null);
                        if (dpmFrozen && rootFrozen) {
                            bundle.putInt("status", 3);
                        } else if (dpmFrozen) {
                            bundle.putInt("status", 2);
                        } else if (rootFrozen) {
                            bundle.putInt("status", 1);
                        } else {
                            bundle.putInt("status", 0);
                        }
                    }
                }
                return bundle;
            case QUERY_IF_CAN_INSTALL_APPLICATIONS_STATUS:
                if (context == null) {
                    bundle.putBooleanArray("status", new boolean[]{false, false, false, false}); // 可用状态、installActivityEnabled、hasRootPerm、hasDpmPerm
                } else {
                    boolean installActivityEnabled, hasRootPerm, hasDpmPerm;
                    switch (context.getPackageManager().getComponentEnabledSetting(
                            new ComponentName(context, "cf.playhi.freezeyou.InstallPackagesActivity"))) {
                        case android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                            installActivityEnabled = true;
                            break;
                        case android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
                        case android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                        case android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED:
                        case android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
                        default:
                            installActivityEnabled = false;
                            break;
                    }
                    hasDpmPerm = DevicePolicyManagerUtils.isDeviceOwner(context);
                    hasRootPerm = FUFUtils.checkRootPermission();
                    bundle.putBooleanArray(
                            "status",
                            new boolean[]{
                                    installActivityEnabled && (hasDpmPerm || hasRootPerm),
                                    installActivityEnabled,
                                    hasRootPerm,
                                    hasDpmPerm
                            }
                    );
                }
                return bundle;
            default:
                break;
        }
        return bundle;
    }

}
