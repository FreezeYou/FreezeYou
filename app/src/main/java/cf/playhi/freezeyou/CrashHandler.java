package cf.playhi.freezeyou;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

//最终参考 https://blog.csdn.net/soul_code/article/details/50601960
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler instance;

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String logPath;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            logPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + File.separator
                    + "FreezeYou"
                    + File.separator
                    + "Log";

            File file = new File(logPath);
            if (!file.exists()) {
                if (!file.mkdirs()){
                    Log.e("crash handler", "mkdirs failed");
                }
            }
            try {
                FileWriter fw = new FileWriter(logPath + File.separator
                        + Long.toString(new Date().getTime()) + ".log", true);
                fw.write(new Date() + "\n");
                // 错误信息
                // 这里还可以加上当前的系统版本，机型型号 等等信息
                StackTraceElement[] stackTrace = throwable.getStackTrace();
                fw.write(throwable.getMessage() + "\n");
                for (StackTraceElement aStackTrace : stackTrace) {
                    fw.write("file:" + aStackTrace.getFileName() + " class:"
                            + aStackTrace.getClassName() + " method:"
                            + aStackTrace.getMethodName() + " line:"
                            + aStackTrace.getLineNumber() + "\n");
                }
                fw.write("\n");
                fw.close();
                // 上传错误信息到服务器
                // uploadToServer();
            } catch (Exception e) {
                Log.e("crash handler", "load file failed...", e.getCause());
            }
        }
        throwable.printStackTrace();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
