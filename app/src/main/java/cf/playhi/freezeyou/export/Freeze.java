package cf.playhi.freezeyou.export;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import cf.playhi.freezeyou.fuf.FUFSinglePackage;
import cf.playhi.freezeyou.utils.ApplicationInfoUtils;
import cf.playhi.freezeyou.utils.FUFUtils;

import static cf.playhi.freezeyou.export.FUFMode.MODE_AUTO;
import static cf.playhi.freezeyou.export.FUFMode.MODE_DPM;
import static cf.playhi.freezeyou.export.FUFMode.MODE_LEGACY_AUTO;
import static cf.playhi.freezeyou.export.FUFMode.MODE_MROOT;
import static cf.playhi.freezeyou.export.FUFMode.MODE_ROOT;
import static cf.playhi.freezeyou.export.FUFMode.MODE_ROOT_DISABLE_ENABLE;
import static cf.playhi.freezeyou.export.FUFMode.MODE_ROOT_HIDE_UNHIDE;
import static cf.playhi.freezeyou.export.FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE;
import static cf.playhi.freezeyou.export.FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED;
import static cf.playhi.freezeyou.export.FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE_USER;
import static cf.playhi.freezeyou.utils.DevicePolicyManagerUtils.isDeviceOwner;
import static cf.playhi.freezeyou.utils.FUFUtils.sendStatusChangedBroadcast;
import static cf.playhi.freezeyou.utils.NotificationUtils.deleteNotification;
import static cf.playhi.freezeyou.utils.TasksUtils.onFApplications;

public class Freeze extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Context context = getContext();
        Bundle bundle = new Bundle();
        if (method != null && extras != null) {
            String pkgName = extras.getString("packageName");
            if (context == null) {
                bundle.putInt("result", -1);
            } else if (pkgName == null) {
                bundle.putInt("result", -2);
            } else {
                switch (method) {
                    case MODE_AUTO:
                        if (ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context) == null) {
                            bundle.putInt("result", 998);
                        } else {
                            if (Build.VERSION.SDK_INT >= 21 && isDeviceOwner(context)) {
                                if (FUFUtils.checkMRootFrozen(context, pkgName)) {
                                    bundle.putInt("result", 999);
                                } else {
                                    if (FUFUtils.processMRootAction(context, pkgName,
                                            null, null, true,
                                            false, false, null,
                                            false, false)) {
                                        bundle.putInt("result", 0);
                                    } else {
                                        bundle.putInt("result", -3);
                                    }
                                }
                            } else if (!FUFUtils.checkRootFrozen(context, pkgName, null)) {
                                if (FUFUtils.processRootAction(pkgName, null, null,
                                        context, false, false, false,
                                        null, false, false)) {
                                    bundle.putInt("result", 0);
                                } else {
                                    bundle.putInt("result", -4);
                                }
                            } else {
                                bundle.putInt("result", 999);
                            }
                        }
                        return bundle;
                    case MODE_MROOT:
                        if (ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context) == null) {
                            bundle.putInt("result", 998);
                        } else {
                            if (FUFUtils.checkMRootFrozen(context, pkgName)) {
                                bundle.putInt("result", 999);
                            } else {
                                if (FUFUtils.processMRootAction(context, pkgName, null,
                                        null, true, false,
                                        false, null,
                                        false, false)) {
                                    bundle.putInt("result", 0);
                                } else {
                                    bundle.putInt("result", -3);
                                }
                            }
                        }
                        return bundle;
                    case MODE_ROOT:
                        if (ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context) == null) {
                            bundle.putInt("result", 998);
                        } else {
                            if (FUFUtils.checkRootFrozen(context, pkgName, null)) {
                                bundle.putInt("result", 999);
                            } else {
                                if (FUFUtils.processRootAction(pkgName, null, null,
                                        context, false, false, false,
                                        null, false, false)) {
                                    bundle.putInt("result", 0);
                                } else {
                                    bundle.putInt("result", -4);
                                }
                            }
                        }
                        bundle.putInt("result", 0);
                        return bundle;
                    case MODE_DPM:
                        bundle.putInt(
                                "result",
                                doApiV2Action(
                                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_MROOT_DPM)
                        );
                        return bundle;
                    case MODE_ROOT_DISABLE_ENABLE:
                        bundle.putInt(
                                "result",
                                doApiV2Action(
                                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_ROOT_DISABLE_ENABLE)
                        );
                        return bundle;
                    case MODE_ROOT_HIDE_UNHIDE:
                        bundle.putInt(
                                "result",
                                doApiV2Action(
                                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_ROOT_UNHIDE_HIDE)
                        );
                        return bundle;
                    case MODE_LEGACY_AUTO:
                        bundle.putInt(
                                "result",
                                doApiV2Action(
                                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_LEGACY_AUTO)
                        );
                        return bundle;
                    case MODE_SYSTEM_APP_ENABLE_DISABLE:
                        bundle.putInt(
                                "result",
                                doApiV2Action(
                                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE)
                        );
                        return bundle;
                    case MODE_SYSTEM_APP_ENABLE_DISABLE_USER:
                        bundle.putInt(
                                "result",
                                doApiV2Action(
                                        context, pkgName,
                                        FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER
                                )
                        );
                        return bundle;
                    case MODE_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED:
                        bundle.putInt(
                                "result",
                                doApiV2Action(
                                        context, pkgName,
                                        FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED
                                )
                        );
                        return bundle;
                    default:
                        break;
                }
            }
        }
        return bundle;
    }

    private int doApiV2Action(Context context, @NonNull String pkgName, int apiMode) {

        int result =
                new FUFSinglePackage(context, pkgName, FUFSinglePackage.ACTION_MODE_FREEZE, apiMode)
                        .commit();

        if (FUFUtils.preProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                context, result, false)) {
            sendStatusChangedBroadcast(context);
            onFApplications(context, pkgName);
            deleteNotification(context, pkgName);
        }

        return result;
    }

}
