package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import static cf.playhi.freezeyou.Support.shortcutMakeDialog;
import static cf.playhi.freezeyou.Support.showToast;

public class Freeze extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcut);
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        init();
    }

    private void init(){

        String backData = "shortCut";
        String pkgName;
        pkgName = getIntent().getStringExtra("pkgName");
        if (pkgName==null){
            showToast(getApplicationContext(),"参数错误");
            Freeze.this.finish();
        } else if (pkgName.equals("")){
            showToast(getApplicationContext(),"参数错误");
            Freeze.this.finish();
        } else if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
            try{
                shortcutMakeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName,2);
            }catch (Exception e){
                e.printStackTrace();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        "pkgName2Name", Context.MODE_PRIVATE);
                shortcutMakeDialog(sharedPreferences.getString(pkgName,getString(R.string.notice)),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName,2);
            }
        } else {
            try {
                shortcutMakeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName, 0)).toString(), getString(R.string.chooseDetailAction), Freeze.this, true, backData, pkgName,1);
            } catch (Exception e) {
                e.printStackTrace();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        "pkgName2Name", Context.MODE_PRIVATE);
                shortcutMakeDialog(sharedPreferences.getString(pkgName,getString(R.string.notice)), getString(R.string.chooseDetailAction), Freeze.this, true, backData, pkgName,1);
            }
        }
    }
}
