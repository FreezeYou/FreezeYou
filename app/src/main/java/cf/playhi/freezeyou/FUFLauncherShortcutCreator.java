package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.Support.checkMRootFrozen;
import static cf.playhi.freezeyou.Support.checkRootFrozen;
import static cf.playhi.freezeyou.Support.getApplicationIcon;
import static cf.playhi.freezeyou.Support.getApplicationLabel;
import static cf.playhi.freezeyou.Support.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ThemeUtils.getThemeDot;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class FUFLauncherShortcutCreator extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            setContentView(R.layout.main);

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
            final Context applicationContext = getApplicationContext();
            PackageManager packageManager = applicationContext.getPackageManager();
            List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            int size = applicationInfo.size();
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

            final SimpleAdapter adapter = new SimpleAdapter(FUFLauncherShortcutCreator.this, AppList,
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
                        SimpleAdapter processedAdapter = new SimpleAdapter(FUFLauncherShortcutCreator.this, processListFilter(charSequence, AppList),
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
                }

                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    ;
                }
            });


            app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    HashMap<String, Object> map = (HashMap<String, Object>) app_listView.getItemAtPosition(i);
                    final String name = (String) map.get("Name");
                    final String pkgName = (String) map.get("PackageName");
                    Intent shortcutIntent  = new Intent(FUFLauncherShortcutCreator.this, Freeze.class);
                    shortcutIntent.putExtra("pkgName", pkgName);
                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,getBitmapFromDrawable( getApplicationIcon(applicationContext,pkgName,null,false)));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

        } else {
            finish();
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

    /**
     * @param packageName 应用包名
     * @return true 则已冻结
     */
    private boolean realGetFrozenStatus(String packageName, PackageManager pm) {
        return (checkRootFrozen(FUFLauncherShortcutCreator.this, packageName, pm) || checkMRootFrozen(FUFLauncherShortcutCreator.this, packageName));
    }

    private void processFrozenStatus(Map<String, Object> keyValuePair, String packageName, PackageManager packageManager) {
        keyValuePair.put("isFrozen", getFrozenStatus(packageName, packageManager));
    }

    /**
     * @param packageName 应用包名
     * @return 资源 Id
     */
    private int getFrozenStatus(String packageName, PackageManager packageManager) {
        return realGetFrozenStatus(packageName, packageManager) ? getThemeDot(FUFLauncherShortcutCreator.this) : R.drawable.shapedotwhite;
    }

}
