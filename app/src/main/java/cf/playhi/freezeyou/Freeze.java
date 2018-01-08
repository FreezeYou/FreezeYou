package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class Freeze extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcut);
        String backData = "backData";
        String pkgName;
        pkgName = getIntent().getStringExtra("pkgName");
        if (pkgName.equals("")){
            Toast.makeText(getApplicationContext(),"参数错误",Toast.LENGTH_LONG).show();
            Freeze.this.finish();
        }
        if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
            try{
                Support.makeDialog2(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }catch (Exception e){
                e.printStackTrace();
                Support.makeDialog2(getString(R.string.notice),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }
        } else {
            try{
                Support.makeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }catch (Exception e){
                e.printStackTrace();
                Support.makeDialog(getString(R.string.notice),getString(R.string.chooseDetailAction),Freeze.this,true,backData,pkgName);
            }
        }
    }


}
