package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;
import static cf.playhi.freezeyou.Support.shortcutMakeDialog;
import static cf.playhi.freezeyou.Support.showToast;

public class Freeze extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
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
        ApplicationInfo applicationInfo = null;
        String pkgName = getIntent().getStringExtra("pkgName");
        boolean auto = getIntent().getBooleanExtra("auto",true);
        if (pkgName==null){
            showToast(getApplicationContext(),"参数错误");
            Freeze.this.finish();
        } else if ("".equals(pkgName)){
            showToast(getApplicationContext(),"参数错误");
            Freeze.this.finish();
        } else if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
            try{
                applicationInfo = getPackageManager().getApplicationInfo(pkgName,GET_UNINSTALLED_PACKAGES);
                shortcutMakeDialog(getPackageManager().getApplicationLabel(applicationInfo).toString(),getString(R.string.chooseDetailAction),Freeze.this,true,applicationInfo,pkgName,2,auto);
            }catch (Exception e){
                e.printStackTrace();
                shortcutMakeDialog(getString(R.string.notice),getString(R.string.chooseDetailAction),Freeze.this,true,applicationInfo,pkgName,2,auto);
            }
        } else {
            try {
                applicationInfo = getPackageManager().getApplicationInfo(pkgName,GET_UNINSTALLED_PACKAGES);
                shortcutMakeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName, GET_UNINSTALLED_PACKAGES)).toString(), getString(R.string.chooseDetailAction), Freeze.this, true, applicationInfo, pkgName,1,auto);
            } catch (Exception e) {
                e.printStackTrace();
                shortcutMakeDialog(getString(R.string.notice), getString(R.string.chooseDetailAction), Freeze.this, true, applicationInfo, pkgName,1,auto);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }
}
