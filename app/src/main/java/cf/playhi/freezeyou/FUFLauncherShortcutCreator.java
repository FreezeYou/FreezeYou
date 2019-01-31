package cf.playhi.freezeyou;

import android.app.Activity;
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
import android.view.MenuItem;
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

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.Support.realGetFrozenStatus;
import static cf.playhi.freezeyou.ThemeUtils.getThemeDot;
import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class FUFLauncherShortcutCreator extends Activity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getActionBar());

        final Intent intent = getIntent();
        String slf_n = intent.getStringExtra("slf_n");
        boolean returnPkgName = intent.getBooleanExtra("returnPkgName", false);
        boolean isSlfMode = slf_n != null;

        if (isSlfMode || returnPkgName) {
            setContentView(R.layout.fuflsc_select_application);

            if (isSlfMode)
                setTitle(R.string.add);
            else
                setTitle(R.string.plsSelect);

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

            final MainAppListSimpleAdapter adapter =
                    new MainAppListSimpleAdapter(
                            FUFLauncherShortcutCreator.this,
                            (ArrayList<Map<String, Object>>) AppList.clone(),
                            R.layout.app_list_1,
                            new String[]{"Img", "Name", "PackageName", "isFrozen"},
                            new int[]{R.id.img, R.id.name, R.id.pkgName, R.id.isFrozen});//isFrozen、isAutoList传图像资源id

            adapter.setViewBinder(new MainAppListSimpleAdapter.ViewBinder() {
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
                    Intent it = getIntent();
                    if (it.getStringExtra("slf_n") != null) {
                        SharedPreferences sp = getSharedPreferences(getIntent().getStringExtra("slf_n"), MODE_PRIVATE);
                        sp.edit().putString("pkgS", sp.getString("pkgS", "") + pkgName + ",").apply();
                        setResult(RESULT_OK);
                    } else if (it.getBooleanExtra("returnPkgName", false)) {
                        setResult(RESULT_OK, new Intent().putExtra("pkgName", pkgName));
//                    } else {
//
//                        <!--桌面快捷方式（类小部件）入口已迁移至 LauncherShortcutConfirmAndGenerateActivity.java -->
//
//                        Intent shortcutIntent = new Intent(FUFLauncherShortcutCreator.this, Freeze.class);
//                        shortcutIntent.putExtra("pkgName", pkgName);
//                        Intent intent = new Intent();
//                        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
//                        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
//                        try {
//                            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(getPackageManager().getApplicationIcon(pkgName)));
//                        } catch (Exception e) {
//                            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(getApplicationIcon(applicationContext, pkgName, null, false)));
//                        }
//                        setResult(RESULT_OK, intent);
                    }
                    finish();
                }
            });

        } else {
            finish();
        }

    }

    private ArrayList<Map<String, Object>> processListFilter(CharSequence prefix, ArrayList<Map<String, Object>> unfilteredValues) {

        String prefixString = prefix.toString().toLowerCase();

        if (unfilteredValues != null) {
            int count = unfilteredValues.size();

            ArrayList<Map<String, Object>> newValues = new ArrayList<>(count);
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
        return realGetFrozenStatus(FUFLauncherShortcutCreator.this, packageName, packageManager) ? getThemeDot(FUFLauncherShortcutCreator.this) : R.drawable.shapedotwhite;
    }

}
