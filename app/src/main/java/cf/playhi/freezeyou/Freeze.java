package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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

        String backData = "backData";
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
                Support.makeDialog2(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }catch (Exception e){
                e.printStackTrace();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        "pkgName2Name", Context.MODE_PRIVATE);
                Support.makeDialog2(sharedPreferences.getString(pkgName,getString(R.string.notice)),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }
        } else {
            try {
                Support.makeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName, 0)).toString(), getString(R.string.chooseDetailAction), Freeze.this, true, backData, pkgName);
            } catch (Exception e) {
                e.printStackTrace();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        "pkgName2Name", Context.MODE_PRIVATE);
                Support.makeDialog(sharedPreferences.getString(pkgName,getString(R.string.notice)), getString(R.string.chooseDetailAction), Freeze.this, true, backData, pkgName);
            }
        }
    }
}
