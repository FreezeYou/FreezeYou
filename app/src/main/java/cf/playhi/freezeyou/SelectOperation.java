package cf.playhi.freezeyou;

import android.app.Activity;
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
import static cf.playhi.freezeyou.MoreUtils.copyToClipboard;
import static cf.playhi.freezeyou.Support.createShortCut;
import static cf.playhi.freezeyou.Support.existsInOneKeyList;
import static cf.playhi.freezeyou.Support.getApplicationIcon;
import static cf.playhi.freezeyou.Support.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.Support.isAccessibilitySettingsOn;
import static cf.playhi.freezeyou.Support.openAccessibilitySettings;
import static cf.playhi.freezeyou.Support.removeFromOneKeyList;
import static cf.playhi.freezeyou.ToastUtils.showToast;

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

        final AppPreferences sharedPreferences = new AppPreferences(getApplicationContext());

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
                                name,
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
                        copyToClipboard(SelectOperation.this, pkgName);
                        finish();
                        break;
                    case 3:
                        checkAddOrRemove(pkgNames, pkgName, getString(R.string.sAutoFreezeApplicationList));
                        finish();
                        break;
                    case 4:
                        checkAddOrRemove(UFPkgNames, pkgName, getString(R.string.sOneKeyUFApplicationList));
                        finish();
                        break;
                    case 5:
                        checkAddOrRemove(FreezeOnceQuitPkgNames, pkgName, getString(R.string.sFreezeOnceQuit));
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

    private void checkAddOrRemove(String pkgNames, String pkgName, String oneKeyName) {
        if (existsInOneKeyList(pkgNames, pkgName)) {
            showToast(getApplicationContext(),
                    removeFromOneKeyList(getApplicationContext(),
                            oneKeyName,
                            pkgName) ? R.string.removed : R.string.removeFailed);
        } else {
            showToast(getApplicationContext(),
                    addToOneKeyList(getApplicationContext(),
                            oneKeyName,
                            pkgName) ? R.string.added : R.string.addFailed);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (getString(R.string.sFreezeOnceQuit).equals(oneKeyName)) {
                if (!preferences.getBoolean("freezeOnceQuit", false)) {
                    preferences.edit().putBoolean("freezeOnceQuit", true).apply();
                    new AppPreferences(getApplicationContext()).put("freezeOnceQuit", true);
                }
                if (!isAccessibilitySettingsOn(getApplicationContext())) {
                    showToast(SelectOperation.this, R.string.needActiveAccessibilityService);
                    openAccessibilitySettings(SelectOperation.this);
                }
            }
        }
    }
}
