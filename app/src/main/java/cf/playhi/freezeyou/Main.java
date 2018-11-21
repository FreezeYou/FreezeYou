package cf.playhi.freezeyou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.AlertDialogUtils.buildAlertDialog;
import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.LauncherShortcutUtils.createShortCut;
import static cf.playhi.freezeyou.MoreUtils.copyToClipboard;
import static cf.playhi.freezeyou.MoreUtils.requestOpenWebSite;
import static cf.playhi.freezeyou.OneKeyListUtils.addToOneKeyList;
import static cf.playhi.freezeyou.OneKeyListUtils.removeFromOneKeyList;
import static cf.playhi.freezeyou.Support.realGetFrozenStatus;
import static cf.playhi.freezeyou.ThemeUtils.getThemeDot;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.ToastUtils.showToast;
import static cf.playhi.freezeyou.VersionUtils.checkUpdate;
import static cf.playhi.freezeyou.VersionUtils.getVersionCode;

public class Main extends Activity {

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

    private final ArrayList<String> selectedPackages = new ArrayList<>();
    private int appListViewOnClickMode = APPListViewOnClickMode_chooseAction;
    private int customThemeDisabledDot = R.drawable.shapedotblue;
    private View itemView;
//    private String filterNowStatus;
    private BroadcastReceiver updateFrozenStatusBroadcastReceiver;

    private boolean shortcutsCompleted = true;
    private int shortcutsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            manageCrashLog();
        } catch (Exception e) {
            e.printStackTrace();
            go();
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
        if (!shortcutsCompleted) {
            String pkgName;
            shortcutsCount = shortcutsCount - 1;
            pkgName = selectedPackages.get(shortcutsCount);
            createShortCut(
                    getApplicationLabel(Main.this, null, null, pkgName),
                    pkgName,
                    getApplicationIcon(Main.this, pkgName, null, false),
                    Freeze.class,
                    "FreezeYou! " + pkgName,
                    Main.this
            );
            shortcutsCompleted = (shortcutsCount <= 0);
        }
        updateFrozenStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateFrozenStatusBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        if (customThemeDisabledDot == R.drawable.shapedotblack)
            menu.findItem(R.id.menu_timedTasks).setIcon(R.drawable.ic_action_add_light);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_createOneKeyFreezeShortCut:
                createShortCut(
                        getString(R.string.oneKeyFreeze),
                        "",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),
                        OneKeyFreeze.class,
                        "OneKeyFreeze",
                        this
                );
                return true;
            case R.id.menu_createOneKeyUFShortCut:
                createShortCut(
                        getString(R.string.oneKeyUF),
                        "",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),
                        OneKeyUF.class,
                        "OneKeyUF",
                        this
                );
                return true;
            case R.id.menu_createOneKeyLockScreenShortCut:
                createShortCut(
                        getString(R.string.oneKeyLockScreen),
                        "",
                        getResources().getDrawable(R.drawable.screenlock),
                        OneKeyScreenLockImmediatelyActivity.class,
                        "OneKeyLockScreen",
                        this
                );
                return true;
            case R.id.menu_createOnlyFrozenShortCut:
                createShortCut(
                        getString(R.string.onlyFrozen),
                        "OF",
                        getResources().getDrawable(R.mipmap.ic_launcher_round), Main.class,
                        "OF",
                        this
                );
                return true;
            case R.id.menu_createOnlyUFShortCut:
                createShortCut(
                        getString(R.string.onlyUF),
                        "UF",
                        getResources().getDrawable(R.mipmap.ic_launcher_round), Main.class,
                        "UF",
                        this
                );
                return true;
            case R.id.menu_createOnlyOnekeyShortCut:
                createShortCut(
                        getString(R.string.onlyOnekey),
                        "OO",
                        getResources().getDrawable(R.mipmap.ic_launcher_round), Main.class,
                        "OO",
                        this
                );
                return true;
            case R.id.menu_createOnlyOnekeyUFShortCut:
                createShortCut(
                        getString(R.string.oneKeyUF),
                        "OOU",
                        getResources().getDrawable(R.mipmap.ic_launcher_round), Main.class,
                        "OOU",
                        this
                );
                return true;
            case R.id.menu_createFreezeOnceQuitShortCut:
                createShortCut(
                        getString(R.string.freezeOnceQuit),
                        "FOQ",
                        getResources().getDrawable(R.mipmap.ic_launcher_round), Main.class,
                        "FOQ",
                        this
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
            case R.id.menu_update:
                checkUpdate(Main.this);
                return true;
            case R.id.menu_moreSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_faq:
                requestOpenWebSite(this, "https://freezeyou.playhi.net/faq.html");
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void generateList(String filter) {
//        filterNowStatus = filter;
        final ListView app_listView = findViewById(R.id.app_list);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView textView = findViewById(R.id.textView);
        final LinearLayout linearLayout = findViewById(R.id.layout2);
        final List<Map<String, Object>> AppList = new ArrayList<>();
        final EditText search_editText = findViewById(R.id.search_editText);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                app_listView.setVisibility(View.GONE);
            }
        });
        try {
            customThemeDisabledDot = getThemeDot(Main.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ApplicationInfo applicationInfo1;
        Context applicationContext = getApplicationContext();
        PackageManager packageManager = applicationContext.getPackageManager();
        List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        int size = applicationInfo == null ? 0 : applicationInfo.size();
        switch (filter) {
            case "all":
                for (int i = 0; i < size; i++) {
                    applicationInfo1 = applicationInfo.get(i);
                    Map<String, Object> keyValuePair = processAppStatus(
                            getApplicationLabel(applicationContext, packageManager, applicationInfo1, applicationInfo1.packageName),
                            applicationInfo1.packageName,
                            applicationInfo1,
                            packageManager
                    );
                    if (keyValuePair != null) {
                        AppList.add(keyValuePair);
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            case "OF":
                for (int i = 0; i < size; i++) {
                    applicationInfo1 = applicationInfo.get(i);
                    Map<String, Object> keyValuePair = processAppStatus(
                            getApplicationLabel(applicationContext, packageManager, applicationInfo1, applicationInfo1.packageName),
                            applicationInfo1.packageName,
                            applicationInfo1,
                            packageManager
                    );
                    if ((keyValuePair != null) && (R.drawable.shapedotwhite != (int) keyValuePair.get("isFrozen"))) {
                        AppList.add(keyValuePair);
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            case "UF":
                for (int i = 0; i < size; i++) {
                    applicationInfo1 = applicationInfo.get(i);
                    Map<String, Object> keyValuePair = processAppStatus(
                            getApplicationLabel(applicationContext, packageManager, applicationInfo1, applicationInfo1.packageName),
                            applicationInfo1.packageName,
                            applicationInfo1,
                            packageManager
                    );
                    if (keyValuePair != null && R.drawable.shapedotwhite == (int) keyValuePair.get("isFrozen")) {
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
                    applicationInfo1 = applicationInfo.get(i);
                    if ((applicationInfo1.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                        Map<String, Object> keyValuePair = processAppStatus(
                                getApplicationLabel(applicationContext, packageManager, applicationInfo1, applicationInfo1.packageName),
                                applicationInfo1.packageName,
                                applicationInfo1,
                                packageManager
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
                    applicationInfo1 = applicationInfo.get(i);
                    if ((applicationInfo1.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                        Map<String, Object> keyValuePair = processAppStatus(
                                getApplicationLabel(applicationContext, packageManager, applicationInfo1, applicationInfo1.packageName),
                                applicationInfo1.packageName,
                                applicationInfo1,
                                packageManager
                        );
                        if (keyValuePair != null) {
                            AppList.add(keyValuePair);
                        }
                    }
                }
                checkAndAddNotAvailablePair(AppList);
                break;
            default:
                break;
        }

        if (!AppList.isEmpty()) {
            Collections.sort(AppList, new Comparator<Map<String, Object>>() {

                @Override
                public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                    return ((String) stringObjectMap.get("PackageName")).compareTo((String) t1.get("PackageName"));
                }
            });
        }

        final SimpleAdapter adapter = new SimpleAdapter(Main.this, AppList,
                R.layout.app_list_1, new String[]{"Img", "Name",
                "PackageName", "isFrozen"}, new int[]{R.id.img, R.id.name, R.id.pkgName, R.id.isFrozen});//isFrozen、isAutoList传图像资源id

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageDrawable((Drawable) data);
                    return true;
                } else
                    return false;
            }
        });

        search_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {//setFilterText报错不断，simpleAdapter这类似问题网上似乎隐隐约约也有人提出过……自己写一个蹩脚的筛选吧
                if (TextUtils.isEmpty(charSequence)) {
                    app_listView.setAdapter(adapter);
                } else {
                    SimpleAdapter processedAdapter = new SimpleAdapter(Main.this, processListFilter(charSequence, AppList),
                            R.layout.app_list_1, new String[]{"Img", "Name",
                            "PackageName", "isFrozen"}, new int[]{R.id.img, R.id.name, R.id.pkgName, R.id.isFrozen});

                    processedAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                        public boolean setViewValue(View view, Object data,
                                                    String textRepresentation) {
                            if (view instanceof ImageView && data instanceof Drawable) {
                                ImageView imageView = (ImageView) view;
                                imageView.setImageDrawable((Drawable) data);
                                return true;
                            } else
                                return false;
                        }
                    });
                    app_listView.setAdapter(processedAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
                app_listView.setTextFilterEnabled(true);
                app_listView.setAdapter(adapter);
                app_listView.setTextFilterEnabled(true);
                app_listView.setVisibility(View.VISIBLE);
            }
        });

        app_listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                final String pkgName = ((HashMap<String, String>) app_listView.getItemAtPosition(i)).get("PackageName");
                if (b) {
                    selectedPackages.add(pkgName);
                    actionMode.setTitle(Integer.toString(selectedPackages.size()));
                } else {
                    selectedPackages.remove(pkgName);
                    actionMode.setTitle(Integer.toString(selectedPackages.size()));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                Main.this.getMenuInflater().inflate(R.menu.multichoicemenu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
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
                    case R.id.list_menu_createDisEnableShortCut:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ShortcutManager mShortcutManager =
                                    Main.this.getSystemService(ShortcutManager.class);
                            if (mShortcutManager != null && mShortcutManager.isRequestPinShortcutSupported()) {
                                String pkgName;
                                shortcutsCount = selectedPackages.size() - 1;
                                pkgName = selectedPackages.get(shortcutsCount);
                                createShortCut(
                                        getApplicationLabel(Main.this, null, null, pkgName),
                                        pkgName,
                                        getApplicationIcon(Main.this, pkgName, null, false),
                                        Freeze.class,
                                        "FreezeYou! " + pkgName,
                                        Main.this
                                );
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
                        copyToClipboard(Main.this, formattedPackages.toString());
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                selectedPackages.clear();
            }
        });

        app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, Object> map = (HashMap<String, Object>) app_listView.getItemAtPosition(i);
                final String name = (String) map.get("Name");
                final String pkgName = (String) map.get("PackageName");
                itemView = view;
                if (!getString(R.string.notAvailable).equals(name)) {
                    switch (appListViewOnClickMode) {
                        case APPListViewOnClickMode_chooseAction:
                            startActivityForResult(
                                    new Intent(Main.this, SelectOperation.class).
                                            putExtra("Name", name).
                                            putExtra("pkgName", pkgName),
                                    1092
                            );
                            overridePendingTransition(R.anim.pullup, R.anim.pulldown);
                            break;
                        case APPListViewOnClickMode_autoUFOrFreeze:
                            if (realGetFrozenStatus(Main.this, pkgName, null)) {
                                Support.processUnfreezeAction(Main.this, pkgName, false, false, null, false);
                            } else {
                                Support.processFreezeAction(Main.this, pkgName, false, null, false);
                            }
                            updateFrozenStatus();
                            break;
                        case APPListViewOnClickMode_freezeImmediately:
                            if (!realGetFrozenStatus(Main.this, pkgName, null)) {
                                Support.processFreezeAction(Main.this, pkgName, false, null, false);
                            } else {
                                if (!(new AppPreferences(Main.this).getBoolean("lesserToast", false))) {
                                    showToast(Main.this, R.string.freezeCompleted);
                                }
                            }
                            updateFrozenStatus();
                            break;
                        case APPListViewOnClickMode_UFImmediately:
                            if (realGetFrozenStatus(Main.this, pkgName, null)) {
                                Support.processUnfreezeAction(Main.this, pkgName, false, false, null, false);
                            } else {
                                if (!(new AppPreferences(Main.this).getBoolean("lesserToast", false))) {
                                    showToast(Main.this, R.string.UFCompleted);
                                }
                            }
                            updateFrozenStatus();
                            break;
                        case APPListViewOnClickMode_UFAndRun:
                            if (realGetFrozenStatus(Main.this, pkgName, null)) {
                                Support.processUnfreezeAction(Main.this, pkgName, true, false, null, false);
                            } else {
                                if (!(new AppPreferences(Main.this).getBoolean("lesserToast", false))) {
                                    showToast(Main.this, R.string.UFCompleted);
                                }
                                Support.askRun(Main.this, pkgName, false, null, false);
                            }
                            updateFrozenStatus();
                            break;
                        case APPListViewOnClickMode_autoUFOrFreezeAndRun:
                            if (realGetFrozenStatus(Main.this, pkgName, null)) {
                                Support.processUnfreezeAction(Main.this, pkgName, true, false, null, false);
                            } else {
                                Support.processFreezeAction(Main.this, pkgName, false, null, false);
                            }
                            updateFrozenStatus();
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
                        default:
                            break;
                    }
                }
            }
        });
    }

    private static void addNotAvailablePair(Context context, List<Map<String, Object>> AppList) {
        Map<String, Object> keyValuePair = new HashMap<>();
        keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
        keyValuePair.put("Name", context.getString(R.string.notAvailable));
        keyValuePair.put("PackageName", context.getString(R.string.notAvailable));
        AppList.add(keyValuePair);
    }

    private void manageCrashLog() throws Exception {
        File crashCheck = new File(Environment.getDataDirectory().getPath()
                + File.separator
                + "data"
                + File.separator
                + "cf.playhi.freezeyou"
                + File.separator
                + "log"
                + File.separator
                + "NeedUpload.log");
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
                            go();
                        }
                    })
                    .setNeutralButton(R.string.update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkUpdate(Main.this);
                            go();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            go();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            go();
                        }
                    })
                    .create()
                    .show();
            //删除数据
            new File(filePath).delete();
            crashCheck.delete();
        } else {
            go();
        }
    }

    private List<Map<String, Object>> processListFilter(CharSequence prefix, List<Map<String, Object>> unfilteredValues) {

        String prefixString = prefix.toString().toLowerCase();

        if (unfilteredValues != null) {
            int count = unfilteredValues.size();

            List<Map<String, Object>> newValues = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                try {
                    Map<String, Object> h = unfilteredValues.get(i);
                    String name = ((String) h.get("Name"));
                    if (name != null && name.toLowerCase().contains(prefixString)) {
                        newValues.add(h);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return newValues;
        }

        return new ArrayList<>();
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
                    default:
                        generateList("all");
                        break;
                }
            }
        });
        initThread.start();
        checkLongTimeNotUpdated();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Main.this);
        if (sharedPref.getBoolean("saveOnClickFunctionStatus", false)) {
            appListViewOnClickMode = sharedPref.getInt("onClickFunctionStatus", 0);
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
                            requestOpenWebSite(Main.this, "https://freezeyou.playhi.net/");
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
    }

    /**
     * @param packageName 应用包名
     * @return 资源 Id
     */
    private int getFrozenStatus(String packageName, PackageManager packageManager) {
        return realGetFrozenStatus(Main.this, packageName, packageManager) ? customThemeDisabledDot : R.drawable.shapedotwhite;
    }

    private void processFrozenStatus(Map<String, Object> keyValuePair, String packageName, PackageManager packageManager) {
        keyValuePair.put("isFrozen", getFrozenStatus(packageName, packageManager));
    }

    private Map<String, Object> processAppStatus(String name, String packageName, ApplicationInfo applicationInfo, PackageManager packageManager) {
        if (!("android".equals(packageName) || "cf.playhi.freezeyou".equals(packageName))) {
            Map<String, Object> keyValuePair = new HashMap<>();
            keyValuePair.put("Img", getApplicationIcon(Main.this, packageName, applicationInfo, true));
            keyValuePair.put("Name", name);
            processFrozenStatus(keyValuePair, packageName, packageManager);
            keyValuePair.put("PackageName", packageName);
            return keyValuePair;
        }
        return null;
    }

    private void oneKeyListGenerate(String[] source, List<Map<String, Object>> AppList) {
        String name;
        Drawable icon;
        for (String aPkgNameList : source) {
            name = getApplicationLabel(getApplicationContext(), null, null, aPkgNameList);
            if (!("android".equals(aPkgNameList) || "cf.playhi.freezeyou".equals(aPkgNameList) || "".equals(aPkgNameList))) {
                Map<String, Object> keyValuePair = new HashMap<>();
                icon = getApplicationIcon(Main.this, aPkgNameList, null, true);
                keyValuePair.put("Img", icon);
                keyValuePair.put("Name", name);
                processFrozenStatus(keyValuePair, aPkgNameList, null);
                keyValuePair.put("PackageName", aPkgNameList);
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
//        if (Build.VERSION.SDK_INT >= 21 && Support.isDeviceOwner(Main.this)) {
        ServiceUtils.startService(
                Main.this,
                new Intent(Main.this, FUFService.class)
                        .putExtra("single", false)
                        .putExtra("packages", pkgNameList)
                        .putExtra("freeze", freeze));
//            Support.oneKeyActionMRoot(Main.this, freeze, pkgNameList);
//        } else {
//            Support.oneKeyActionRoot(Main.this, freeze, pkgNameList);
//        }
//        generateList(filterNowStatus);
    }

    private void updateFrozenStatus() {
        if (itemView != null) {
            try {
                ImageView imageView = itemView.findViewById(R.id.isFrozen);
                TextView textView = itemView.findViewById(R.id.pkgName);
                imageView.setImageResource(getFrozenStatus(textView.getText().toString(), null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveOnClickFunctionStatus(int status) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.getBoolean("saveOnClickFunctionStatus", false)) {
            sharedPreferences.edit().putInt("onClickFunctionStatus", status).apply();
        }
    }

    private void checkLongTimeNotUpdated() {
        new Thread(new Runnable() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void run() {
                try {
                    final SharedPreferences sharedPreferences = getSharedPreferences("Ver", MODE_PRIVATE);
                    if (sharedPreferences.getInt("Ver", 0) < getVersionCode(getApplicationContext())) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("Ver", getVersionCode(getApplicationContext()));
                        editor.putLong("Time", new Date().getTime());
                        editor.commit();
                    }
                    if ((new Date().getTime() - sharedPreferences.getLong("Time", 0)) > 1728000000) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildAlertDialog(
                                        Main.this,
                                        R.mipmap.ic_launcher_new_round,
                                        R.string.notUpdatedForALongTimeMessage,
                                        R.string.notice)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                checkUpdate(Main.this);
                                            }
                                        })
                                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .setNeutralButton(R.string.later, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putLong("Time", new Date().getTime());
                                                editor.commit();
                                            }
                                        })
                                        .create().show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    getApplicationIcon(Main.this, pkgName, null, false),
                    Freeze.class,
                    "FreezeYou! " + pkgName,
                    Main.this
            );
        }
    }

    @Override
    public void finish() {
        if (Build.VERSION.SDK_INT >= 21 && !(new AppPreferences(this).getBoolean("showInRecents", true))) {
            finishAndRemoveTask();
        }
        super.finish();
    }
}
