package cf.playhi.freezeyou;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.OneKeyListUtils;
import cf.playhi.freezeyou.utils.ToastUtils;

import static cf.playhi.freezeyou.ThemeUtils.getThemeDot;
import static cf.playhi.freezeyou.ThemeUtils.getThemeSecondDot;
import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus;
import static cf.playhi.freezeyou.utils.MoreUtils.processListFilter;

public class FUFLauncherShortcutCreator extends FreezeYouBaseActivity {

    private int customThemeDisabledDot = R.drawable.shapedotblue;
    private int customThemeEnabledDot = R.drawable.shapedotblack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getSupportActionBar());

        final Intent intent = getIntent();
        String slf_n = intent.getStringExtra("slf_n");
        final boolean returnPkgName = intent.getBooleanExtra("returnPkgName", false);
        final boolean isSlfMode = slf_n != null;

        try {
            customThemeDisabledDot = getThemeDot(this);
            customThemeEnabledDot = getThemeSecondDot(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isSlfMode || returnPkgName) {
            setContentView(R.layout.fuflsc_select_application);

            if (isSlfMode)
                setTitle(R.string.add);
            else
                setTitle(R.string.plsSelect);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    final ListView app_listView = findViewById(R.id.fuflsc_app_list);
                    final ProgressBar progressBar = findViewById(R.id.fuflsc_progressBar);
                    final LinearLayout linearLayout = findViewById(R.id.fuflsc_linearLayout);
                    final ArrayList<Map<String, Object>> AppList = new ArrayList<>();
                    final EditText search_editText = findViewById(R.id.fuflsc_search_editText);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.VISIBLE);
                            app_listView.setVisibility(View.GONE);
                        }
                    });
                    app_listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
                    final Context applicationContext = getApplicationContext();
                    PackageManager packageManager = applicationContext.getPackageManager();
                    List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
                    int size = applicationInfo == null ? 0 : applicationInfo.size();
                    for (int i = 0; i < size; i++) {
                        ApplicationInfo applicationInfo1 = applicationInfo.get(i);
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

                    if (!AppList.isEmpty()) {
                        Collections.sort(AppList, new Comparator<Map<String, Object>>() {

                            @Override
                            public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                                return ((String) stringObjectMap.get("PackageName")).compareTo((String) t1.get("PackageName"));
                            }
                        });
                    }

                    final ReplaceableSimpleAdapter adapter =
                            new ReplaceableSimpleAdapter(
                                    FUFLauncherShortcutCreator.this,
                                    (ArrayList<Map<String, Object>>) AppList.clone(),
                                    R.layout.app_list_1,
                                    new String[]{"Img", "Name", "PackageName", "isFrozen"},
                                    new int[]{R.id.img, R.id.name, R.id.pkgName, R.id.isFrozen});//isFrozen、isAutoList传图像资源id

                    adapter.setViewBinder(new ReplaceableSimpleAdapter.ViewBinder() {
                        public boolean setViewValue(View view, Object data, String textRepresentation) {
                            if (view instanceof ImageView && data instanceof Drawable) {
                                ((ImageView) view).setImageDrawable((Drawable) data);
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.GONE);
                            app_listView.setAdapter(adapter);
                            app_listView.setTextFilterEnabled(true);
                            app_listView.setVisibility(View.VISIBLE);
                        }
                    });

                    app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            HashMap<String, Object> map = (HashMap<String, Object>) app_listView.getItemAtPosition(i);
                            final String name = (String) map.get("Name");
                            final String pkgName = (String) map.get("PackageName");
                            if (isSlfMode) {
                                SharedPreferences sp = getSharedPreferences(getIntent().getStringExtra("slf_n"), MODE_PRIVATE);
                                if (!OneKeyListUtils.existsInOneKeyList(sp.getString("pkgS", ""), pkgName)) {
                                    sp.edit().putString("pkgS", sp.getString("pkgS", "") + pkgName + ",").apply();
                                    ToastUtils.showToast(FUFLauncherShortcutCreator.this, R.string.added);
                                } else {
                                    ToastUtils.showToast(FUFLauncherShortcutCreator.this, R.string.alreadyExist);
                                }
                                setResult(RESULT_OK);
                            } else {// if (returnPkgName)s
                                setResult(RESULT_OK, new Intent()
                                        .putExtra("pkgName", pkgName)
                                        .putExtra("name", name)
                                        .putExtra("id", "FreezeYou! " + pkgName));
                                finish();
                            }
                        }
                    });
                }
            }).start();

        } else {
            finish();
        }

    }

    private Map<String, Object> processAppStatus(String name, String packageName, ApplicationInfo applicationInfo, PackageManager packageManager) {
        if (!("android".equals(packageName) || "cf.playhi.freezeyou".equals(packageName))) {
            Map<String, Object> keyValuePair = new HashMap<>();
            keyValuePair.put("Img", getApplicationIcon(FUFLauncherShortcutCreator.this, packageName, applicationInfo, true));
            keyValuePair.put("Name", name);
            processFrozenStatus(keyValuePair, packageName, packageManager);
            keyValuePair.put("PackageName", packageName);
            return keyValuePair;
        }
        return null;
    }

//    /**
//     * @param packageName 应用包名
//     * @return true 则已冻结
//     */
//    private boolean realGetFrozenStatus(String packageName, PackageManager pm) {
//        return (checkRootFrozen(FUFLauncherShortcutCreator.this, packageName, pm) || checkMRootFrozen(FUFLauncherShortcutCreator.this, packageName));
//    }

    private void processFrozenStatus(Map<String, Object> keyValuePair, String packageName, PackageManager packageManager) {
        keyValuePair.put("isFrozen", getFrozenStatus(packageName, packageManager));
    }

    /**
     * @param packageName 应用包名
     * @return 资源 Id
     */
    private int getFrozenStatus(String packageName, PackageManager packageManager) {
        return realGetFrozenStatus(FUFLauncherShortcutCreator.this, packageName, packageManager)
                ? customThemeDisabledDot : customThemeEnabledDot;
    }

}
