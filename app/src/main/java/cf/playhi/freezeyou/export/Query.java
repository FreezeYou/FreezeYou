package cf.playhi.freezeyou.export;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.io.DataOutputStream;

import cf.playhi.freezeyou.utils.ApplicationInfoUtils;
import cf.playhi.freezeyou.utils.DevicePolicyManagerUtils;
import cf.playhi.freezeyou.utils.FUFUtils;

public class Query extends ContentProvider {

    private static final String QUERY_MODE = "QUERY_MODE";
    private static final String QUERY_FREEZE_STATUS = "QUERY_FREEZE_STATUS";

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
            String queryPkg = extras.getString("packageName");
            switch (method) {
                case QUERY_MODE:
                    if (context != null && DevicePolicyManagerUtils.isDeviceOwner(getContext())) {
                        bundle.putString("currentMode", "dpm");
                    } else if (checkRootPermission()) {
                        bundle.putString("currentMode", "root");
                    } else {
                        bundle.putString("currentMode", "unavailable");
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
                default:
                    break;
            }
        }
        return bundle;
    }

    private boolean checkRootPermission() {
        boolean hasPermission = true;
        int value = -1;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            value = process.waitFor();
            try {
                outputStream.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("permission denied") || e.getMessage().toLowerCase().contains("not found")) {
                hasPermission = false;
            }
        }
        return hasPermission && value == 0;
    }

}
