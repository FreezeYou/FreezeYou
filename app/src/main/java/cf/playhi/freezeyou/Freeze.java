package cf.playhi.freezeyou;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class Freeze extends Activity{

    String backData = "backData";
    String pkgName = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pkgName = getIntent().getStringExtra("pkgName");
        if (pkgName.equals("unknown")||pkgName.equals("")){
            Toast.makeText(getApplicationContext(),"参数错误",Toast.LENGTH_LONG).show();
            Freeze.this.finish();
        }
        if (getPackageManager().getLaunchIntentForPackage(pkgName)!=null){
            try{
                Support.makeDialog2(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),"请选择具体操作",Freeze.this,true,backData,pkgName);
            }catch (Exception e){
                e.printStackTrace();
                Support.makeDialog2("提示","请选择具体操作",Freeze.this,true,backData,pkgName);
            }
        } else {
            try{
                Support.makeDialog(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName,0)).toString(),"请选择具体操作",Freeze.this,true,backData,pkgName);
            }catch (Exception e){
                e.printStackTrace();
                Support.makeDialog("提示","请选择具体操作",Freeze.this,true,backData,pkgName);
            }
        }
    }


}
