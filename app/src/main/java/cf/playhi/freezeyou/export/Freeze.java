package cf.playhi.freezeyou.export;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import cf.playhi.freezeyou.utils.Support;

public class Freeze extends ContentProvider {

    private static final String MODE_ROOT = "MODE_ROOT";
    private static final String MODE_MROOT = "MODE_MROOT";
    private static final String MODE_AUTO = "MODE_AUTO";

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
            switch (method) {
                case MODE_AUTO:
                    if (context == null) {
                        bundle.putInt("result", -1);
                    } else if (pkgName == null) {
                        bundle.putInt("result", -2);
                    } else {
                        if (Support.checkMRootFrozen(context, pkgName)) {
                            if (Support.processMRootAction(context, pkgName, null, null, true, false, false, null, false)) {
                                bundle.putInt("result", 0);
                            } else {
                                bundle.putInt("result", -3);
                            }
                        } else if (Support.checkRootFrozen(context, pkgName, null)) {
                            if (Support.processRootAction(pkgName, null, null, context, false, false, false, null, false)) {
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
                    if (context == null) {
                        bundle.putInt("result", -1);
                    } else if (pkgName == null) {
                        bundle.putInt("result", -2);
                    } else {
                        if (Support.checkMRootFrozen(context, pkgName)) {
                            if (Support.processMRootAction(context, pkgName, null, null, true, false, false, null, false)) {
                                bundle.putInt("result", 0);
                            } else {
                                bundle.putInt("result", -3);
                            }
                        } else {
                            bundle.putInt("result", 999);
                        }
                    }
                    return bundle;
                case MODE_ROOT:
                    if (context == null) {
                        bundle.putInt("result", -1);
                    } else if (pkgName == null) {
                        bundle.putInt("result", -2);
                    } else {
                        if (Support.checkRootFrozen(context, pkgName, null)) {
                            if (Support.processRootAction(pkgName, null, null, context, false, false, false, null, false)) {
                                bundle.putInt("result", 0);
                            } else {
                                bundle.putInt("result", -4);
                            }
                        } else {
                            bundle.putInt("result", 999);
                        }
                    }
                    bundle.putInt("result", 0);
                    return bundle;
                default:
                    break;
            }
        }
        return bundle;
    }

}
