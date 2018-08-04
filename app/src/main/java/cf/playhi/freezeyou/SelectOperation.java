package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import static cf.playhi.freezeyou.Support.addToOneKeyList;
import static cf.playhi.freezeyou.Support.createShortCut;
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

        String[] operationData = new String[] {
                getResources().getString(R.string.createDisEnableShortCut),
                getResources().getString(R.string.disableAEnable),
                getResources().getString(R.string.copyPkgName),
                getResources().getString(R.string.addToOneKeyList),
                getResources().getString(R.string.addToOneKeyUFList),
                getResources().getString(R.string.addToFreezeOnceQuit),
                getResources().getString(R.string.appDetail)
        };

        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE);
        final String pkgNameList = sharedPreferences.getString("pkgName", "");
        if (pkgNameList.contains("|" + pkgName + "|")) {
            operationData[3] = getResources().getString(R.string.removeFromOneKeyList);
        }

        final SharedPreferences UFSharedPreferences = getApplicationContext().getSharedPreferences(
                "OneKeyUFApplicationList", Context.MODE_PRIVATE);
        final String UFPkgNameList = UFSharedPreferences.getString("pkgName", "");
        if (UFPkgNameList.contains("|" + pkgName + "|")) {
            operationData[4] = getResources().getString(R.string.removeFromOneKeyUFList);
        }

        final SharedPreferences FreezeOnceQuitSharedPreferences = getApplicationContext().getSharedPreferences(
                "FreezeOnceQuit", Context.MODE_PRIVATE);
        final String FreezeOnceQuitPkgNameList = FreezeOnceQuitSharedPreferences.getString("pkgName", "");
        if (UFPkgNameList.contains("|" + pkgName + "|")) {
            operationData[4] = getResources().getString(R.string.removeFromFreezeOnceQuit);
        }

        final ListAdapter adapt = new ArrayAdapter<>(SelectOperation.this, R.layout.so_item, operationData);
        listView.setAdapter(adapt);

        if (Build.VERSION.SDK_INT<21){
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
                                name.replace("(" + getString(R.string.frozen) + ")", "").replace("(" + getString(R.string.oneKeyFreeze) + ")",""),
                                pkgName,
                                getApplicationIcon(SelectOperation.this,pkgName,getApplicationInfoFromPkgName(pkgName,getApplicationContext()),false),
                                Freeze.class,
                                "FreezeYou! "+pkgName,
                                SelectOperation.this
                        );
                        finish();
                        break;
                    case 1:
                        if (!(getString(R.string.notAvailable).equals(name))){
                            startActivity(new Intent(SelectOperation.this,Freeze.class).putExtra("pkgName",pkgName).putExtra("auto",false));
                        }
                        finish();
                        break;
                    case 2:
                        ClipboardManager copy = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(pkgName, pkgName);
                        if (copy != null){
                            copy.setPrimaryClip(clip);
                            showToast(SelectOperation.this,R.string.success);
                        } else {
                            showToast(SelectOperation.this,R.string.failed);
                        }
                        finish();
                        break;
                    case 3:
                        if (pkgNameList.contains("|" + pkgName + "|")) {
                            showToast(getApplicationContext(),
                                    removeFromOneKeyList(getApplicationContext(),
                                            "AutoFreezeApplicationList",
                                            pkgName)? R.string.removed : R.string.removeFailed);
//                            sharedPreferences.edit()
//                                    .putString(
//                                            "pkgName",
//                                            pkgNameList.replace("|" + pkgName + "|", ""))
//                                    .commit()
                        } else {
                            showToast(getApplicationContext(),
                                    addToOneKeyList(getApplicationContext(),
                                            "AutoFreezeApplicationList",
                                            pkgName)? R.string.added : R.string.addFailed);
//                            showToast(getApplicationContext(), sharedPreferences.edit()
//                                    .putString(
//                                            "pkgName",
//                                            pkgNameList + "|" + pkgName + "|")
//                                    .commit() ? R.string.added : R.string.addFailed);
                        }
                        finish();
                        break;
                    case 4:
                        if (UFPkgNameList.contains("|" + pkgName + "|")) {
                            showToast(getApplicationContext(),
                                    removeFromOneKeyList(getApplicationContext(),
                                            "OneKeyUFApplicationList",
                                            pkgName) ? R.string.removed : R.string.removeFailed);
//                            UFSharedPreferences.edit()
//                                    .putString(
//                                            "pkgName",
//                                            UFPkgNameList.replace("|" + pkgName + "|", ""))
//                                    .commit()
                        } else {
                            showToast(getApplicationContext(),
                                    addToOneKeyList(getApplicationContext(),
                                            "OneKeyUFApplicationList",
                                            pkgName) ? R.string.added : R.string.addFailed);
//                            UFSharedPreferences.edit()
//                                    .putString(
//                                            "pkgName",
//                                            UFPkgNameList + "|" + pkgName + "|")
//                                    .commit()
                        }
                        finish();
                        break;
                    case 5:
                        if (FreezeOnceQuitPkgNameList.contains("|" + pkgName + "|")) {
                            showToast(getApplicationContext(),
                                    removeFromOneKeyList(getApplicationContext(),
                                            "FreezeOnceQuit",
                                            pkgName) ? R.string.removed : R.string.removeFailed);
                        } else {
                            showToast(getApplicationContext(),
                                    addToOneKeyList(getApplicationContext(),
                                            "FreezeOnceQuit",
                                            pkgName) ? R.string.added : R.string.addFailed);
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            if(!preferences.getBoolean("freezeOnceQuit",false)){
                                preferences.edit().putBoolean("freezeOnceQuit",true).apply();
                                if (!isAccessibilitySettingsOn(getApplicationContext())){
                                    showToast(SelectOperation.this,R.string.needActiveAccessibilityService);
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
        this.overridePendingTransition(R.anim.pullup,R.anim.pulldown);
    }
}
