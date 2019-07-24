package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.view.Window.FEATURE_NO_TITLE;
import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.ApplicationIconUtils.getGrayBitmap;
import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class ShortcutLauncherFolderActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction()));
        super.onCreate(savedInstanceState);

        final String uuid = getIntent().getStringExtra("UUID");
        if (uuid == null) {
            return;
        }
        final SharedPreferences uuidSp = getSharedPreferences(uuid, MODE_PRIVATE);
        uuidSp.registerOnSharedPreferenceChangeListener(this);

        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            doCreateShortCut();
        } else {
            doShowFolder();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6 && resultCode == RESULT_OK && data != null && dialog != null) {
            Bitmap bm = data.getParcelableExtra("Icon");
            Window w = dialog.getWindow();
            if (w != null && bm != null) {
                ImageButton imageButton = w.findViewById(R.id.slfa_confirm_icon_name_dialog_imageButton);
                if (imageButton != null) {
                    imageButton.setImageDrawable(new BitmapDrawable(bm));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        final String uuid = getIntent().getStringExtra("UUID");
        if (uuid == null) {
            return;
        }
        final SharedPreferences uuidSp = getSharedPreferences(uuid, MODE_PRIVATE);
        uuidSp.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private void doCreateShortCut() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        View confirmIconNameDialogView = View.inflate(this, R.layout.slfa_confirm_icon_name_dialog, null);
        final EditText editText = confirmIconNameDialogView.findViewById(R.id.slfa_confirm_icon_name_dialog_editText);
        final ImageButton imageButton = confirmIconNameDialogView.findViewById(R.id.slfa_confirm_icon_name_dialog_imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(ShortcutLauncherFolderActivity.this, SelectShortcutIconActivity.class),
                        6);
            }
        });
        ab.setView(confirmIconNameDialogView);
        ab.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        ab.setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString();
                Intent intent = new Intent();
                intent.putExtra(
                        Intent.EXTRA_SHORTCUT_INTENT,
                        new Intent(ShortcutLauncherFolderActivity.this, ShortcutLauncherFolderActivity.class)
                                .putExtra("UUID", name + new Date().getTime()));
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(imageButton.getDrawable()));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        ab.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        AlertDialog alertDialog = ab.show();
        dialog = alertDialog;
        Window w = alertDialog.getWindow();
        if (w != null) {
            View v = (View) w.findViewById(android.R.id.custom).getParent();
            if (v != null) {
                v.setMinimumHeight(0);
            }
        }
    }

    private void doShowFolder() {
        requestWindowFeature(FEATURE_NO_TITLE);
        setContentView(R.layout.shortcut_launcher_folder);
        final String uuid = getIntent().getStringExtra("UUID");
        if (uuid == null) {
            ToastUtils.showToast(this, R.string.failed);
            finish();
            return;
        }
        final GridView slf_apps_gridView = findViewById(R.id.slf_apps_gridView);
        final TextView slf_folderName_textView = findViewById(R.id.slf_folderName_textView);

        slf_apps_gridView.setColumnWidth((int) (getResources().getDimension(android.R.dimen.app_icon_size) * 1.8));
//            if (Build.VERSION.SDK_INT>=16)
//                slf_apps_gridView.setVerticalSpacing(slf_apps_gridView.getRequestedColumnWidth()/slf_apps_gridView.getNumColumns());

        final ArrayList<Map<String, Object>> folderItems = new ArrayList<>();

//            final List<ApplicationInfo> applicationInfo = getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
//            int size = applicationInfo == null ? 0 : applicationInfo.size();
//            String pkgName;
        final SharedPreferences uuidSp = getSharedPreferences(uuid, MODE_PRIVATE);
        slf_folderName_textView.setText(uuidSp.getString("folderName", getString(R.string.folder)));
        generateFolderItems(folderItems, uuidSp);
        ReplaceableSimpleAdapter replaceableSimpleAdapter =
                new ReplaceableSimpleAdapter(
                        this, (ArrayList<Map<String, Object>>) folderItems.clone(),
                        R.layout.shortcut_launcher_folder_item, new String[]{"Icon", "Label"},
                        new int[]{R.id.slfi_imageView, R.id.slfi_textView});

        replaceableSimpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
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

        slf_apps_gridView.setAdapter(replaceableSimpleAdapter);

        slf_apps_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String pkg =
                        (String) ((ReplaceableSimpleAdapter) slf_apps_gridView.getAdapter())
                                .getStoredArrayList().get(position).get("Package");
                if ("freezeyou@add".equals(pkg)) {
                    startActivityForResult(
                            new Intent(
                                    ShortcutLauncherFolderActivity.this,
                                    FUFLauncherShortcutCreator.class)
                                    .putExtra("slf_n", uuid),
                            7001);
                } else {
                    Support.checkFrozenStatusAndStartApp(
                            ShortcutLauncherFolderActivity.this,
                            pkg,
                            null,
                            null);
                }
            }
        });

        slf_apps_gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> hm =
                        ((ReplaceableSimpleAdapter) slf_apps_gridView.getAdapter())
                                .getStoredArrayList().get(position);
                String pkgName = (String) hm.get("Package");
                if (!"freezeyou@add".equals(pkgName)) {
                    String name = (String) hm.get("Label");
                    Support.showChooseActionPopupMenu(
                            ShortcutLauncherFolderActivity.this, view, pkgName,
                            name, true, uuidSp);
                }
                return true;
            }
        });

        slf_folderName_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ShortcutLauncherFolderActivity.this);
                final EditText input = new EditText(ShortcutLauncherFolderActivity.this);
                final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setTitle(R.string.name);
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences(uuid, MODE_PRIVATE);
                        String s = input.getText().toString();
                        sharedPreferences.edit().putString("folderName", s).apply();
                        slf_folderName_textView.setText(s);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                AlertDialog alertDialog = builder.show();
                Window w = alertDialog.getWindow();
                if (w != null) {
                    View v1 = (View) w.findViewById(android.R.id.custom).getParent();
                    if (v1 != null) {
                        v1.setMinimumHeight(0);
                    }
                }
            }
        });

    }

    private void generateFolderItems(ArrayList<Map<String, Object>> folderItems, SharedPreferences UUIDSharedPreferences) {
        String s = UUIDSharedPreferences.getString("pkgS", "");
        String[] pkgS = s == null ? new String[]{} : s.split(",");
        for (String aPkg : pkgS) {
            if (!"".equals(aPkg)) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("Icon",
                        Support.realGetFrozenStatus(this, aPkg, null)
                                ?
                                new BitmapDrawable(getGrayBitmap(getBitmapFromDrawable(getApplicationIcon(this, aPkg, ApplicationInfoUtils.getApplicationInfoFromPkgName(aPkg, this), false))))
                                :
                                getApplicationIcon(this, aPkg, ApplicationInfoUtils.getApplicationInfoFromPkgName(aPkg, this), false)
                );
                map.put("Label", getApplicationLabel(this, null, null, aPkg));
                map.put("Package", aPkg);

                folderItems.add(map);
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("Icon", getResources().getDrawable(R.drawable.grid_add));
        map.put("Label", getString(R.string.add));
        map.put("Package", "freezeyou@add");

        folderItems.add(map);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if ("pkgS".equals(key)) {
            final GridView slf_apps_gridView = findViewById(R.id.slf_apps_gridView);
            if (slf_apps_gridView != null) {
                Adapter adapter = slf_apps_gridView.getAdapter();
                if (adapter instanceof ReplaceableSimpleAdapter) {
                    final ArrayList<Map<String, Object>> folderItems = new ArrayList<>();
                    generateFolderItems(folderItems, sharedPreferences);
                    ((ReplaceableSimpleAdapter) adapter).replaceAllInFormerArrayList(folderItems);
                }
            }
        } else if ("folderName".equals(key)) {
            final TextView slf_folderName_textView = findViewById(R.id.slf_folderName_textView);
            if (slf_folderName_textView != null) {
                slf_folderName_textView.setText(sharedPreferences.getString("folderName", getString(R.string.folder)));
            }
        }
    }
}
