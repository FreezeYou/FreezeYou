package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import net.grandcentrix.tray.AppPreferences;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cf.playhi.freezeyou.adapter.MainAppListSimpleAdapter;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.service.FUFService;
import cf.playhi.freezeyou.service.ForceStopService;
import cf.playhi.freezeyou.ui.AboutActivity;
import cf.playhi.freezeyou.ui.BackupMainActivity;
import cf.playhi.freezeyou.ui.FirstTimeSetupActivity;
import cf.playhi.freezeyou.ui.OneKeyScreenLockImmediatelyActivity;
import cf.playhi.freezeyou.ui.ScheduledTasksManageActivity;
import cf.playhi.freezeyou.ui.SettingsActivity;
import cf.playhi.freezeyou.ui.ShortcutLauncherFolderActivity;
import cf.playhi.freezeyou.ui.fragment.MainActivityAppListFragment;
import cf.playhi.freezeyou.utils.AccessibilityUtils;
import cf.playhi.freezeyou.utils.LauncherShortcutUtils;
import cf.playhi.freezeyou.utils.ServiceUtils;
import cf.playhi.freezeyou.utils.TasksUtils;

import static cf.playhi.freezeyou.utils.LauncherShortcutUtils.checkSettingsAndRequestCreateShortcut;
import static cf.playhi.freezeyou.utils.LauncherShortcutUtils.createShortCut;
import static cf.playhi.freezeyou.utils.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getGrayBitmap;
import static cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.utils.ClipboardUtils.copyToClipboard;
import static cf.playhi.freezeyou.utils.FUFUtils.askRun;
import static cf.playhi.freezeyou.utils.FUFUtils.processFreezeAction;
import static cf.playhi.freezeyou.utils.FUFUtils.processUnfreezeAction;
import static cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus;
import static cf.playhi.freezeyou.utils.MoreUtils.processListFilter;
import static cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite;
import static cf.playhi.freezeyou.utils.OneKeyListUtils.addToOneKeyList;
import static cf.playhi.freezeyou.utils.OneKeyListUtils.removeFromOneKeyList;
import static cf.playhi.freezeyou.utils.Support.showChooseActionPopupMenu;
import static cf.playhi.freezeyou.utils.ThemeUtils.getThemeDot;
import static cf.playhi.freezeyou.utils.ThemeUtils.getThemeFabDotBackground;
import static cf.playhi.freezeyou.utils.ThemeUtils.getThemeSecondDot;
import static cf.playhi.freezeyou.utils.ThemeUtils.getUiTheme;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;
import static cf.playhi.freezeyou.utils.VersionUtils.checkUpdate;
import static cf.playhi.freezeyou.utils.VersionUtils.getVersionCode;
import static cf.playhi.freezeyou.utils.VersionUtils.isOutdated;

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
public class Main extends FreezeYouBaseActivity {

    private final static int APPListViewOnClickMode_chooseAction = 0;
    private final static int APPListViewOnClickMode_autoUFOrFreeze = 1;
    private final static int APPListViewOnClickMode_freezeImmediately = 2;
    private final static int APPListViewOnClickMode_UFImmediately = 3;
    private final static int APPListViewOnClickMode_addToOFList = 4;
    private final static int APPListViewOnClickMode_removeFromOFList = 5;
    private final static int APPListViewOnClickMode_addToOUFList = 6;
    private final static int APPListViewOnClickMode_removeFromOUFList = 7;
    private final static int APPListViewOnClickMode_addToFOQList = 8;
    private final static int APPListViewOnClickMode_removeFromFOQList = 9;
    private final static int APPListViewOnClickMode_UFAndRun = 10;
    private final static int APPListViewOnClickMode_autoUFOrFreezeAndRun = 11;
    private final static int APPListViewOnClickMode_createFUFShortcut = 12;

    private final static int SORT_BY_DEFAULT = 0;
    private final static int SORT_BY_NO = 1;
    private final static int SORT_BY_UF_ASCENDING = 2;
    private final static int SORT_BY_UF_DESCENDING = 3;
    private final static int SORT_BY_FF_ASCENDING = 4;
    private final static int SORT_BY_FF_DESCENDING = 5;
    private final static int SORT_BY_US_ASCENDING = 6;
    private final static int SORT_BY_US_DESCENDING = 7;
    private final static int SORT_BY_ALPHABETICAL = 8;
    private final static int SORT_BY_LAST_INSTALLED = 9;
    private final static int SORT_BY_LAST_UPDATED = 10;

    private final ArrayList<String> selectedPackages = new ArrayList<>();
    private int appListViewOnClickMode = APPListViewOnClickMode_chooseAction;
    private int customThemeDisabledDot = R.drawable.shapedotblue;
    private int customThemeEnabledDot = R.drawable.shapedotblack;
    private BroadcastReceiver updateFrozenStatusBroadcastReceiver;
    private String currentFilter = "all";
    private int currentSortRule = SORT_BY_DEFAULT;
    private boolean needProcessOnItemCheckedStateChanged = true;

    private boolean shortcutsCompleted = true;
    private int shortcutsCount;

    private boolean isGridMode = false;

    private MainActivityAppListFragment mMainActivityAppListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            manageCrashLog();
        } catch (Exception e) {
            e.printStackTrace();
            checkIfNeedAskFirstTimeSetupAndShowDialog();
        }
//        throw new RuntimeException("自定义异常：仅于异常上报测试中使用");//发版前务必注释
    }


//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        go();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!shortcutsCompleted && shortcutsCount > 0) {
            String pkgName;
            shortcutsCount = shortcutsCount - 1;
            pkgName = selectedPackages.get(shortcutsCount);
            createShortCut(
                    getApplicationLabel(Main.this, null, null, pkgName),
                    pkgName,
                    getApplicationIcon(
                            Main.this,
                            pkgName,
                            getApplicationInfoFromPkgName(pkgName, this),
                            false),
                    Freeze.class,
                    "FreezeYou! " + pkgName,
                    Main.this
            );
            shortcutsCompleted = (shortcutsCount <= 0);
        }
        updateFrozenStatus();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("saveOnClickFunctionStatus", false)) {
            appListViewOnClickMode = sharedPref.getInt("onClickFunctionStatus", APPListViewOnClickMode_chooseAction);
        }
        if (sharedPref.getBoolean("saveSortMethodStatus", true)) {
            currentSortRule = sharedPref.getInt("sortMethodStatus", SORT_BY_DEFAULT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateFrozenStatusBroadcastReceiver != null)
            unregisterReceiver(updateFrozenStatusBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 80001:
                if (resultCode == RESULT_OK && data != null) {
                    String c_80001_title = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                    LauncherShortcutUtils.requestCreateShortCut(
                            c_80001_title,
                            ((Intent) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT))
                                    .setAction(Intent.ACTION_MAIN),
                            null,
                            "FreezeYou! FolderShortcut" + c_80001_title + new Date().getTime(),
                            this,
                            (Bitmap) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
                    );
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String cTheme = getUiTheme(this);
            if ("white".equals(cTheme) || "default".equals(cTheme)) {
                menu.findItem(R.id.menu_timedTasks).setIcon(R.drawable.ic_action_alarm_light);
                menu.findItem(R.id.menu_viewMode).setIcon(R.drawable.ic_action_filter_light);
                menu.findItem(R.id.menu_sortBy).setIcon(R.drawable.ic_action_sort_light);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        onPrepareMainOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        return onMainOptionsItemSelected(item);
    }

    private void generateList(String filter) {
        generateList(filter, currentSortRule);
    }

    /**
     * @param filter   筛选规则 或 所有需要显示的软件包包名（以“,”分割）
     * @param sortRule 排序规则
     */
    private void generateList(String filter, int sortRule) {
        currentFilter = filter;
        currentSortRule = sortRule;
        final FrameLayout appListFragmentContainer = findViewById(R.id.main_appList_fragmentContainer_frameLayout);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView mainCautionTextView = findViewById(R.id.main_caution_textView);
        final TextView main_loading_progress_textView = findViewById(R.id.main_loading_progress_textView);
        final FrameLayout linearLayout = findViewById(R.id.layout2);
        final ArrayList<Map<String, Object>> AppList = new ArrayList<>();
        final EditText search_editText = findViewById(R.id.search_editText);
        final ImageButton moreSettingsImageButton = findViewById(R.id.main_moreSettings_button);

        final Context applicationContext = getApplicationContext();

        moreSettingsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Main.this, v);
                popupMenu.inflate(R.menu.menu);
                onPrepareMainOptionsMenu(popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onMainOptionsItemSelected(item);
                    }
                });
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        RotateAnimation animation =
                                new RotateAnimation(45, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setDuration(300);
                        animation.setRepeatMode(RotateAnimation.REVERSE);
                        animation.setFillAfter(true);
                        moreSettingsImageButton.startAnimation(animation);
                    }
                });
                RotateAnimation animation =
                        new RotateAnimation(0, 45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(300);
                animation.setRepeatMode(RotateAnimation.REVERSE);
                animation.setFillAfter(true);
                moreSettingsImageButton.startAnimation(animation);
                popupMenu.show();
            }
        });

        if (isFinishing()) return;

        runOnUiThread(() -> {

            moreSettingsImageButton.setBackgroundResource(
                    Build.VERSION.SDK_INT >= 21 ?
                            R.drawable.oval_ripple : getThemeFabDotBackground(Main.this));

            linearLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            main_loading_progress_textView.setVisibility(View.VISIBLE);
            appListFragmentContainer.setVisibility(View.GONE);
            main_loading_progress_textView.setText(R.string.loadingPkgList);
            if (PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    .getBoolean("noCaution", false)) {
                mainCautionTextView.setVisibility(View.GONE);
            }
        });

        try {
            customThemeDisabledDot = getThemeDot(Main.this);
            customThemeEnabledDot = getThemeSecondDot(Main.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PackageInfo packageInfo1;
        PackageManager packageManager = applicationContext.getPackageManager();
        List<PackageInfo> packageInfo = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        int size = packageInfo == null ? 0 : packageInfo.size();
        boolean saveIconCache =
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        .getBoolean("cacheApplicationsIcons", false);
        switch (filter) {
            case "all":
                for (int i = 0; i < size; i++) {
                    packageInfo1 = packageInfo.get(i);
                    Map<String, Object> keyValuePair = processAppStatus(
                            getApplicationLabel(applicationContext, packageManager, packageInfo1.applicationInfo, packageInfo1.packageName),
                            packageInfo1.packageName,
                            packageInfo1,
                            packageManager,
                            saveIconCache
                    );
                    if (keyValuePair != null) {
                        AppList.add(keyValuePair);
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            case "OF":
                for (int i = 0; i < size; i++) {
                    packageInfo1 = packageInfo.get(i);
                    Map<String, Object> keyValuePair = processAppStatus(
                            getApplicationLabel(applicationContext, packageManager, packageInfo1.applicationInfo, packageInfo1.packageName),
                            packageInfo1.packageName,
                            packageInfo1,
                            packageManager,
                            saveIconCache
                    );
                    if ((keyValuePair != null) && (customThemeDisabledDot == (int) keyValuePair.get("isFrozen"))) {
                        AppList.add(keyValuePair);
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            case "UF":
                for (int i = 0; i < size; i++) {
                    packageInfo1 = packageInfo.get(i);
                    Map<String, Object> keyValuePair = processAppStatus(
                            getApplicationLabel(applicationContext, packageManager, packageInfo1.applicationInfo, packageInfo1.packageName),
                            packageInfo1.packageName,
                            packageInfo1,
                            packageManager,
                            saveIconCache
                    );
                    if (keyValuePair != null && customThemeEnabledDot == (int) keyValuePair.get("isFrozen")) {
                        AppList.add(keyValuePair);
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            case "OO":
                oneKeyListCheckAndGenerate(
                        new AppPreferences(applicationContext).getString(getString(R.string.sAutoFreezeApplicationList), ""),
                        AppList);
                checkAndAddNotAvailablePair(AppList);
                break;
            case "OOU":
                oneKeyListCheckAndGenerate(
                        new AppPreferences(applicationContext).getString(getString(R.string.sOneKeyUFApplicationList), ""),
                        AppList);
                checkAndAddNotAvailablePair(AppList);
                break;
            case "FOQ":
                oneKeyListCheckAndGenerate(
                        new AppPreferences(applicationContext).getString(getString(R.string.sFreezeOnceQuit), ""),
                        AppList);
                checkAndAddNotAvailablePair(AppList);
                break;
            case "OS":
                for (int i = 0; i < size; i++) {
                    packageInfo1 = packageInfo.get(i);
                    if ((packageInfo1.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                        Map<String, Object> keyValuePair = processAppStatus(
                                getApplicationLabel(applicationContext, packageManager, packageInfo1.applicationInfo, packageInfo1.packageName),
                                packageInfo1.packageName,
                                packageInfo1,
                                packageManager,
                                saveIconCache
                        );
                        if (keyValuePair != null) {
                            AppList.add(keyValuePair);
                        }
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            case "OU":
                for (int i = 0; i < size; i++) {
                    packageInfo1 = packageInfo.get(i);
                    if ((packageInfo1.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                        Map<String, Object> keyValuePair = processAppStatus(
                                getApplicationLabel(applicationContext, packageManager, packageInfo1.applicationInfo, packageInfo1.packageName),
                                packageInfo1.packageName,
                                packageInfo1,
                                packageManager,
                                saveIconCache
                        );
                        if (keyValuePair != null) {
                            AppList.add(keyValuePair);
                        }
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            case "UFU":
                for (int i = 0; i < size; i++) {
                    packageInfo1 = packageInfo.get(i);
                    if ((packageInfo1.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                        Map<String, Object> keyValuePair = processAppStatus(
                                getApplicationLabel(applicationContext, packageManager, packageInfo1.applicationInfo, packageInfo1.packageName),
                                packageInfo1.packageName,
                                packageInfo1,
                                packageManager,
                                saveIconCache
                        );
                        if (keyValuePair != null && customThemeEnabledDot == (int) keyValuePair.get("isFrozen")) {
                            AppList.add(keyValuePair);
                        }
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            default:
                oneKeyListCheckAndGenerate(filter, AppList);
                checkAndAddNotAvailablePair(AppList);
                break;
        }

        if (isFinishing()) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main_loading_progress_textView.setText(R.string.sorting);
            }
        });

        if (!AppList.isEmpty()) {
            switch (sortRule) {
                case SORT_BY_DEFAULT:
                    setSortByDefault(AppList);
                    break;
                case SORT_BY_UF_ASCENDING:
                    setSortByDefault(AppList);
                    final HashMap<String, Integer> ufTimesMap = getUFreezeTimesMap();
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            String s0 =
                                    Base64.encodeToString(
                                            ((String) m0.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);
                            String s1 =
                                    Base64.encodeToString(
                                            ((String) m1.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);

                            if (ufTimesMap.containsKey(s0) || ufTimesMap.containsKey(s1)) {
                                if (ufTimesMap.containsKey(s0) && ufTimesMap.containsKey(s1)) {
                                    return ufTimesMap.get(s0).compareTo(ufTimesMap.get(s1));
                                } else if (ufTimesMap.containsKey(s0) && ufTimesMap.get(s0) > 0) {
                                    return 1;
                                } else if (ufTimesMap.containsKey(s1) && ufTimesMap.get(s1) > 0) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                    });
                    break;
                case SORT_BY_UF_DESCENDING:
                    setSortByDefault(AppList);
                    final HashMap<String, Integer> uFreezeTimesMapTimesMap = getUFreezeTimesMap();
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            String s0 =
                                    Base64.encodeToString(
                                            ((String) m0.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);
                            String s1 =
                                    Base64.encodeToString(
                                            ((String) m1.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);

                            if (uFreezeTimesMapTimesMap.containsKey(s0) || uFreezeTimesMapTimesMap.containsKey(s1)) {
                                if (uFreezeTimesMapTimesMap.containsKey(s0) && uFreezeTimesMapTimesMap.containsKey(s1)) {
                                    return uFreezeTimesMapTimesMap.get(s1).compareTo(uFreezeTimesMapTimesMap.get(s0));
                                } else if (uFreezeTimesMapTimesMap.containsKey(s0) && uFreezeTimesMapTimesMap.get(s0) > 0) {
                                    return -1;
                                } else if (uFreezeTimesMapTimesMap.containsKey(s1) && uFreezeTimesMapTimesMap.get(s1) > 0) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                    });
                    break;
                case SORT_BY_FF_ASCENDING:
                    setSortByDefault(AppList);
                    final HashMap<String, Integer> freezeTimesMap = getFreezeTimesMap();
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            String s0 =
                                    Base64.encodeToString(
                                            ((String) m0.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);
                            String s1 =
                                    Base64.encodeToString(
                                            ((String) m1.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);

                            if (freezeTimesMap.containsKey(s0) || freezeTimesMap.containsKey(s1)) {
                                if (freezeTimesMap.containsKey(s0) && freezeTimesMap.containsKey(s1)) {
                                    return freezeTimesMap.get(s0).compareTo(freezeTimesMap.get(s1));
                                } else if (freezeTimesMap.containsKey(s0) && freezeTimesMap.get(s0) > 0) {
                                    return 1;
                                } else if (freezeTimesMap.containsKey(s1) && freezeTimesMap.get(s1) > 0) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                    });
                    break;
                case SORT_BY_FF_DESCENDING:
                    setSortByDefault(AppList);
                    final HashMap<String, Integer> freezeTimesMap1 = getFreezeTimesMap();
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            String s0 =
                                    Base64.encodeToString(
                                            ((String) m0.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);
                            String s1 =
                                    Base64.encodeToString(
                                            ((String) m1.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);

                            if (freezeTimesMap1.containsKey(s0) || freezeTimesMap1.containsKey(s1)) {
                                if (freezeTimesMap1.containsKey(s0) && freezeTimesMap1.containsKey(s1)) {
                                    return freezeTimesMap1.get(s1).compareTo(freezeTimesMap1.get(s0));
                                } else if (freezeTimesMap1.containsKey(s0) && freezeTimesMap1.get(s0) > 0) {
                                    return -1;
                                } else if (freezeTimesMap1.containsKey(s1) && freezeTimesMap1.get(s1) > 0) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                    });
                    break;
                case SORT_BY_US_ASCENDING:
                    setSortByDefault(AppList);
                    final HashMap<String, Integer> useTimesMap1 = getUseTimesMap();
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            String s0 =
                                    Base64.encodeToString(
                                            ((String) m0.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);
                            String s1 =
                                    Base64.encodeToString(
                                            ((String) m1.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);

                            if (useTimesMap1.containsKey(s0) || useTimesMap1.containsKey(s1)) {
                                if (useTimesMap1.containsKey(s0) && useTimesMap1.containsKey(s1)) {
                                    return useTimesMap1.get(s0).compareTo(useTimesMap1.get(s1));
                                } else if (useTimesMap1.containsKey(s0) && useTimesMap1.get(s0) > 0) {
                                    return 1;
                                } else if (useTimesMap1.containsKey(s1) && useTimesMap1.get(s1) > 0) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                    });
                    break;
                case SORT_BY_US_DESCENDING:
                    setSortByDefault(AppList);
                    final HashMap<String, Integer> useTimesMap = getUseTimesMap();
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            String s0 =
                                    Base64.encodeToString(
                                            ((String) m0.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);
                            String s1 =
                                    Base64.encodeToString(
                                            ((String) m1.get("PackageName")).getBytes(),
                                            Base64.DEFAULT);

                            if (useTimesMap.containsKey(s0) || useTimesMap.containsKey(s1)) {
                                if (useTimesMap.containsKey(s0) && useTimesMap.containsKey(s1)) {
                                    return useTimesMap.get(s1).compareTo(useTimesMap.get(s0));
                                } else if (useTimesMap.containsKey(s0) && useTimesMap.get(s0) > 0) {
                                    return -1;
                                } else if (useTimesMap.containsKey(s1) && useTimesMap.get(s1) > 0) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                    });
                    break;
                case SORT_BY_ALPHABETICAL:
                    setSortByDefault(AppList);
                    Collator collator = Collator.getInstance();
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            return collator.compare((String) m0.get("Name"), (String) m1.get("Name"));
                        }
                    });
                    break;
                case SORT_BY_LAST_INSTALLED:
                    setSortByDefault(AppList);
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            return -Long.compare((Long) m0.get("InstallTime"), (Long) m1.get("InstallTime"));
                        }
                    });
                    break;
                case SORT_BY_LAST_UPDATED:
                    setSortByDefault(AppList);
                    Collections.sort(AppList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> m0, Map<String, Object> m1) {
                            return -Long.compare((Long) m0.get("UpdateTime"), (Long) m1.get("UpdateTime"));
                        }
                    });
                    break;
                case SORT_BY_NO:
                default:
                    break;

            }
        }
        if (isFinishing()) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final MainAppListSimpleAdapter adapter =
                        mMainActivityAppListFragment.setAppListAdapter(
                                Main.this,
                                (ArrayList<Map<String, Object>>) AppList.clone(),
                                selectedPackages
                        );


                search_editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (TextUtils.isEmpty(charSequence)) {
                            adapter.replaceAllInFormerArrayList(AppList);
                        } else {
                            adapter.replaceAllInFormerArrayList(processListFilter(charSequence, AppList));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                main_loading_progress_textView.setText(R.string.finish);
                progressBar.setVisibility(View.GONE);
                mainCautionTextView.setVisibility(View.GONE);
                main_loading_progress_textView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
                appListFragmentContainer.setVisibility(View.VISIBLE);

                mMainActivityAppListFragment.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                    @Override
                    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                        final String pkgName =
                                (String) ((MainAppListSimpleAdapter) mMainActivityAppListFragment.getAppListAdapter())
                                        .getStoredArrayList().get(i).get("PackageName");

                        if (needProcessOnItemCheckedStateChanged) {
                            if (selectedPackages.contains(pkgName)) {
                                selectedPackages.remove(pkgName);
                            } else {
                                selectedPackages.add(pkgName);
                            }
                            needProcessOnItemCheckedStateChanged = false;
                            mMainActivityAppListFragment.setItemChecked(i, true);
                            actionMode.setTitle(Integer.toString(selectedPackages.size()));
                            adapter.notifyDataSetChanged();
                        } else {
                            needProcessOnItemCheckedStateChanged = true;
                        }
                    }

                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        Main.this.getMenuInflater().inflate(R.menu.multichoicemenu, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                        try {
                            SubMenu addToUserDefinedSubMenu = menu.findItem(R.id.list_menu_groupItem_addToUserDefined).getSubMenu();
                            SubMenu removeFromUserDefinedSubMenu = menu.findItem(R.id.list_menu_groupItem_removeFromUserDefined).getSubMenu();
                            addToUserDefinedSubMenu.clear(); // 清空先前产生的数据
                            removeFromUserDefinedSubMenu.clear();

                            addToUserDefinedSubMenu.add(
                                    R.id.list_menu_groupItem_addToUserDefined_menuGroup,
                                    R.id.list_menu_groupItem_addToUserDefined_newClassification,
                                    0,
                                    R.string.newClassification
                            ); // 加入“新建分类”
                            removeFromUserDefinedSubMenu.add(
                                    R.id.list_menu_groupItem_removeFromUserDefined_menuGroup,
                                    R.id.list_menu_groupItem_removeFromUserDefined_newClassification,
                                    0,
                                    R.string.newClassification
                            ); // 加入“新建分类”

                            // 添加用户定义的自定义分类
                            SQLiteDatabase userDefinedDb = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null);
                            userDefinedDb.execSQL(
                                    "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                            );
                            Cursor cursor = userDefinedDb.query("categories", new String[]{"label", "_id"}, null, null, null, null, null);
                            if (cursor.moveToFirst()) {
                                for (int i = 0; i < cursor.getCount(); i++) {
                                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                                    String title = cursor.getString(cursor.getColumnIndex("label"));
                                    addToUserDefinedSubMenu.add(R.id.list_menu_groupItem_addToUserDefined_menuGroup, id, id, new String(Base64.decode(title, Base64.DEFAULT)));
                                    removeFromUserDefinedSubMenu.add(R.id.list_menu_groupItem_removeFromUserDefined_menuGroup, id, id, new String(Base64.decode(title, Base64.DEFAULT)));
                                    cursor.moveToNext();
                                }
                            }
                            cursor.close();
                            userDefinedDb.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                        switch (menuItem.getGroupId()) {
                            case R.id.list_menu_groupItem_addToUserDefined_menuGroup:
                            case R.id.list_menu_groupItem_removeFromUserDefined_menuGroup:
                                switch (menuItem.getItemId()) {
                                    case R.id.list_menu_groupItem_addToUserDefined_newClassification:
                                    case R.id.list_menu_groupItem_removeFromUserDefined_newClassification:
                                        showAddNewUserDefinedClassificationDialog();
                                        return true;
                                    default:
                                        boolean isInRemoveMode =
                                                menuItem.getGroupId()
                                                        == R.id.list_menu_groupItem_removeFromUserDefined_menuGroup;
                                        String title = menuItem.getTitle().toString();
                                        List<String> existsPkgsList = new ArrayList<>();
                                        String existsPkgs = "";
                                        SQLiteDatabase userDefinedDb = openOrCreateDatabase("userDefinedCategories", Context.MODE_PRIVATE, null);
                                        userDefinedDb.execSQL(
                                                "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                                        );
                                        Cursor cursor =
                                                userDefinedDb.query(
                                                        "categories",
                                                        new String[]{"packages"},
                                                        "label = '" + Base64.encodeToString(title.getBytes(), Base64.DEFAULT) + "'",
                                                        null, null, null, null
                                                );

                                        if (cursor.moveToFirst()) {
                                            existsPkgs =
                                                    cursor.getString(
                                                            cursor.getColumnIndex("packages")
                                                    );
                                            existsPkgsList =
                                                    Arrays.asList(existsPkgs.split(","));
                                        }

                                        int size = selectedPackages.size();
                                        if (isInRemoveMode) {
                                            for (int i = 0; i < size; i++) {
                                                if (existsPkgsList.contains(selectedPackages.get(i))) {
                                                    existsPkgs =
                                                            existsPkgs.replace(
                                                                    selectedPackages.get(i) + ",",
                                                                    ""
                                                            );
                                                }
                                            }
                                        } else {
                                            StringBuilder existsPkgsBuilder = new StringBuilder(existsPkgs);
                                            for (int i = 0; i < size; i++) {
                                                if (!existsPkgsList.contains(selectedPackages.get(i))) {
                                                    existsPkgsBuilder.append(selectedPackages.get(i)).append(",");
                                                }
                                            }
                                            existsPkgs = existsPkgsBuilder.toString();
                                        }
                                        userDefinedDb.execSQL(
                                                "UPDATE categories SET packages = '"
                                                        + existsPkgs
                                                        + "' WHERE label = '"
                                                        + Base64.encodeToString(title.getBytes(), Base64.DEFAULT)
                                                        + "';"
                                        );
                                        cursor.close();
                                        userDefinedDb.close();
                                        showToast(Main.this, isInRemoveMode ? R.string.removed : R.string.added);
                                        return true;
                                }
                            default:
                                switch (menuItem.getItemId()) {
                                    case R.id.list_menu_selectAll:
                                        Adapter adpt = mMainActivityAppListFragment.getAppListAdapter();
                                        if (adpt instanceof MainAppListSimpleAdapter) {
                                            for (int i = 0; i < adpt.getCount(); i++) {
                                                String pkg = (String) ((MainAppListSimpleAdapter) adpt).getStoredArrayList().get(i).get("PackageName");
                                                if (!selectedPackages.contains(pkg)) {
                                                    selectedPackages.add(pkg);
                                                }
                                            }
                                            actionMode.setTitle(Integer.toString(selectedPackages.size()));
                                            ((MainAppListSimpleAdapter) adpt).notifyDataSetChanged();
                                        }
                                        return true;
                                    case R.id.list_menu_selectUnselected:
                                        Adapter adapt = mMainActivityAppListFragment.getAppListAdapter();
                                        if (adapt instanceof MainAppListSimpleAdapter) {
                                            for (int i = 0; i < adapt.getCount(); i++) {
                                                String pkg = (String) ((MainAppListSimpleAdapter) adapt).getStoredArrayList().get(i).get("PackageName");
                                                if (selectedPackages.contains(pkg)) {
                                                    selectedPackages.remove(pkg);
                                                } else {
                                                    selectedPackages.add(pkg);
                                                }
                                            }
                                            actionMode.setTitle(Integer.toString(selectedPackages.size()));
                                            ((MainAppListSimpleAdapter) adapt).notifyDataSetChanged();
                                        }
                                        return true;
                                    case R.id.list_menu_addToOneKeyFreezeList:
                                        processAddToOneKeyList(getString(R.string.sAutoFreezeApplicationList));
                                        return true;
                                    case R.id.list_menu_addToOneKeyUFList:
                                        processAddToOneKeyList(getString(R.string.sOneKeyUFApplicationList));
                                        return true;
                                    case R.id.list_menu_addToFreezeOnceQuit:
                                        processAddToOneKeyList(getString(R.string.sFreezeOnceQuit));
                                        return true;
                                    case R.id.list_menu_removeFromOneKeyFreezeList:
                                        processRemoveFromOneKeyList(getString(R.string.sAutoFreezeApplicationList));
                                        return true;
                                    case R.id.list_menu_removeFromOneKeyUFList:
                                        processRemoveFromOneKeyList(getString(R.string.sOneKeyUFApplicationList));
                                        return true;
                                    case R.id.list_menu_removeFromFreezeOnceQuit:
                                        processRemoveFromOneKeyList(getString(R.string.sFreezeOnceQuit));
                                        return true;
                                    case R.id.list_menu_freezeImmediately:
                                        processDisableAndEnableImmediately(true);
                                        actionMode.finish();
                                        return true;
                                    case R.id.list_menu_UFImmediately:
                                        processDisableAndEnableImmediately(false);
                                        actionMode.finish();
                                        return true;
                                    case R.id.list_menu_ForceStopImmediately:
                                        processForceStopImmediately();
                                        actionMode.finish();
                                        return true;
                                    case R.id.list_menu_createDisEnableShortCut:
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            ShortcutManager mShortcutManager =
                                                    Main.this.getSystemService(ShortcutManager.class);
                                            if (mShortcutManager != null && mShortcutManager.isRequestPinShortcutSupported()) {
                                                shortcutsCount = selectedPackages.size() - 1;
                                                if (shortcutsCount >= 0) {
                                                    String pkgName = selectedPackages.get(shortcutsCount);
                                                    createShortCut(
                                                            getApplicationLabel(Main.this, null, null, pkgName),
                                                            pkgName,
                                                            getApplicationIcon(
                                                                    Main.this,
                                                                    pkgName,
                                                                    getApplicationInfoFromPkgName(pkgName, Main.this),
                                                                    false),
                                                            Freeze.class,
                                                            "FreezeYou! " + pkgName,
                                                            Main.this
                                                    );
                                                }
                                                shortcutsCompleted = (shortcutsCount <= 0);
                                            } else {
                                                createFUFShortcutsBatch();
                                            }
                                        } else {
                                            createFUFShortcutsBatch();
                                        }
                                        return true;
                                    case R.id.list_menu_copyAfterBeingFormatted:
                                        StringBuilder formattedPackages = new StringBuilder();
                                        int size = selectedPackages.size();
                                        for (int i = 0; i < size; i++) {
                                            formattedPackages.append(selectedPackages.get(i)).append(",");
                                        }
                                        if (copyToClipboard(Main.this, formattedPackages.toString())) {
                                            showToast(Main.this, R.string.success);
                                        } else {
                                            showToast(Main.this, R.string.failed);
                                        }
                                        return true;
                                    default:
                                        return false;
                                }
                        }
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode actionMode) {
                        selectedPackages.clear();
                    }
                });

            }
        });

        mMainActivityAppListFragment.setOnAppListItemClickListener((adapterView, view, i, l) -> {
            Map<String, Object> map =
                    ((MainAppListSimpleAdapter) mMainActivityAppListFragment.getAppListAdapter())
                            .getStoredArrayList().get(i);
            final String name = (String) map.get("Name");
            final String pkgName = (String) map.get("PackageName");
            if (!getString(R.string.notAvailable).equals(name)) {
                switch (appListViewOnClickMode) {
                    case APPListViewOnClickMode_chooseAction:
                        showChooseActionPopupMenu(Main.this, Main.this, view, pkgName, name);
                        break;
                    case APPListViewOnClickMode_autoUFOrFreeze:
                        if (realGetFrozenStatus(Main.this, pkgName, null)) {
                            processUnfreezeAction(
                                    Main.this, pkgName, null, null,
                                    false, false, null, false);
                        } else {
                            processFreezeAction(
                                    Main.this, pkgName, null, null,
                                    false, null, false);
                        }
                        break;
                    case APPListViewOnClickMode_freezeImmediately:
                        if (!realGetFrozenStatus(Main.this, pkgName, null)) {
                            processFreezeAction(
                                    Main.this, pkgName, null, null,
                                    false, null, false);
                        } else {
                            if (!(new AppPreferences(Main.this)
                                    .getBoolean("lesserToast", false))) {
                                showToast(Main.this, R.string.freezeCompleted);
                            }
                        }
                        break;
                    case APPListViewOnClickMode_UFImmediately:
                        if (realGetFrozenStatus(Main.this, pkgName, null)) {
                            processUnfreezeAction(
                                    Main.this, pkgName, null, null,
                                    false, false, null, false);
                        } else {
                            if (!(new AppPreferences(Main.this)
                                    .getBoolean("lesserToast", false))) {
                                showToast(Main.this, R.string.UFCompleted);
                            }
                        }
                        break;
                    case APPListViewOnClickMode_UFAndRun:
                        if (realGetFrozenStatus(Main.this, pkgName, null)) {
                            processUnfreezeAction(
                                    Main.this, pkgName, null, null,
                                    true, false, null, false);
                        } else {
                            if (!(new AppPreferences(Main.this)
                                    .getBoolean("lesserToast", false))) {
                                showToast(Main.this, R.string.UFCompleted);
                            }
                            askRun(Main.this, pkgName, null,
                                    null, false, null, false);
                        }
                        break;
                    case APPListViewOnClickMode_autoUFOrFreezeAndRun:
                        if (realGetFrozenStatus(Main.this, pkgName, null)) {
                            processUnfreezeAction(
                                    Main.this, pkgName, null, null,
                                    true, false, null, false);
                        } else {
                            processFreezeAction(
                                    Main.this, pkgName, null, null,
                                    false, null, false);
                        }
                        break;
                    case APPListViewOnClickMode_addToOFList:
                        showToast(Main.this, addToOneKeyList(Main.this, getString(R.string.sAutoFreezeApplicationList), pkgName) ? R.string.added : R.string.failed);
                        break;
                    case APPListViewOnClickMode_removeFromOFList:
                        showToast(Main.this, removeFromOneKeyList(Main.this, getString(R.string.sAutoFreezeApplicationList), pkgName) ? R.string.removed : R.string.failed);
                        break;
                    case APPListViewOnClickMode_addToOUFList:
                        showToast(Main.this, addToOneKeyList(Main.this, getString(R.string.sOneKeyUFApplicationList), pkgName) ? R.string.added : R.string.failed);
                        break;
                    case APPListViewOnClickMode_removeFromOUFList:
                        showToast(Main.this, removeFromOneKeyList(Main.this, getString(R.string.sOneKeyUFApplicationList), pkgName) ? R.string.removed : R.string.failed);
                        break;
                    case APPListViewOnClickMode_addToFOQList:
                        showToast(Main.this, addToOneKeyList(Main.this, getString(R.string.sFreezeOnceQuit), pkgName) ? R.string.added : R.string.failed);
                        break;
                    case APPListViewOnClickMode_removeFromFOQList:
                        showToast(Main.this, removeFromOneKeyList(Main.this, getString(R.string.sFreezeOnceQuit), pkgName) ? R.string.removed : R.string.failed);
                        break;
                    case APPListViewOnClickMode_createFUFShortcut:
                        checkSettingsAndRequestCreateShortcut(
                                name,
                                pkgName,
                                getApplicationIcon(
                                        Main.this,
                                        pkgName,
                                        getApplicationInfoFromPkgName(pkgName, Main.this),
                                        false),
                                Freeze.class,
                                "FreezeYou! " + pkgName,
                                Main.this);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private static void addNotAvailablePair(Context context, List<Map<String, Object>> AppList) {
        Map<String, Object> keyValuePair = new HashMap<>();
        keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
        keyValuePair.put("Name", context.getString(R.string.notAvailable));
        keyValuePair.put("PackageName", context.getString(R.string.notAvailable));
        keyValuePair.put("InstallTime", 0L);
        keyValuePair.put("UpdateTime", 0L);
        AppList.add(keyValuePair);
    }

    private void manageCrashLog() throws Exception {
        File crashCheck = new File(getCacheDir() + File.separator + "log" + File.separator + "NeedUpload.log");
        if (crashCheck.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(crashCheck));
            String filePath = bufferedReader.readLine();
            bufferedReader.close();
            FileInputStream fileInputStream = new FileInputStream(filePath);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[fileInputStream.available()];
            fileInputStream.read(buffer);
            byteArrayOutputStream.write(buffer);
            fileInputStream.close();
            buildAlertDialog(Main.this, R.mipmap.ic_launcher_new_round, R.string.ifUploadCrashLog, R.string.notice)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri webPage = Uri.parse("https://freezeyou.playhi.net/crashReport.php?data=" + Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
                            Intent report = new Intent(Intent.ACTION_VIEW, webPage);
                            if (report.resolveActivity(getPackageManager()) != null) {
                                startActivity(report);
                            } else {
                                showToast(Main.this, R.string.failed);
                            }
                            checkIfNeedAskFirstTimeSetupAndShowDialog();
                        }
                    })
                    .setNeutralButton(R.string.update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkUpdate(Main.this);
                            checkIfNeedAskFirstTimeSetupAndShowDialog();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkIfNeedAskFirstTimeSetupAndShowDialog();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            checkIfNeedAskFirstTimeSetupAndShowDialog();
                        }
                    })
                    .create()
                    .show();
            //删除数据
            new File(filePath).delete();
            crashCheck.delete();
        } else {
            checkIfNeedAskFirstTimeSetupAndShowDialog();
        }
    }

    private void go() {
        if (updateFrozenStatusBroadcastReceiver == null) {
            updateFrozenStatusBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateFrozenStatus();
                }
            };
            IntentFilter filter = new IntentFilter("cf.playhi.freezeyou.action.packageStatusChanged");
            filter.addAction("cf.playhi.freezeyou.action.packageStatusChanged");
            this.registerReceiver(updateFrozenStatusBroadcastReceiver, filter);
        }

        TasksUtils.checkTimeTasks(this);
        TasksUtils.checkTriggerTasks(this);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Main.this);
        if (sharedPref.getBoolean("saveOnClickFunctionStatus", false)) {
            appListViewOnClickMode = sharedPref.getInt("onClickFunctionStatus", APPListViewOnClickMode_chooseAction);
        }
        if (sharedPref.getBoolean("saveSortMethodStatus", true)) {
            currentSortRule = sharedPref.getInt("sortMethodStatus", SORT_BY_DEFAULT);
        }
        if (!sharedPref.getBoolean("noCaution", false)) {
            buildAlertDialog(Main.this, R.mipmap.ic_launcher_new_round, R.string.cautionContent, R.string.caution)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int ii) {
                        }
                    })
                    .setNeutralButton(R.string.hToUse, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestOpenWebSite(Main.this, "https://www.zidon.net/");
                        }
                    })
                    .setNegativeButton(R.string.nCaution, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPref.edit().putBoolean("noCaution", true).apply();
                        }
                    })
                    .create().show();
        }

        String mainActivityPattern = sharedPref.getString("mainActivityPattern", "default");
        if ("default".equals(mainActivityPattern)) {
            isGridMode =
                    getWindowManager().getDefaultDisplay().getWidth()
                            > getWindowManager().getDefaultDisplay().getHeight() * 1.2;
        } else {
            isGridMode = "grid".equals(mainActivityPattern);
        }
        if (mMainActivityAppListFragment == null) {
            mMainActivityAppListFragment = new MainActivityAppListFragment();
            mMainActivityAppListFragment.setUseGridMode(isGridMode);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_appList_fragmentContainer_frameLayout, mMainActivityAppListFragment);
        fragmentTransaction.commit();

        Thread initThread;
        initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String mode = getIntent().getStringExtra("pkgName");//快捷方式提供
                if (mode == null) {
                    mode = PreferenceManager.getDefaultSharedPreferences(Main.this).getString("launchMode", "all");
                }
                if (mode == null) {
                    mode = "";
                }
                if (mode.startsWith("CATEGORY")) {
                    String categoryLabel = mode.substring("CATEGORY".length());
                    generateListForCategory(categoryLabel);
                    return;
                }
                switch (mode) {
                    case "OF":
                        generateList("OF");
                        break;
                    case "UF":
                        generateList("UF");
                        break;
                    case "OO":
                        generateList("OO");
                        break;
                    case "OOU":
                        generateList("OOU");
                        break;
                    case "OS":
                        generateList("OS");
                        break;
                    case "OU":
                        generateList("OU");
                        break;
                    case "FOQ":
                        generateList("FOQ");
                        break;
                    case "UFU":
                        generateList("UFU");
                        break;
                    default:
                        generateList("all");
                        break;
                }
            }
        });
        initThread.start();
        checkLongTimeNotUpdated();
    }

    /**
     * @param packageName 应用包名
     * @return 资源 Id
     */
    private int getFrozenStatus(String packageName, PackageManager packageManager) {
        return realGetFrozenStatus(Main.this, packageName, packageManager) ? customThemeDisabledDot : customThemeEnabledDot;
    }

    private void processFrozenStatus(Map<String, Object> keyValuePair, String packageName, PackageManager packageManager) {
        keyValuePair.put("isFrozen", getFrozenStatus(packageName, packageManager));
    }

    private Map<String, Object> processAppStatus(String name, String packageName, PackageInfo packageInfo, PackageManager packageManager, boolean saveIconCache) {
        if (!("android".equals(packageName) || "cf.playhi.freezeyou".equals(packageName))) {
            Map<String, Object> keyValuePair = new HashMap<>();
            keyValuePair.put(
                    "Img",
                    isGridMode && realGetFrozenStatus(this, packageName, packageManager)
                            ?
                            new BitmapDrawable(
                                    getGrayBitmap(
                                            getBitmapFromDrawable(
                                                    getApplicationIcon(
                                                            this, packageName,
                                                            packageInfo.applicationInfo,
                                                            false,
                                                            saveIconCache)
                                            )
                                    )
                            )
                            :
                            getApplicationIcon(
                                    Main.this,
                                    packageName,
                                    packageInfo.applicationInfo,
                                    false,
                                    saveIconCache
                            )

            );
            keyValuePair.put("Name", name);
            processFrozenStatus(keyValuePair, packageName, packageManager);
            keyValuePair.put("PackageName", packageName);
            keyValuePair.put("InstallTime", packageInfo.firstInstallTime);
            keyValuePair.put("UpdateTime", packageInfo.lastUpdateTime);
            return keyValuePair;
        }
        return null;
    }

    private void oneKeyListGenerate(String[] source, List<Map<String, Object>> AppList) {
        String name;
        Drawable icon;
        for (String aPkg : source) {
            name = getApplicationLabel(getApplicationContext(), null, null, aPkg);
            long installTime = 0L;
            long updateTime = 0L;
            try {
                PackageInfo pi = getPackageManager().getPackageInfo(aPkg, PackageManager.GET_UNINSTALLED_PACKAGES);
                installTime = pi.firstInstallTime;
                updateTime = pi.lastUpdateTime;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (!("android".equals(aPkg) || "cf.playhi.freezeyou".equals(aPkg) || "".equals(aPkg))) {
                Map<String, Object> keyValuePair = new HashMap<>();
                icon = isGridMode && realGetFrozenStatus(this, aPkg, null)
                        ?
                        new BitmapDrawable(
                                getGrayBitmap(
                                        getBitmapFromDrawable(getApplicationIcon(
                                                this, aPkg,
                                                getApplicationInfoFromPkgName(aPkg, this),
                                                false)
                                        )
                                )
                        )
                        :
                        getApplicationIcon(
                                Main.this,
                                aPkg,
                                getApplicationInfoFromPkgName(aPkg, Main.this),
                                true
                        );
                keyValuePair.put("Img", icon);
                keyValuePair.put("Name", name);
                processFrozenStatus(keyValuePair, aPkg, null);
                keyValuePair.put("PackageName", aPkg);
                keyValuePair.put("InstallTime", installTime);
                keyValuePair.put("UpdateTime", updateTime);
                AppList.add(keyValuePair);
            }
        }
    }

    private void oneKeyListCheckAndGenerate(String pkgNames, List<Map<String, Object>> AppList) {
        if (pkgNames != null) {
            oneKeyListGenerate(pkgNames.split(","), AppList);
        }
    }

    private void checkAndAddNotAvailablePair(List<Map<String, Object>> AppList) {
        if (AppList.size() == 0) {
            addNotAvailablePair(getApplicationContext(), AppList);
        }
    }

    private void processAddToOneKeyList(String string) {
        int size = selectedPackages.size();
        for (int i = 0; i < size; i++) {
            if (!addToOneKeyList(getApplicationContext(), string, selectedPackages.get(i))) {
                showToast(Main.this, selectedPackages.get(i) + getString(R.string.failed));
            }
        }
        showToast(Main.this, R.string.success);
    }

    private void processRemoveFromOneKeyList(String s) {
        int size = selectedPackages.size();
        for (int i = 0; i < size; i++) {
            if (!removeFromOneKeyList(getApplicationContext(), s, selectedPackages.get(i))) {
                showToast(Main.this, selectedPackages.get(i) + getString(R.string.failed));
            }
        }
        showToast(Main.this, R.string.success);
    }

    private void processDisableAndEnableImmediately(boolean freeze) {
        int size = selectedPackages.size();
        String[] pkgNameList = selectedPackages.toArray(new String[size]);
        ServiceUtils.startService(
                Main.this,
                new Intent(Main.this, FUFService.class)
                        .putExtra("single", false)
                        .putExtra("packages", pkgNameList)
                        .putExtra("freeze", freeze));
    }

    private void processForceStopImmediately() {
        int size = selectedPackages.size();
        String[] pkgNameList = selectedPackages.toArray(new String[size]);
        ServiceUtils.startService(
                Main.this,
                new Intent(Main.this, ForceStopService.class)
                        .putExtra("packages", pkgNameList)
        );
    }

    private void updateFrozenStatus() {

        if (mMainActivityAppListFragment == null) {
            return;
        }

        Adapter adapter = mMainActivityAppListFragment.getAppListAdapter();
        if (adapter instanceof MainAppListSimpleAdapter) {
            PackageManager pm = getPackageManager();
            int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                Map<String, Object> hm = ((MainAppListSimpleAdapter) adapter).getStoredArrayList().get(i);
                String pkgName = (String) hm.get("PackageName");
                ApplicationInfo applicationInfo = getApplicationInfoFromPkgName(pkgName, this);

                //检查是否已卸载
                if (applicationInfo == null) {
                    hm.put("Name", getString(R.string.uninstalled));
                    break;
                }

                //更新冻结状态信息
                if ((int) hm.get("isFrozen") != getFrozenStatus(pkgName, pm)) {

                    //更新冻结状态点
                    processFrozenStatus(hm, pkgName, pm);

                    //更新图标
                    if (isGridMode) {
                        hm.put("Img",
                                realGetFrozenStatus(this, pkgName, pm)
                                        ?
                                        new BitmapDrawable(
                                                getGrayBitmap(
                                                        getBitmapFromDrawable(
                                                                getApplicationIcon(
                                                                        this,
                                                                        pkgName,
                                                                        applicationInfo,
                                                                        false)
                                                        )
                                                )
                                        )
                                        :
                                        getApplicationIcon(
                                                Main.this,
                                                pkgName,
                                                applicationInfo,
                                                true
                                        )
                        );
                    }
                }
            }
            ((MainAppListSimpleAdapter) adapter).notifyDataSetChanged();
        }
    }

    private void saveOnClickFunctionStatus(int status) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.getBoolean("saveOnClickFunctionStatus", false)) {
            sharedPreferences.edit().putInt("onClickFunctionStatus", status).apply();
        }
    }

    private void saveSortMethodStatus(int status) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.getBoolean("saveSortMethodStatus", true)) {
            sharedPreferences.edit().putInt("sortMethodStatus", status).apply();
        }
    }

    private void checkLongTimeNotUpdated() {
        new Thread(() -> {
            try {
                final SharedPreferences sharedPreferences = getSharedPreferences("Ver", MODE_PRIVATE);
                if (sharedPreferences.getInt("Ver", 0) < getVersionCode(getApplicationContext())) {
                    sharedPreferences.edit()
                            .putInt("Ver", getVersionCode(getApplicationContext()))
                            .putLong("Time", new Date().getTime())
                            .apply();
                }
                if (isOutdated(sharedPreferences)) {

                    if (isFinishing()) return;

                    runOnUiThread(() -> buildAlertDialog(
                            Main.this,
                            R.mipmap.ic_launcher_new_round,
                            R.string.notUpdatedForALongTimeMessage,
                            R.string.notice)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) ->
                                    checkUpdate(Main.this))
                            .setNegativeButton(R.string.no, null)
                            .setNeutralButton(R.string.later, (dialog, which) -> {
                                sharedPreferences.edit()
                                        .putLong("Time", new Date().getTime())
                                        .apply();
                            })
                            .create().show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createFUFShortcutsBatch() { //小于 Android 8.0 适用
        int sps = selectedPackages.size();
        String pkgName;
        for (int i = 0; i < sps; i++) {
            pkgName = selectedPackages.get(i);
            createShortCut(
                    getApplicationLabel(Main.this, null, null, pkgName),
                    pkgName,
                    getApplicationIcon(
                            Main.this,
                            pkgName,
                            getApplicationInfoFromPkgName(pkgName, Main.this),
                            false),
                    Freeze.class,
                    "FreezeYou! " + pkgName,
                    Main.this
            );
        }
    }

    private void setSortByDefault(ArrayList<Map<String, Object>> AppList) {
        Collections.sort(AppList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                return ((String) stringObjectMap.get("PackageName")).compareTo((String) t1.get("PackageName"));
            }
        });
    }

    private void checkIfNeedAskFirstTimeSetupAndShowDialog() {

        if (getSharedPreferences("Ver", MODE_PRIVATE).getInt("Ver", 0) != 0) {
            go();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher_new_round);
        builder.setTitle(String.format(getString(R.string.welcomeToUseAppName), getString(R.string.app_name)));
        builder.setMessage(String.format(getString(R.string.welcomeToUseAppName), getString(R.string.app_name)));
        builder.setPositiveButton(R.string.quickSetup, (dialogInterface, i) -> {
            startActivity(
                    new Intent(getApplicationContext(), FirstTimeSetupActivity.class)
            );
            go();
        });
        builder.setNegativeButton(R.string.importConfig, (dialogInterface, i) -> {
            startActivity(
                    new Intent(getApplicationContext(), BackupMainActivity.class)
            );
            go();
        });
        builder.setNeutralButton(R.string.okay, (dialogInterface, i) -> go());
        builder.setOnCancelListener(dialogInterface -> go());
        builder.show();
    }

    private HashMap<String, Integer> getUFreezeTimesMap() {
        SQLiteDatabase db = openOrCreateDatabase("ApplicationsUFreezeTimes", Context.MODE_PRIVATE, null);
        HashMap<String, Integer> hashMap = getTimesMap(db);
        db.close();
        return hashMap;
    }

    private HashMap<String, Integer> getFreezeTimesMap() {
        SQLiteDatabase db = openOrCreateDatabase("ApplicationsFreezeTimes", Context.MODE_PRIVATE, null);
        HashMap<String, Integer> hashMap = getTimesMap(db);
        db.close();
        return hashMap;
    }

    private HashMap<String, Integer> getUseTimesMap() {
        SQLiteDatabase db = openOrCreateDatabase("ApplicationsUseTimes", Context.MODE_PRIVATE, null);
        HashMap<String, Integer> hashMap = getTimesMap(db);
        db.close();
        return hashMap;
    }

    private HashMap<String, Integer> getTimesMap(SQLiteDatabase db) {
        HashMap<String, Integer> hashMap = new HashMap<>();

        if (db == null) {
            return hashMap;
        }

        db.execSQL(
                "create table if not exists TimesList(_id integer primary key autoincrement,pkg varchar,times int)"
        );
        Cursor cursor = db.query("TimesList", new String[]{"pkg", "times"}, null, null, null, null, null);

        if (cursor == null) {
            return hashMap;
        }

        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                hashMap.put(
                        cursor.getString(cursor.getColumnIndex("pkg")),
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex("times")))
                );
                cursor.moveToNext();
            }
        }
        cursor.close();
        return hashMap;
    }

    private void addUserDefinedCategoriesTo(SubMenu subMenu, int groupId, int newClassificationId, int idBase) {
        subMenu.clear(); // 清空先前产生的数据

        subMenu.add(
                groupId,
                newClassificationId,
                0,
                R.string.newClassification
        ); // 加入“新建分类”

        // 添加用户定义的自定义分类
        SQLiteDatabase userDefinedDb = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null);
        userDefinedDb.execSQL(
                "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        );
        Cursor cursor = userDefinedDb.query("categories", new String[]{"label", "_id"}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("label"));
                subMenu.add(groupId, id + idBase, id, new String(Base64.decode(title, Base64.DEFAULT)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        userDefinedDb.close();
    }

    private void onPrepareMainOptionsMenu(Menu menu) {
        try {
            SubMenu vmUserDefinedSubMenu = menu.findItem(R.id.menu_vM_userDefined).getSubMenu();
            SubMenu createUserDefinedShortcutSubMenu = menu.findItem(R.id.menu_createUserDefinedShortcut).getSubMenu();
            SubMenu forceStopUserDefinedShortcutSubMenu = menu.findItem(R.id.menu_forceStopUserDefinedShortcut).getSubMenu();

            addUserDefinedCategoriesTo(vmUserDefinedSubMenu,
                    R.id.menu_vM_userDefined_menuGroup,
                    R.id.menu_vM_userDefined_newClassification,
                    0
            );

            addUserDefinedCategoriesTo(createUserDefinedShortcutSubMenu,
                    R.id.menu_createUserDefinedShortcut_menuGroup,
                    R.id.menu_createUserDefinedShortcut_newClassification,
                    0x2AAAAAAA
            );

            addUserDefinedCategoriesTo(forceStopUserDefinedShortcutSubMenu,
                    R.id.menu_forceStopUserDefinedShortcut_menuGroup,
                    R.id.menu_forceStopUserDefinedShortcut_newClassification,
                    0x55555555
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateListForCategory(String base64Label) {
        SQLiteDatabase userDefinedDb = openOrCreateDatabase("userDefinedCategories", Context.MODE_PRIVATE, null);
        userDefinedDb.execSQL(
                "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
        );
        Cursor cursor =
                userDefinedDb.query(
                        "categories",
                        new String[]{"packages"},
                        "label = '" + base64Label + "'",
                        null, null, null, null
                );
        if (cursor.moveToFirst()) {
            generateList(cursor.getString(cursor.getColumnIndex("packages")));
        } else {
            showToast(this, R.string.failed);
        }
        cursor.close();
        userDefinedDb.close();
    }

    private boolean onMainOptionsItemSelected(MenuItem item) {

        switch (item.getGroupId()) {
            case R.id.menu_vM_userDefined_menuGroup:
                switch (item.getItemId()) {
                    case R.id.menu_vM_userDefined_newClassification:
                        showAddNewUserDefinedClassificationDialog();
                        return true;
                    default:
                        String title = item.getTitle().toString();
                        generateListForCategory(Base64.encodeToString(title.getBytes(), Base64.DEFAULT));
                        return true;
                }
            case R.id.menu_createUserDefinedShortcut_menuGroup:
                switch (item.getItemId()) {
                    case R.id.menu_createUserDefinedShortcut_newClassification:
                        showAddNewUserDefinedClassificationDialog();
                        return true;
                    default:
                        String title = item.getTitle().toString();

                        checkSettingsAndRequestCreateShortcut(
                                title,
                                "CATEGORY" + Base64.encodeToString(title.getBytes(), Base64.DEFAULT),
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "Category " + Base64.encodeToString(title.getBytes(), Base64.DEFAULT),
                                this);

                        return true;
                }
            case R.id.menu_forceStopUserDefinedShortcut_menuGroup:
                switch (item.getItemId()) {
                    case R.id.menu_forceStopUserDefinedShortcut_newClassification:
                        showAddNewUserDefinedClassificationDialog();
                        return true;
                    default:
                        String title = item.getTitle().toString();

                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.forceStop) + " " + title,
                                "FORCESTOPCATEGORY" + Base64.encodeToString(title.getBytes(), Base64.DEFAULT),
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                ForceStop.class,
                                "ForceStopCategory " + Base64.encodeToString(title.getBytes(), Base64.DEFAULT),
                                this);

                        return true;
                }

            default:
                switch (item.getItemId()) {
                    case R.id.menu_createOneKeyFreezeShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.oneKeyFreeze),
                                "cf.playhi.freezeyou.extra.fuf",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                OneKeyFreeze.class,
                                "OneKeyFreeze",
                                this);
                        return true;
                    case R.id.menu_createOneKeyUFShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.oneKeyUF),
                                "cf.playhi.freezeyou.extra.fuf",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                OneKeyUF.class,
                                "OneKeyUF",
                                this);
                        return true;
                    case R.id.menu_createOneKeyLockScreenShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.oneKeyLockScreen),
                                "cf.playhi.freezeyou.extra.oklock",
                                getResources().getDrawable(R.drawable.screenlock),
                                OneKeyScreenLockImmediatelyActivity.class,
                                "OneKeyLockScreen",
                                this);
                        return true;
                    case R.id.menu_createOnlyFrozenShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.onlyFrozen),
                                "OF",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "OF",
                                this);
                        return true;
                    case R.id.menu_createOnlyUFShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.onlyUF),
                                "UF",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "UF",
                                this);
                        return true;
                    case R.id.menu_createOnlyOnekeyShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.onlyOnekey),
                                "OO",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "OO",
                                this);
                        return true;
                    case R.id.menu_createOnlyOnekeyUFShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.oneKeyUF),
                                "OOU",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "OOU",
                                this);
                        return true;
                    case R.id.menu_createFreezeOnceQuitShortCut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.freezeOnceQuit),
                                "FOQ",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "FOQ",
                                this);
                        return true;
                    case R.id.menu_createOnlySAShortcut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.onlySA),
                                "OS",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "OS",
                                this);
                        return true;
                    case R.id.menu_createOnlyUAShortcut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.onlyUA),
                                "OU",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "OU",
                                this);
                        return true;
                    case R.id.menu_createUnfrozenUAShortcut:
                        checkSettingsAndRequestCreateShortcut(
                                getString(R.string.unfrozenUA),
                                "UFU",
                                getResources().getDrawable(R.mipmap.ic_launcher_round),
                                Main.class,
                                "UFU",
                                this);
                        return true;
                    case R.id.menu_createNewFolderShortCut:
                        startActivityForResult(
                                new Intent(this, ShortcutLauncherFolderActivity.class)
                                        .setAction(Intent.ACTION_CREATE_SHORTCUT),
                                80001
                        );
                        return true;
                    case R.id.menu_timedTasks:
                        startActivity(new Intent(this, ScheduledTasksManageActivity.class));
                        return true;
                    case R.id.menu_about:
                        startActivity(new Intent(this, AboutActivity.class));
                        return true;
                    case R.id.menu_oneKeyFreezeImmediately:
                        startActivity(new Intent(this, OneKeyFreeze.class).putExtra("autoCheckAndLockScreen", false));
                        return true;
                    case R.id.menu_oneKeyUFImmediately:
                        startActivity(new Intent(this, OneKeyUF.class));
                        return true;
                    case R.id.menu_vM_onlyFrozen:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("OF");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_onlyUF:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("UF");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_all:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("all");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_onlyOnekey:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("OO");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_onlyOnekeyUF:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("OOU");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_onlySA:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("OS");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_onlyUA:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("OU");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_freezeOnceQuit:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("FOQ");
                            }
                        }).start();
                        return true;
                    case R.id.menu_vM_unfrozenUA:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList("UFU");
                            }
                        }).start();
                        return true;
                    case R.id.menu_update:
                        checkUpdate(Main.this);
                        return true;
                    case R.id.menu_moreSettings:
                        startActivity(new Intent(this, SettingsActivity.class));
                        return true;
                    case R.id.menu_faq:
                        requestOpenWebSite(this,
                                String.format("https://www.zidon.net/%1$s/faq/",
                                        getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                        return true;
                    case R.id.menu_onClickFunc_autoUFOrFreeze:
                        appListViewOnClickMode = APPListViewOnClickMode_autoUFOrFreeze;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_freezeImmediately:
                        appListViewOnClickMode = APPListViewOnClickMode_freezeImmediately;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_UFImmediately:
                        appListViewOnClickMode = APPListViewOnClickMode_UFImmediately;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_chooseAction:
                        appListViewOnClickMode = APPListViewOnClickMode_chooseAction;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_addToFOQList:
                        appListViewOnClickMode = APPListViewOnClickMode_addToFOQList;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_addToOFList:
                        appListViewOnClickMode = APPListViewOnClickMode_addToOFList;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_addToOUFList:
                        appListViewOnClickMode = APPListViewOnClickMode_addToOUFList;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_removeFromFOQList:
                        appListViewOnClickMode = APPListViewOnClickMode_removeFromFOQList;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_removeFromOFList:
                        appListViewOnClickMode = APPListViewOnClickMode_removeFromOFList;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_removeFromOUFList:
                        appListViewOnClickMode = APPListViewOnClickMode_removeFromOUFList;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_UFAndRun:
                        appListViewOnClickMode = APPListViewOnClickMode_UFAndRun;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_autoUFOrFreezeAndRun:
                        appListViewOnClickMode = APPListViewOnClickMode_autoUFOrFreezeAndRun;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_onClickFunc_createFUFShortcut:
                        appListViewOnClickMode = APPListViewOnClickMode_createFUFShortcut;
                        saveOnClickFunctionStatus(appListViewOnClickMode);
                        return true;
                    case R.id.menu_sB_default:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_DEFAULT);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_DEFAULT);
                        return true;
                    case R.id.menu_sB_no:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_NO);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_NO);
                        return true;
                    case R.id.menu_sB_uf_ascending:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_UF_ASCENDING);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_UF_ASCENDING);
                        return true;
                    case R.id.menu_sB_uf_descending:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_UF_DESCENDING);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_UF_DESCENDING);
                        return true;
                    case R.id.menu_sB_ff_ascending:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_FF_ASCENDING);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_FF_ASCENDING);
                        return true;
                    case R.id.menu_sB_ff_descending:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_FF_DESCENDING);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_FF_DESCENDING);
                        return true;
                    case R.id.menu_sB_us_ascending:
                        AccessibilityUtils.checkAndRequestIfAccessibilitySettingsOff(this);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_US_ASCENDING);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_US_ASCENDING);
                        return true;
                    case R.id.menu_sB_us_descending:
                        AccessibilityUtils.checkAndRequestIfAccessibilitySettingsOff(this);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_US_DESCENDING);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_US_DESCENDING);
                        return true;
                    case R.id.menu_sB_alphabetical:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_ALPHABETICAL);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_ALPHABETICAL);
                        return true;
                    case R.id.menu_sB_last_install:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_LAST_INSTALLED);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_LAST_INSTALLED);
                        return true;
                    case R.id.menu_sB_last_update:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generateList(currentFilter, SORT_BY_LAST_UPDATED);
                            }
                        }).start();
                        saveSortMethodStatus(SORT_BY_LAST_UPDATED);
                        return true;
                    default:
                        return super.onOptionsItemSelected(item);
                }
        }
    }

    private void showAddNewUserDefinedClassificationDialog() {
        final EditText vmUserDefinedNameAlertDialogEditText = new EditText(this);
        AlertDialog.Builder vmUserDefinedNameAlertDialog = new AlertDialog.Builder(this);
        vmUserDefinedNameAlertDialog.setTitle(R.string.label);
        vmUserDefinedNameAlertDialog.setView(vmUserDefinedNameAlertDialogEditText);
        vmUserDefinedNameAlertDialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String label = Base64.encodeToString(vmUserDefinedNameAlertDialogEditText.getText().toString().getBytes(), Base64.DEFAULT);
                if ("".equals(label)) {
                    showToast(Main.this, R.string.emptyNotAllowed);
                } else {
                    boolean alreadyExists = false;
                    SQLiteDatabase vmUserDefinedDb = openOrCreateDatabase("userDefinedCategories", MODE_PRIVATE, null);
                    vmUserDefinedDb.execSQL(
                            "create table if not exists categories(_id integer primary key autoincrement,label varchar,packages varchar)"
                    );
                    Cursor cursor = vmUserDefinedDb.query("categories", new String[]{"label"}, null, null, null, null, null);
                    if (cursor.moveToFirst()) {
                        for (int i = 0; i < cursor.getCount(); i++) {
                            if (label.equals(cursor.getString(cursor.getColumnIndex("label")))) {
                                alreadyExists = true;
                                break;
                            }
                            cursor.moveToNext();
                        }
                    }
                    cursor.close();
                    if (alreadyExists) {
                        showToast(Main.this, R.string.alreadyExist);
                    } else {
                        vmUserDefinedDb.execSQL(
                                "replace into categories(_id,label,packages) VALUES ( "
                                        + null + ",'"
                                        + label + "','')"
                        );
                    }
                    vmUserDefinedDb.close();
                }
            }
        });
        vmUserDefinedNameAlertDialog.setNegativeButton(R.string.cancel, null);
        vmUserDefinedNameAlertDialog.show();
    }

    @Override
    public void finish() {
        if (Build.VERSION.SDK_INT >= 21 && !(new AppPreferences(this).getBoolean("showInRecents", true))) {
            finishAndRemoveTask();
        }
        super.finish();
    }

}
