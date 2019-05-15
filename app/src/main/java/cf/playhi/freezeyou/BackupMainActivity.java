package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import net.grandcentrix.tray.AppPreferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static cf.playhi.freezeyou.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.ThemeUtils.processSetTheme;

public class BackupMainActivity extends Activity {

    final static String false_base64_encoded = Base64.encodeToString("0".getBytes(), Base64.DEFAULT);
    final static String true_base64_encoded = Base64.encodeToString("1".getBytes(), Base64.DEFAULT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getActionBar());
        setContentView(R.layout.bma_main);

        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        processQRCodeAndQRCodeImageView(processQRCodeContent());
    }

    private String processQRCodeContent() {
        final AppPreferences appPreferences = new AppPreferences(getApplicationContext());
        final SharedPreferences defSP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String nl = System.getProperty("line.separator");

        StringBuilder result = new StringBuilder("<target=FreezeYou,version=1,category=" + "base" + ">");

        // boolean 开始
        result.append(nl).append("<boolean>");

        result.append(nl).append("allowEditWhenCreateShortcut").append(":");
        result.append(convertBooleanSharedPreference(defSP, "allowEditWhenCreateShortcut", true));

        result.append(nl).append("noCaution").append(":");
        result.append(convertBooleanSharedPreference(defSP, "noCaution", false));

        result.append(nl).append("saveOnClickFunctionStatus").append(":");
        result.append(convertBooleanSharedPreference(defSP, "saveOnClickFunctionStatus", false));

        result.append(nl).append("cacheApplicationsIcons").append(":");
        result.append(convertBooleanSharedPreference(defSP, "cacheApplicationsIcons", false));

        result.append(nl).append("showInRecents").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "showInRecents", true));

        result.append(nl).append("lesserToast").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "lesserToast", false));

        result.append(nl).append("notificationBarFreezeImmediately").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "notificationBarFreezeImmediately", true));

        result.append(nl).append("notificationBarDisableSlideOut").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "notificationBarDisableSlideOut", false));

        result.append(nl).append("notificationBarDisableClickDisappear").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "notificationBarDisableClickDisappear", false));

        result.append(nl).append("onekeyFreezeWhenLockScreen").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "onekeyFreezeWhenLockScreen", false));

        result.append(nl).append("freezeOnceQuit").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "freezeOnceQuit", false));

        result.append(nl).append("avoidFreezeForegroundApplications").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "avoidFreezeForegroundApplications", false));

        result.append(nl).append("avoidFreezeNotifyingApplications").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "avoidFreezeNotifyingApplications", false));

        result.append(nl).append("openImmediately").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "openImmediately", false));

        result.append(nl).append("openAndUFImmediately").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "openAndUFImmediately", false));

        result.append(nl).append("shortcutAutoFUF").append(":");
        result.append(convertBooleanSharedPreference(defSP, "shortcutAutoFUF", false));

        result.append(nl).append("needConfirmWhenFreezeUseShortcutAutoFUF").append(":");
        result.append(convertBooleanSharedPreference(defSP, "needConfirmWhenFreezeUseShortcutAutoFUF", false));

        result.append(nl).append("openImmediatelyAfterUnfreezeUseShortcutAutoFUF").append(":");
        result.append(convertBooleanSharedPreference(defSP, "openImmediatelyAfterUnfreezeUseShortcutAutoFUF", true));

        result.append(nl).append("enableInstallPkgFunc").append(":");
        result.append(convertBooleanSharedPreference(defSP, "enableInstallPkgFunc", true));

        result.append(nl).append("tryDelApkAfterInstalled").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "tryDelApkAfterInstalled", false));

        result.append(nl).append("useForegroundService").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "useForegroundService", false));

        result.append(nl).append("debugModeEnabled").append(":");
        result.append(convertBooleanSharedPreference(appPreferences, "debugModeEnabled", false));

        result.append(nl).append("</boolean>");
        // boolean 结束


        return GZipUtils.gzipCompress(result.toString());//Base64.encodeToString(result.toString().getBytes(),Base64.DEFAULT)
    }

    private String convertBooleanSharedPreference(
            SharedPreferences sharedPreferences, String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue) ? true_base64_encoded : false_base64_encoded;
    }

    private String convertBooleanSharedPreference(
            AppPreferences appPreferences, String key, boolean defValue) {
        return appPreferences.getBoolean(key, defValue) ? true_base64_encoded : false_base64_encoded;
    }

    private void processQRCodeAndQRCodeImageView(String qrContent) {
        ImageView bma_main_qrCode_imageView = findViewById(R.id.bma_main_qrCode_imageView);

        int width = getWindowManager().getDefaultDisplay().getWidth();
        if (width <= 0) {
            width = 300;
        }

        int wh = (int) (width * 0.6);
        int padding = (width - wh) / 2;

        int frontColor;
        if (Build.VERSION.SDK_INT < 21) {
            frontColor = Color.BLACK;
        } else {
            // https://stackoverflow.com/questions/27611173/how-to-get-accent-color-programmatically
            TypedValue typedValue = new TypedValue();
            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.colorAccent});
            frontColor = a.getColor(0, 0);
            a.recycle();
        }

        final Bitmap qrCode = QRCodeUtil.createQRCodeBitmap(
                qrContent == null ? "" : qrContent,
                wh, wh, "L", "1", frontColor, Color.TRANSPARENT);

        bma_main_qrCode_imageView.setImageBitmap(qrCode);

        bma_main_qrCode_imageView.setPadding(padding, padding, padding, padding);

        bma_main_qrCode_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imgPath = getCacheDir() + File.separator + new Date().toString();
                ApplicationIconUtils.writeBitmapToFile(imgPath, qrCode);
                startActivity(
                        new Intent(BackupMainActivity.this, FullScreenImageViewerActivity.class)
                                .putExtra("imgPath", imgPath)
                );
            }
        });
    }
}


class QRCodeUtil {
    //修改自 https://www.jianshu.com/p/b275e818de6a

    /**
     * 创建二维码位图
     *
     * @param content 字符串内容(支持中文)
     * @param width   位图宽度(单位:px)
     * @param height  位图高度(单位:px)
     * @return Bitmap
     */
    static Bitmap createQRCodeBitmap(String content, int width, int height) {
        return createQRCodeBitmap(content, width, height, "UTF-8", "M", "1", Color.BLACK, Color.WHITE);
    }

    /**
     * @param content    字符串内容(支持中文)
     * @param width      位图宽度(单位:px)
     * @param height     位图高度(单位:px)
     * @param ec         容错级别
     * @param margin     空白边距
     * @param front      前景色的自定义颜色值
     * @param background 背景色的自定义颜色值
     * @return Bitmap
     */
    static Bitmap createQRCodeBitmap(String content, int width, int height, String ec, String margin, int front, int background) {
        return createQRCodeBitmap(content, width, height, "UTF-8", ec, margin, front, background);
    }

    /**
     * 创建二维码位图 (支持自定义配置和自定义样式)
     *
     * @param content          字符串内容
     * @param width            位图宽度,要求>=0(单位:px)
     * @param height           位图高度,要求>=0(单位:px)
     * @param character_set    字符集/字符转码格式。传null时,zxing源码默认使用 "ISO-8859-1"
     * @param error_correction 容错级别。传null时,zxing源码默认使用 "L"
     * @param margin           空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"。
     * @param color_black      黑色色块的自定义颜色值
     * @param color_white      白色色块的自定义颜色值
     * @return Bitmap
     */
    private static Bitmap createQRCodeBitmap(String content, int width, int height,
                                             String character_set, String error_correction, String margin,
                                             int color_black, int color_white) {

        /* 1.参数合法性判断 */
        if (TextUtils.isEmpty(content)) { // 字符串内容判空
            return null;
        }

        if (width < 0 || height < 0) { // 宽和高都需要>=0
            return null;
        }

        try {
            /* 2.设置二维码相关配置,生成BitMatrix(位矩阵)对象 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();

            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set); // 字符转码格式设置
            }

            if (!TextUtils.isEmpty(error_correction)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction); // 容错级别设置
            }

            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin); // 空白边距设置
            }
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /* 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black; // 黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white; // 白色色块像素设置
                    }
                }
            }

            /* 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }
}

class GZipUtils {
    //参考 http://www.cnblogs.com/whoislcj/p/5473806.html

    /**
     * @param unGzipStr 被压缩字符串
     * @return 压缩后字符串，失败返回 String s=""
     */
    public static String gzipCompress(String unGzipStr) {

        if (TextUtils.isEmpty(unGzipStr)) {
            return "";
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream);
            gzip.write(unGzipStr.getBytes());
            gzip.close();
            byte[] b = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return Base64.encodeToString(b, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * @param gzipStr 已压缩过的 String
     * @return 解压缩后的 String
     */
    public static String gzipDecompress(String gzipStr) {
        if (TextUtils.isEmpty(gzipStr)) {
            return "";
        }
        byte[] t = Base64.decode(gzipStr, Base64.DEFAULT);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(t);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
