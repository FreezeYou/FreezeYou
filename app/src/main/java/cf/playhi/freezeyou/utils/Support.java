package cf.playhi.freezeyou.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import net.grandcentrix.tray.AppPreferences;

import java.util.Locale;

import cf.playhi.freezeyou.Freeze;
import cf.playhi.freezeyou.R;

import static cf.playhi.freezeyou.LauncherShortcutUtils.checkSettingsAndRequestCreateShortcut;
import static cf.playhi.freezeyou.utils.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ClipboardUtils.copyToClipboard;
import static cf.playhi.freezeyou.utils.OneKeyListUtils.addToOneKeyList;
import static cf.playhi.freezeyou.utils.OneKeyListUtils.existsInOneKeyList;
import static cf.playhi.freezeyou.utils.OneKeyListUtils.removeFromOneKeyList;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public final class Support {

    private static void makeDialog(final String title, final String message, final Context context, final ApplicationInfo applicationInfo, final String pkgName, final String target, final String tasks, final boolean enabled, final Activity activity, final boolean finish) {
        AlertDialog.Builder builder =
                buildAlertDialog(context, getApplicationIcon(context, pkgName, applicationInfo, true), message, title)
                        .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FUFUtils.checkAndDoActivityFinish(activity, finish);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                FUFUtils.checkAndDoActivityFinish(activity, finish);
                            }
                        });
        if (enabled) {
            builder.setPositiveButton(R.string.launch, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FUFUtils.checkAndStartApp(context, pkgName, target, tasks, activity, finish);
                }
            });
            builder.setNegativeButton(R.string.freeze, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FUFUtils.processFreezeAction(context, pkgName, target, tasks, true, activity, finish);
                }
            });
        } else {
            builder.setPositiveButton(R.string.unfreeze, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FUFUtils.processUnfreezeAction(context, pkgName, target, tasks, true, false, activity, finish);
                }
            });
        }
        builder.create().show();
    }

    public static void shortcutMakeDialog(Context context, String title, String message, final Activity activity, final ApplicationInfo applicationInfo, final String pkgName, String target, String tasks, int ot, boolean auto, boolean finish) {
        if (new AppPreferences(context).getBoolean("openAndUFImmediately", false) && auto) {
            if (ot == 2) {
                FUFUtils.checkAndStartApp(context, pkgName, target, tasks, activity, finish);
            } else {
                FUFUtils.processUnfreezeAction(context, pkgName, target, tasks, true, true, activity, finish);//ot==1
            }
        } else {
            makeDialog(title, message, context, applicationInfo, pkgName, target, tasks, ot == 2, activity, finish);
        }
    }

    public static void checkAddOrRemove(Context context, String pkgNames, String pkgName, String oneKeyName) {
        if (existsInOneKeyList(pkgNames, pkgName)) {
            showToast(context,
                    removeFromOneKeyList(context,
                            oneKeyName,
                            pkgName) ? R.string.removed : R.string.removeFailed);
        } else {
            showToast(context,
                    addToOneKeyList(context,
                            oneKeyName,
                            pkgName) ? R.string.added : R.string.addFailed);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (context.getString(R.string.sFreezeOnceQuit).equals(oneKeyName)) {
                if (!preferences.getBoolean("freezeOnceQuit", false)) {
                    preferences.edit().putBoolean("freezeOnceQuit", true).apply();
                    new AppPreferences(context).put("freezeOnceQuit", true);
                }
                AccessibilityUtils.checkAndRequestIfAccessibilitySettingsOff(context);
            }
        }
    }

    public static void showChooseActionPopupMenu(final Context context, View view, final String pkgName, final String name) {
        showChooseActionPopupMenu(context, view, pkgName, name, false, null);
    }

    public static void showChooseActionPopupMenu(final Context context, View view, final String pkgName, final String name, boolean canRemoveItem, final SharedPreferences folderPkgListSp) {
        generateChooseActionPopupMenu(context, view, pkgName, name, canRemoveItem, folderPkgListSp).show();
    }

    private static PopupMenu generateChooseActionPopupMenu(final Context context, View view, final String pkgName, final String name, final boolean canRemoveItem, final SharedPreferences folderPkgListSp) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.main_single_choose_action_menu);

        final AppPreferences sharedPreferences = new AppPreferences(context);

        final String pkgNames = sharedPreferences.getString(context.getString(R.string.sAutoFreezeApplicationList), "");
        if (existsInOneKeyList(pkgNames, pkgName)) {
            popup.getMenu().findItem(R.id.main_sca_menu_addToOneKeyList).setTitle(R.string.removeFromOneKeyList);
        }

        final String FreezeOnceQuitPkgNames = sharedPreferences.getString(context.getString(R.string.sFreezeOnceQuit), "");
        if (existsInOneKeyList(FreezeOnceQuitPkgNames, pkgName)) {
            popup.getMenu().findItem(R.id.main_sca_menu_addToFreezeOnceQuit).setTitle(R.string.removeFromFreezeOnceQuit);
        }

        final String UFPkgNames = sharedPreferences.getString(context.getString(R.string.sOneKeyUFApplicationList), "");
        if (existsInOneKeyList(UFPkgNames, pkgName)) {
            popup.getMenu().findItem(R.id.main_sca_menu_addToOneKeyUFList).setTitle(R.string.removeFromOneKeyUFList);
        }

        if (FUFUtils.realGetFrozenStatus(context, pkgName, null)) {
            popup.getMenu().findItem(R.id.main_sca_menu_disableAEnable).setTitle(R.string.UfSlashRun);
        } else {
            popup.getMenu().findItem(R.id.main_sca_menu_disableAEnable).setTitle(R.string.freezeSlashRun);
        }

        if (!canRemoveItem) {
            popup.getMenu().removeItem(R.id.main_sca_menu_removeFromTheList);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.main_sca_menu_addToFreezeOnceQuit:
                        Support.checkAddOrRemove(context, FreezeOnceQuitPkgNames, pkgName, context.getString(R.string.sFreezeOnceQuit));
                        break;
                    case R.id.main_sca_menu_addToOneKeyList:
                        Support.checkAddOrRemove(context, pkgNames, pkgName, context.getString(R.string.sAutoFreezeApplicationList));
                        break;
                    case R.id.main_sca_menu_addToOneKeyUFList:
                        Support.checkAddOrRemove(context, UFPkgNames, pkgName, context.getString(R.string.sOneKeyUFApplicationList));
                        break;
                    case R.id.main_sca_menu_appDetail:
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", pkgName, null);
                        intent.setData(uri);
                        try {
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showToast(context, e.getLocalizedMessage());
                        }
                        break;
                    case R.id.main_sca_menu_copyPkgName:
                        showToast(context, copyToClipboard(context, pkgName) ? R.string.success : R.string.failed);
                        break;
                    case R.id.main_sca_menu_disableAEnable:
                        if (!(context.getString(R.string.notAvailable).equals(name))) {
                            context.startActivity(new Intent(context, Freeze.class).putExtra("pkgName", pkgName).putExtra("auto", false));
                        }
                        break;
                    case R.id.main_sca_menu_createDisEnableShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                name,
                                pkgName,
                                getApplicationIcon(
                                        context,
                                        pkgName,
                                        ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context),
                                        false),
                                Freeze.class,
                                "FreezeYou! " + pkgName,
                                context);
                        break;
                    case R.id.main_sca_menu_removeFromTheList:
                        if (folderPkgListSp != null) {
                            String folderPkgs = folderPkgListSp.getString("pkgS", "");
                            if (existsInOneKeyList(folderPkgs, pkgName)) {
                                folderPkgListSp.edit()
                                        .putString("pkgS", folderPkgs.replace(pkgName + ",", ""))
                                        .apply();
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        return popup;
    }

    public static void checkLanguage(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();

        config.locale = getLocal(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(config.locale);
        }

        resources.updateConfiguration(config, dm);
    }

    private static Locale getLocal(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context).getString("languagePref", "Default");

        if (s == null) {
            s = "Default";
        }

        switch (s) {
            case "tc":
                return Locale.TRADITIONAL_CHINESE;
            case "sc":
                return Locale.SIMPLIFIED_CHINESE;
            case "en":
                return Locale.ENGLISH;
            case "Default":
            default:
                return Locale.getDefault();
        }
    }

}
