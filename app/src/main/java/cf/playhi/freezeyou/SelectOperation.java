package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.Support.addToOneKeyList;
import static cf.playhi.freezeyou.Support.createShortCut;
import static cf.playhi.freezeyou.Support.existsInOneKeyList;
import static cf.playhi.freezeyou.Support.getApplicationIcon;
import static cf.playhi.freezeyou.Support.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.Support.isAccessibilitySettingsOn;
import static cf.playhi.freezeyou.Support.openAccessibilitySettings;
import static cf.playhi.freezeyou.Support.removeFromOneKeyList;
import static cf.playhi.freezeyou.Support.showToast;

public class SelectOperation extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectoperation);
        LinearLayout so_body = findViewById(R.id.so_body);
        TextView so_name = findViewById(R.id.so_name);
        TextView so_pkgName = findViewById(R.id.so_pkgName);
        ListView listView = findViewById(R.id.operationsListView);
        final String name = getIntent().getStringExtra("Name");
        final String pkgName = getIntent().getStringExtra("pkgName");

        so_name.setText(name);
        so_pkgName.setText(pkgName);

        String[] operationData = new String[]{
                getResources().getString(R.string.createDisEnableShortCut),
                getResources().getString(R.string.disableAEnable),
                getResources().getString(R.string.copyPkgName),
                getResources().getString(R.string.addToOneKeyList),
                getResources().getString(R.string.addToOneKeyUFList),
                getResources().getString(R.string.addToFreezeOnceQuit),
                getResources().getString(R.string.appDetail)
        };

        final AppPreferences sharedPreferences =  new AppPreferences(getApplicationContext());

        final String pkgNames = sharedPreferences.getString(getString(R.string.sAutoFreezeApplicationList), "");
        if (existsInOneKeyList(pkgNames, pkgName)) {
            operationData[3] = getResources().getString(R.string.removeFromOneKeyList);
        }

        final String UFPkgNames = sharedPreferences.getString(getString(R.string.sOneKeyUFApplicationList), "");
        if (existsInOneKeyList(UFPkgNames, pkgName)) {
            operationData[4] = getResources().getString(R.string.removeFromOneKeyUFList);
        }

        final String FreezeOnceQuitPkgNames = sharedPreferences.getString(getString(R.string.sFreezeOnceQuit), "");
        if (existsInOneKeyList(FreezeOnceQuitPkgNames, pkgName)) {
            operationData[5] = getResources().getString(R.string.removeFromFreezeOnceQuit);
        }

        final ListAdapter adapt = new ArrayAdapter<>(SelectOperation.this, R.layout.so_item, operationData);
        listView.setAdapter(adapt);

        if (Build.VERSION.SDK_INT < 21) {
            so_body.setBackgroundColor(Color.parseColor("#90000000"));
        }

        so_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        createShortCut(
                                name.replace("(" + getString(R.string.frozen) + ")", "").replace("(" + getString(R.string.oneKeyFreeze) + ")", ""),
                                pkgName,
                                getApplicationIcon(SelectOperation.this, pkgName, getApplicationInfoFromPkgName(pkgName, getApplicationContext()), false),
                                Freeze.class,
                                "FreezeYou! " + pkgName,
                                SelectOperation.this
                        );
                        finish();
                        break;
                    case 1:
                        if (!(getString(R.string.notAvailable).equals(name))) {
                            startActivity(new Intent(SelectOperation.this, Freeze.class).putExtra("pkgName", pkgName).putExtra("auto", false));
                        }
                        finish();
                        break;
                    case 2:
                        ClipboardManager copy = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(pkgName, pkgName);
                        if (copy != null) {
                            copy.setPrimaryClip(clip);
                            showToast(SelectOperation.this, R.string.success);
                        } else {
                            showToast(SelectOperation.this, R.string.failed);
                        }
                        finish();
                        break;
                    case 3:
                        if (existsInOneKeyList(pkgNames, pkgName)) {
                            showToast(getApplicationContext(),
                                    removeFromOneKeyList(getApplicationContext(),
                                            getString(R.string.sAutoFreezeApplicationList),
                                            pkgName) ? R.string.removed : R.string.removeFailed);
                        } else {
                            showToast(getApplicationContext(),
                                    addToOneKeyList(getApplicationContext(),
                                            getString(R.string.sAutoFreezeApplicationList),
                                            pkgName) ? R.string.added : R.string.addFailed);
                        }
                        finish();
                        break;
                    case 4:
                        if (existsInOneKeyList(UFPkgNames, pkgName)) {
                            showToast(getApplicationContext(),
                                    removeFromOneKeyList(getApplicationContext(),
                                            getString(R.string.sOneKeyUFApplicationList),
                                            pkgName) ? R.string.removed : R.string.removeFailed);
                        } else {
                            showToast(getApplicationContext(),
                                    addToOneKeyList(getApplicationContext(),
                                            getString(R.string.sOneKeyUFApplicationList),
                                            pkgName) ? R.string.added : R.string.addFailed);
                        }
                        finish();
                        break;
                    case 5:
                        if (existsInOneKeyList(FreezeOnceQuitPkgNames, pkgName)) {
                            showToast(getApplicationContext(),
                                    removeFromOneKeyList(getApplicationContext(),
                                            getString(R.string.sFreezeOnceQuit),
                                            pkgName) ? R.string.removed : R.string.removeFailed);
                        } else {
                            showToast(getApplicationContext(),
                                    addToOneKeyList(getApplicationContext(),
                                            getString(R.string.sFreezeOnceQuit),
                                            pkgName) ? R.string.added : R.string.addFailed);
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            if (!preferences.getBoolean("freezeOnceQuit", false)) {
                                preferences.edit().putBoolean("freezeOnceQuit", true).apply();
                                if (!isAccessibilitySettingsOn(getApplicationContext())) {
                                    showToast(SelectOperation.this, R.string.needActiveAccessibilityService);
                                    openAccessibilitySettings(getApplicationContext());
                                }
                            }
                        }
                        finish();
                        break;
                    case 6:
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", pkgName, null);
                        intent.setData(uri);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showToast(getApplicationContext(), e.getLocalizedMessage());
                        }
                        finish();
                        break;
                    default:
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.pullup, R.anim.pulldown);
    }
}
