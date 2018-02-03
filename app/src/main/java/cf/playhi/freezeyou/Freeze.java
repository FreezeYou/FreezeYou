package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
            Toast.makeText(getApplicationContext(),"参数错误",Toast.LENGTH_LONG).show();
            Freeze.this.finish();
        } else if (pkgName.equals("")){
            Toast.makeText(getApplicationContext(),"参数错误",Toast.LENGTH_LONG).show();
            Freeze.this.finish();
        } else if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
            try{
                Support.makeDialog2(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }catch (Exception e){
                e.printStackTrace();
                Support.makeDialog2(getString(R.string.notice),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }
        } else {
            try {
                Support.makeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName, 0)).toString(), getString(R.string.chooseDetailAction), Freeze.this, true, backData, pkgName);
            } catch (Exception e) {
                e.printStackTrace();
                Support.makeDialog(getString(R.string.notice), getString(R.string.chooseDetailAction), Freeze.this, true, backData, pkgName);
            }
        }
    }
}
