package cf.playhi.freezeyou;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

//部分参考 https://blog.csdn.net/soul_code/article/details/50601960
class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;

    void init(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        final Date date = new Date();
        final String logPath = mContext.getCacheDir() + File.separator + "log";
        saveLog(throwable, logPath, date);

        File extCacheDir = mContext.getExternalCacheDir();
        if (extCacheDir != null) {
            saveLog(throwable, extCacheDir + File.separator + File.separator + "Log", date);
        }

        saveLocationAndMore(logPath, date);

        throwable.printStackTrace();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void saveLog(Throwable throwable, String logPath, Date date) {
        File file = new File(logPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("crash handler", "mkdirs failed");
            }
        }
        try {
            FileWriter fw = new FileWriter(logPath + File.separator
                    + date.getTime() + ".log", true);
            fw.write(date + "\n");
            fw.write("Model: " + android.os.Build.MODEL + ","
                    + android.os.Build.VERSION.SDK + ","
                    + android.os.Build.VERSION.RELEASE + "\n");
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo("cf.playhi.freezeyou", 0);
            fw.write("VN:" + packageInfo.versionName + "\n");
            fw.write("VC:" + packageInfo.versionCode + "\n");
            fw.write("LM:" + throwable.getLocalizedMessage() + "\n");
            fw.write("RM:" + throwable.getMessage() + "\n");
            for (StackTraceElement aStackTrace : stackTrace) {
                fw.write("File:" + aStackTrace.getFileName() + " Class:"
                        + aStackTrace.getClassName() + " Method:"
                        + aStackTrace.getMethodName() + " Line:"
                        + aStackTrace.getLineNumber() + "\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception e) {
            Log.e("crash handler", "load file failed...", e.getCause());
        }
    }

    private void saveLocationAndMore(String filePath, Date date) {
        //保存需要提交文件位置
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("crash handler", "mkdirs failed");
            }
        }
        try {
            FileWriter fw = new FileWriter(filePath
                    + File.separator
                    + "NeedUpload.log", true);
            fw.write(filePath + File.separator
                    + date.getTime() + ".log" + "\n");
            fw.write("\n");
            fw.close();
        } catch (Exception e) {
            Log.e("crash handler", "load file failed...", e.getCause());
        }
    }
}
