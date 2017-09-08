package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

public class Freeze extends Activity{

    DataOutputStream outputStream = null;
    java.lang.Process process = null;
    String backData = "backData";
    String pkgName = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pkgName = getIntent().getStringExtra("pkgName");
        if (pkgName.equals("unknown")){
            Toast.makeText(getApplicationContext(),"参数错误",Toast.LENGTH_LONG).show();
            finish();
        }
        try {
            process = Runtime.getRuntime().exec("su");
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                backData += line + "\n";
//            }
            Toast.makeText(getApplicationContext(),process.getInputStream().toString(),Toast.LENGTH_LONG).show();
        } catch (IOException e){
            Log.e("Error",e.getMessage());
            Toast.makeText(getApplicationContext(),"获取ROOT权限时出错",Toast.LENGTH_LONG).show();
            destroyProcess();
            finish();
        }
        makeDialog("提示","请选择具体操作");
    }

    private void makeDialog(String title,String message){
        AlertDialog alertDialog = new AlertDialog.Builder(Freeze.this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("冻结", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            try {
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm disable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                            } catch (IOException e){
                                Log.e("Error",e.getMessage());
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess();
                                finish();
                            }
                            try {
                                process.waitFor();
                            }
                            catch (InterruptedException e){
                                Log.e("Error",e.getMessage());
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess();
                                finish();
                            }
                            Toast.makeText(getApplicationContext(),"执行完成",Toast.LENGTH_LONG).show();
                            destroyProcess();
                        }
                    }
                })
                .setPositiveButton("运行", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            try {
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm enable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                            } catch (IOException e){
                                Log.e("Error",e.getMessage());
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess();
                                finish();
                            }
                            try {
                                process.waitFor();
                            }
                            catch (InterruptedException e){
                                Log.e("Error",e.getMessage());
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess();
                                finish();
                            }
                            Toast.makeText(getApplicationContext(),"执行完成",Toast.LENGTH_LONG).show();
                            destroyProcess();
                        }
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destroyProcess();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void destroyProcess(){
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            process.destroy();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"退出权限时出错",Toast.LENGTH_LONG).show();
        }
    }
}
