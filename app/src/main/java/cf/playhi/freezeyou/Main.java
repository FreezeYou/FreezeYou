package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cf.playhi.freezeyou.Support.buildAlertDialog;
import static cf.playhi.freezeyou.Support.checkFrozen;
import static cf.playhi.freezeyou.Support.createShortCut;
import static cf.playhi.freezeyou.Support.getBitmapFromLocalFile;
import static cf.playhi.freezeyou.Support.getDevicePolicyManager;
import static cf.playhi.freezeyou.Support.getVersionCode;
import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.makeDialog;
import static cf.playhi.freezeyou.Support.makeDialog2;
import static cf.playhi.freezeyou.Support.showToast;

public class Main extends Activity {
    private static String[] autoFreezePkgNameList = new String[0];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            manageCrashLog();
        } catch (Exception e){
            e.printStackTrace();
            go();
        }
        //throw new RuntimeException("自定义异常：仅于异常上报测试中使用");//发版前务必注释
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        go();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_createOneKeyFreezeShortCut:
                createShortCut(
                        getString(R.string.oneKeyFreeze),
                        "",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),OneKeyFreeze.class,
                        "OneKeyFreeze",
                        this
                );
                return true;
            case R.id.menu_createOnlyFrozenShortCut:
                createShortCut(
                        getString(R.string.onlyFrozen),
                        "OF",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),Main.class,
                        "OF",
                        this
                );
                return true;
            case R.id.menu_createOnlyUFShortCut:
                createShortCut(
                        getString(R.string.onlyUF),
                        "UF",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),Main.class,
                        "UF",
                        this
                );
                return true;
            case R.id.menu_createOnlyOnekeyShortCut:
                createShortCut(
                        getString(R.string.onlyOnekey),
                        "OO",
                        getResources().getDrawable(R.mipmap.ic_launcher_round),Main.class,
                        "OO",
                        this
                );
                return true;
            case R.id.menu_about:
                buildAlertDialog(this,R.mipmap.ic_launcher_round,R.string.about_message,R.string.about)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNeutralButton(R.string.visitWebsite, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri webPage = Uri.parse("https://app.playhi.cf/freezeyou");
                                Intent about = new Intent(Intent.ACTION_VIEW, webPage);
                                if (about.resolveActivity(getPackageManager()) != null) {
                                    startActivity(about);
                                } else {
                                    showToast(getApplicationContext(),R.string.plsVisitPXXXX);
                                }
                            }
                        })
                        .setPositiveButton(R.string.addQQGroup, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Support.joinQQGroup(Main.this);
                            }
                        }).create().show();
                return true;
            case R.id.menu_oneKeyFreezeImmediately:
                startActivity(new Intent(this,OneKeyFreeze.class));
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
            case R.id.menu_update:
                Uri webPage = Uri.parse("https://app.playhi.cf/freezeyou/checkupdate.php?v=" + getVersionCode(this));
                Intent about = new Intent(Intent.ACTION_VIEW, webPage);
                if (about.resolveActivity(getPackageManager()) != null) {
                    startActivity(about);
                } else {
                    showToast(this,"请访问 https://app.playhi.cf/freezeyou/checkupdate.php?v=" + getVersionCode(this));
                }
                return true;
            case R.id.menu_exit:
                finish();
                return true;
            case R.id.menu_moreSettings:
                startActivity(new Intent(this,SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void generateList(String filter){
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
        Drawable icon;
        List<ApplicationInfo> applicationInfo = getApplicationContext().getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        int size = applicationInfo.size();
        autoFreezePkgNameList = getApplicationContext().getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        switch (filter) {
            case "all":
                for (int i = 0; i < size; i++) {
                    Map<String, Object> keyValuePair = processAppStatus(
                            getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString(),
                            applicationInfo.get(i).packageName,
                            applicationInfo.get(i)
                    );
                    if (keyValuePair!=null){
                        AppList.add(keyValuePair);
                    }
                }
                if (AppList.size()==0) {
                    addNotAvailablePair(getApplicationContext(), AppList);
                }
                break;
            case "OF":
                for (int i = 0; i < size; i++) {
                    Map<String, Object> keyValuePair = processAppStatus(
                            getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString(),
                            applicationInfo.get(i).packageName,
                            applicationInfo.get(i)
                    );
                    if (keyValuePair!=null){
                        try {
                            if (R.drawable.bluedot == (int) keyValuePair.get("isFrozen")) {
                                AppList.add(keyValuePair);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (AppList.size()==0){
                    addNotAvailablePair(getApplicationContext(),AppList);
                }
                break;
            case "UF":
                for (int i = 0; i < size; i++) {
                    Map<String, Object> keyValuePair = processAppStatus(
                            getPackageManager().getApplicationLabel(applicationInfo.get(i)).toString(),
                            applicationInfo.get(i).packageName,
                            applicationInfo.get(i)
                    );
                    if (keyValuePair!=null){
                        try {
                            if (R.drawable.whitedot == (int) keyValuePair.get("isFrozen")) {
                                AppList.add(keyValuePair);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (AppList.size()==0){
                    addNotAvailablePair(getApplicationContext(),AppList);
                }
                break;
            case "OO":
                for (String aPkgNameList : autoFreezePkgNameList) {
                    aPkgNameList = aPkgNameList.replaceAll("\\|","");
                    String name;
                    try{
                        name = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(aPkgNameList,0)).toString();
                    } catch (Exception e){
                        if (checkFrozen(getApplicationContext(),aPkgNameList)){
                            final SharedPreferences sharedPreferences = getApplicationContext().getApplicationContext().getSharedPreferences(
                                    "pkgName2Name", Context.MODE_PRIVATE);
                            name = sharedPreferences.getString(aPkgNameList,getResources().getString(R.string.notice));
                        } else {
                            name = getResources().getString(R.string.uninstalled);
                        }
                    }
                    if (!("android".equals(aPkgNameList) || "cf.playhi.freezeyou".equals(aPkgNameList) || "".equals(aPkgNameList))) {
                        Map<String, Object> keyValuePair = new HashMap<>();
                        try{
                            icon = getPackageManager().getApplicationIcon(getPackageManager().getApplicationInfo(aPkgNameList,0));
                        } catch (Exception e){
                            if (checkFrozen(getApplicationContext(),aPkgNameList)){
                                Bitmap bitmap = getBitmapFromLocalFile(getApplicationContext().getFilesDir()+"/icon/"+aPkgNameList+".png");
                                if (bitmap!=null){
                                    icon = new BitmapDrawable(bitmap);
                                } else {
                                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                                }
                            } else {
                                icon = getResources().getDrawable(android.R.drawable.ic_menu_delete);//ic_delete
                            }
                        }
                        keyValuePair.put("Img", icon);
                        processFrozenStatus(keyValuePair,name,aPkgNameList);
                        keyValuePair.put("isAutoList", ifOnekeyFreezeList(aPkgNameList) ? R.drawable.bluedot : R.drawable.whitedot);
                        keyValuePair.put("PackageName", aPkgNameList);
                        AppList.add(keyValuePair);
                    }
                }
                if (AppList.size()==0) {
                    addNotAvailablePair(getApplicationContext(), AppList);
                }
                break;
            case "OS":
                for (int i = 0; i < size; i++) {
                    ApplicationInfo applicationInfo1 = applicationInfo.get(i);
                    if ((applicationInfo1.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                        Map<String, Object> keyValuePair = processAppStatus(
                                getPackageManager().getApplicationLabel(applicationInfo1).toString(),
                                applicationInfo1.packageName,
                                applicationInfo1
                        );
                        if (keyValuePair != null) {
                            AppList.add(keyValuePair);
                        }
                    }
                }
                if (AppList.size()==0) {
                    addNotAvailablePair(getApplicationContext(), AppList);
                }
                break;
            case "OU":
                for (int i = 0; i < size; i++) {
                    ApplicationInfo applicationInfo1 = applicationInfo.get(i);
                    if ((applicationInfo1.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                        Map<String, Object> keyValuePair = processAppStatus(
                                getPackageManager().getApplicationLabel(applicationInfo1).toString(),
                                applicationInfo1.packageName,
                                applicationInfo1
                        );
                        if (keyValuePair != null) {
                            AppList.add(keyValuePair);
                        }
                    }
                }
                if (AppList.size()==0) {
                    addNotAvailablePair(getApplicationContext(), AppList);
                }
                break;
            default:
                break;
        }

        if (!AppList.isEmpty()) {
            Collections.sort(AppList, new Comparator<Map<String,Object>>() {

                @Override
                public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                    return ((String) stringObjectMap.get("PackageName")).compareTo((String) t1.get("PackageName"));
                }
            });
        }

        final SimpleAdapter adapter = new SimpleAdapter(Main.this, AppList,
                R.layout.app_list_1, new String[] { "Img","Name",
                "PackageName", "isFrozen", "isAutoList"}, new int[] { R.id.img,R.id.name,R.id.pkgName,R.id.isFrozen,R.id.isAutoFreezeList});//isFrozen、isAutoList传图像资源id

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
                if (TextUtils.isEmpty(charSequence)){
                    app_listView.setAdapter(adapter);
                } else {
                    SimpleAdapter processedAdapter = new SimpleAdapter(Main.this, processListFilter(charSequence,AppList),
                            R.layout.app_list_1, new String[] { "Img","Name",
                            "PackageName", "isFrozen", "isAutoList"}, new int[] { R.id.img,R.id.name,R.id.pkgName,R.id.isFrozen,R.id.isAutoFreezeList});

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

        app_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String,String> map=(HashMap<String,String>)app_listView.getItemAtPosition(i);
                final String name=map.get("Name");
                final String pkgName=map.get("PackageName");
                if (!(getString(R.string.notAvailable).equals(name))){
                    try {
                        int tmp = getPackageManager().getApplicationEnabledSetting(pkgName);
                        if (tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER||tmp==PackageManager.COMPONENT_ENABLED_STATE_DISABLED||checkFrozen(Main.this,pkgName)){
                            makeDialog(name,getString(R.string.chooseDetailAction),Main.this,false,"backData",pkgName);
                        } else {
                            makeDialog2(name,getString(R.string.chooseDetailAction),Main.this,false,"backData",pkgName);
                        }
                    } catch (Exception e){
                        makeDialog(name,getString(R.string.chooseDetailAction),Main.this,false,"backData",pkgName);
                    }
                }
                return true;
            }
        });

        app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String,String> map=(HashMap<String,String>)app_listView.getItemAtPosition(i);
                final String name=map.get("Name");
                final String pkgName=map.get("PackageName");
                if (!getString(R.string.notAvailable).equals(name)) {
                    startActivityForResult(
                            new Intent(Main.this, SelectOperation.class).
                                    putExtra("Name",name).
                                    putExtra("pkgName",pkgName).
                                    putExtra("index",i),
                            1092
                    );
                    overridePendingTransition(R.anim.pullup,R.anim.pulldown);
                }
            }
        });

//        app_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(final AdapterView<?> adapterView, View view, final int i, final long l) {
//                HashMap<String,String> map=(HashMap<String,String>)app_listView.getItemAtPosition(i);
//                final String name=map.get("Name");
//                final String pkgName=map.get("PackageName");
//                if (!getString(R.string.notAvailable).equals(name)) {
//                    buildAlertDialog(Main.this,getApplicationIcon(Main.this,pkgName,null),getResources().getString(R.string.createFreezeShortcutNotice),name)
//                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//
//                                }
//                            })
//                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int ii) {
//                                    try {
//                                        createShortCut(
//                                                name.replace("(" + getString(R.string.frozen) + ")", ""),
//                                                pkgName,
//                                                getPackageManager().getApplicationIcon(pkgName),
//                                                Freeze.class,
//                                                "FreezeYou! "+pkgName
//                                        );
//                                    } catch (PackageManager.NameNotFoundException e) {
//                                        showToast(getApplicationContext(), R.string.cannotFindApp);
//                                    }
//                                }
//                            })
//                            .setNeutralButton(R.string.more, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this)
//                                            .setTitle(R.string.more)
//                                            .setMessage(getString(R.string.chooseDetailAction))
//                                            .setNeutralButton(R.string.appDetail, new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                                    Uri uri = Uri.fromParts("package", pkgName, null);
//                                                    intent.setData(uri);
//                                                    try {
//                                                        startActivity(intent);
//                                                    } catch (Exception e) {
//                                                        e.printStackTrace();
//                                                        showToast(getApplicationContext(), e.getLocalizedMessage());
//                                                    }
//                                                }
//                                            });
//                                    final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
//                                            "AutoFreezeApplicationList", Context.MODE_PRIVATE);
//                                    final String pkgNameList = sharedPreferences.getString("pkgName", "");
//                                    if (pkgNameList.contains("|" + pkgName + "|")) {
//                                        dialog.setPositiveButton(R.string.removeFromOneKeyList, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int ii) {
//                                                showToast(getApplicationContext(), sharedPreferences.edit()
//                                                        .putString(
//                                                                "pkgName",
//                                                                pkgNameList.replace("|" + pkgName + "|", ""))
//                                                        .commit() ? R.string.removed : R.string.removeFailed);
//                                            }
//                                        });
//                                    } else {
//                                        dialog.setPositiveButton(R.string.addToOneKeyList, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int ii) {
//                                                showToast(getApplicationContext(), sharedPreferences.edit().putString("pkgName", pkgNameList + "|" + pkgName + "|").commit() ? R.string.added : R.string.addFailed);
//                                            }
//                                        });
//                                    }
//                                    dialog.create().show();
//                                }
//                            })
//                            .create().show();
//                }
//            }
//        });
    }

//    private static void addMRootApplications(Context context,List<Map<String, Object>> AppList){
////        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(
////                "FrozenList", Context.MODE_PRIVATE);
////        final String pkgNameList = sharedPreferences.getString("pkgName", "");
////        String[] pkgNameListKeyValuePair = pkgNameList.split("\\|");
////        final SharedPreferences pkgName2NameSharedPreferences = context.getApplicationContext().getSharedPreferences(
////                "pkgName2Name", Context.MODE_PRIVATE);
////        for (String aPkgNameListKeyValuePair : pkgNameListKeyValuePair) {
////            if (!"".equals(aPkgNameListKeyValuePair)){
////                Map<String, Object> keyValuePair = new HashMap<>();
////                Bitmap bitmap = getBitmapFromLocalFile(context.getFilesDir()+"/icon/"+aPkgNameListKeyValuePair+".png");
////                if (bitmap!=null){
////                    keyValuePair.put("Img", new BitmapDrawable(bitmap));
////                } else {
////                    keyValuePair.put("Img", R.mipmap.ic_launcher_round);
////                }
////                keyValuePair.put("Name",
////                        addIfOnekeyFreezeList(context,pkgName2NameSharedPreferences.getString(
////                                aPkgNameListKeyValuePair,
////                                context.getString(R.string.notAvailable)) + "(" + context.getString(R.string.frozen)+")",
////                                aPkgNameListKeyValuePair
////                        )
////                );
////                keyValuePair.put("isAutoList", ifOnekeyFreezeList(aPkgNameListKeyValuePair) ? R.drawable.bluedot : R.drawable.whitedot);
////                keyValuePair.put("isFrozen",R.drawable.bluedot);
////                keyValuePair.put("PackageName", aPkgNameListKeyValuePair);
////                AppList.add(keyValuePair);
////            }
////        }
//    }

    private static void addNotAvailablePair(Context context,List<Map<String,Object>> AppList){
        Map<String, Object> keyValuePair = new HashMap<>();
        keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
        keyValuePair.put("Name", context.getString(R.string.notAvailable));
        keyValuePair.put("PackageName", context.getString(R.string.notAvailable));
        AppList.add(keyValuePair);
    }

    private static boolean ifOnekeyFreezeList(String pkgName){
        for(String s: autoFreezePkgNameList){
            if(s.replaceAll("\\|","").equals(pkgName))
                return true;
        }
        return false;
    }

    private static String addIfOnekeyFreezeList(Context context,String name,String pkgName){
        return ifOnekeyFreezeList(pkgName) ? name + "(" + context.getResources().getString(R.string.oneKeyFreeze) + ")" : name;
    }

    private void manageCrashLog() throws Exception{
        File crashCheck = new File(Environment.getDataDirectory().getPath()
                + File.separator
                + "data"
                + File.separator
                +"cf.playhi.freezeyou"
                + File.separator
                + "log"
                + File.separator
                + "NeedUpload.log");
        if (crashCheck.exists()){
            BufferedReader bufferedReader = new BufferedReader(new FileReader(crashCheck));
            String filePath = bufferedReader.readLine();
            bufferedReader.close();
            FileInputStream fileInputStream = new FileInputStream(filePath);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[fileInputStream.available()];
            fileInputStream.read(buffer);
            byteArrayOutputStream.write(buffer);
            fileInputStream.close();
            buildAlertDialog(Main.this,R.mipmap.ic_launcher_new_round,R.string.ifUploadCrashLog,R.string.notice)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri webPage = Uri.parse("https://app.playhi.cf/freezeyou/crashReport.php?data="+ Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT));
                            Intent report = new Intent(Intent.ACTION_VIEW, webPage);
                            if (report.resolveActivity(getPackageManager()) != null) {
                                startActivity(report);
                            } else {
                                showToast(Main.this,R.string.failed);
                            }
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

    private List<Map<String, Object>> processListFilter(CharSequence prefix,List<Map<String, Object>> unfilteredValues){

        String prefixString = prefix.toString().toLowerCase();

        if (unfilteredValues!=null){
            int count = unfilteredValues.size();

            List<Map<String, Object>> newValues = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                try{
                    Map<String, Object> h = unfilteredValues.get(i);
                    if (((String) h.get("Name")).toLowerCase().contains(prefixString)) {
                        newValues.add(h);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            return newValues;
        }

        return new ArrayList<>();
    }

    private void go(){
//        autoFreezePkgNameList = getApplicationContext().getSharedPreferences(
//                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        Thread initThread;
        initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String mode = getIntent().getStringExtra("pkgName");//快捷方式提供
                if (mode == null){
                    mode = PreferenceManager.getDefaultSharedPreferences(Main.this).getString("launchMode","all");
                }
                switch (mode){
                    case "OF":
                        generateList("OF");
                        break;
                    case "UF":
                        generateList("UF");
                        break;
                    case "OO":
                        generateList("OO");
                        break;
                    case "OS":
                        generateList("OS");
                        break;
                    case "OU":
                        generateList("OU");
                        break;
                    default:
                        generateList("all");
                        break;
                }
            }
        });
        initThread.start();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Main.this);
        if (!sharedPref.getBoolean("noCaution",false)){
            buildAlertDialog(Main.this,R.mipmap.ic_launcher_round,R.string.cautionContent,R.string.caution)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int ii) {
                        }
                    })
                    .setNeutralButton(R.string.hMRoot, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri webPage = Uri.parse("https://github.com/Playhi/FreezeYou/wiki/%E5%85%8DROOT%E4%BD%BF%E7%94%A8");
                            Intent about = new Intent(Intent.ACTION_VIEW, webPage);
                            if (about.resolveActivity(getPackageManager()) != null) {
                                startActivity(about);
                            } else {
                                showToast(getApplicationContext(),R.string.plsVisitPXXXX);
                            }
                        }
                    })
                    .setNegativeButton(R.string.nCaution, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPref.edit().putBoolean("noCaution",true).apply();
                        }
                    })
                    .create().show();
        }
    }

    private void processFrozenStatus(Map<String, Object> keyValuePair,String name,String packageName){
        int tmp;
        try {
            tmp = getPackageManager().getApplicationEnabledSetting(packageName);
        } catch (Exception e){
            tmp = -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isDeviceOwner(getApplicationContext())){
            if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || getDevicePolicyManager(Main.this).isApplicationHidden(cf.playhi.freezeyou.DeviceAdminReceiver.getComponentName(getApplicationContext()),packageName)){
                keyValuePair.put("Name", addIfOnekeyFreezeList(Main.this,name + "(" + getString(R.string.frozen) + ")",packageName));
                keyValuePair.put("isFrozen",R.drawable.bluedot);
            } else {
                keyValuePair.put("Name", addIfOnekeyFreezeList(Main.this,name,packageName));
                keyValuePair.put("isFrozen",R.drawable.whitedot);
            }
        } else {
            if (tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || tmp == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ) {
                keyValuePair.put("Name", addIfOnekeyFreezeList(Main.this,name + "(" + getString(R.string.frozen) + ")",packageName));
                keyValuePair.put("isFrozen",R.drawable.bluedot);
            } else {
                keyValuePair.put("Name", addIfOnekeyFreezeList(Main.this,name,packageName));
                keyValuePair.put("isFrozen",R.drawable.whitedot);
            }
        }
    }

    private Map<String, Object> processAppStatus(String name,String packageName,ApplicationInfo applicationInfo){
        if (!("android".equals(packageName) || "cf.playhi.freezeyou".equals(packageName))) {
            Map<String, Object> keyValuePair = new HashMap<>();
            Drawable icon = getPackageManager().getApplicationIcon(applicationInfo);
            if (icon != null) {
                keyValuePair.put("Img", icon);
            } else {
                keyValuePair.put("Img", android.R.drawable.sym_def_app_icon);
            }
            processFrozenStatus(keyValuePair, name, packageName);
            keyValuePair.put("isAutoList", ifOnekeyFreezeList(packageName) ? R.drawable.bluedot : R.drawable.whitedot);
            keyValuePair.put("PackageName", packageName);
            return keyValuePair;
        }
        return null;
    }
    //TODO:运行中亮绿灯（列表、与白点、蓝点并列，覆盖是否已冻结状态）//高考
    //TODO:Main.java查看列表icon等代码整理&复用//高考
}
