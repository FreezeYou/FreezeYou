package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;

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
            Freeze.this.finish();
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
                                process = Runtime.getRuntime().exec("su");
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                backData += line + "\n";
//            }
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm disable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                                process.waitFor();
                            } catch (Exception e){
                                Log.e("Error",e.getMessage());
                                Toast.makeText(getApplicationContext(),"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(true);
                            }
                            Toast.makeText(getApplicationContext(),"执行完成",Toast.LENGTH_LONG).show();
                            destroyProcess(true);
                        }
                    }
                })
                .setPositiveButton("解冻", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (backData.equals("backData")){
                            try {
                                process = Runtime.getRuntime().exec("su");
                                outputStream = new DataOutputStream(process.getOutputStream());
                                outputStream.writeBytes("pm enable " + pkgName + "\n");
                                outputStream.writeBytes("exit\n");
                                outputStream.flush();
                                process.waitFor();
                            } catch (Exception e){
                                Log.e("Error",e.getMessage());
                                Toast.makeText(getApplicationContext(),"异常 "+e.getMessage(),Toast.LENGTH_LONG).show();
                                destroyProcess(true);
                            }
//                            Toast.makeText(getApplicationContext(),"执行完成",Toast.LENGTH_LONG).show();
//                            destroyProcess(true);
                            AlertDialog alertDialog = new AlertDialog.Builder(Freeze.this)
                                    .setTitle("提示")
                                    .setMessage("已解冻，立即启动？")
                                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            destroyProcess(true);
                                        }
                                    })
                                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int ii) {
                                            startActivity(new Intent(
                                                    getPackageManager().getLaunchIntentForPackage(pkgName)
                                            ));
                                            destroyProcess(true);
                                        }
                                    })
                                    .create();
                            alertDialog.show();
                        }
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destroyProcess(true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        destroyProcess(true);
                    }
                })
                .create();
        alertDialog.show();
    }

    private void destroyProcess(Boolean finish){
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            process.destroy();
            if (finish){
                Freeze.this.finish();
            }
        } catch (Exception e) {
            if (finish){
                Freeze.this.finish();
            }
        }
    }
}
