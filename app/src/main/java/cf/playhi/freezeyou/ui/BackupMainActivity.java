package cf.playhi.freezeyou.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.ClipboardUtils;
import cf.playhi.freezeyou.utils.GZipUtils;
import cf.playhi.freezeyou.utils.ToastUtils;

import static cf.playhi.freezeyou.utils.BackupUtils.getExportContent;
import static cf.playhi.freezeyou.utils.ThemeUtils.processActionBar;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;

public class BackupMainActivity extends FreezeYouBaseActivity {

//    Camera mCamera = null; 先把文本方式稳定下来，再做 QRCode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this);
        super.onCreate(savedInstanceState);
        processActionBar(getSupportActionBar());
        setContentView(R.layout.bma_main);

        onCreateInit();
    }

    private void onCreateInit() {
        initButtons();
    }

    private void initButtons() {
        Button bma_main_export_button = findViewById(R.id.bma_main_export_button);
        Button bma_main_import_button = findViewById(R.id.bma_main_import_button);
        Button bma_main_copy_button = findViewById(R.id.bma_main_copy_button);
        Button bma_main_paste_button = findViewById(R.id.bma_main_paste_button);

        bma_main_export_button.setOnClickListener(v -> {
            EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
            editText.setText(GZipUtils.gzipCompress(getExportContent(getApplicationContext())));
            editText.selectAll();
        });

        bma_main_import_button.setOnClickListener(v -> {
            EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
            startActivity(
                    new Intent(BackupMainActivity.this, BackupImportChooserActivity.class)
                            .putExtra("jsonObjectString", GZipUtils.gzipDecompress(editText.getText().toString()))
            );
        });

        bma_main_copy_button.setOnClickListener(v -> {
            EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
            if (ClipboardUtils.copyToClipboard(getApplicationContext(), editText.getText().toString())) {
                ToastUtils.showToast(BackupMainActivity.this, R.string.success);
            } else {
                ToastUtils.showToast(BackupMainActivity.this, R.string.failed);
            }
        });

        bma_main_paste_button.setOnClickListener(v -> {
            EditText editText = findViewById(R.id.bma_main_inputAndoutput_editText);
            editText.setText(ClipboardUtils.getClipboardItemText(getApplicationContext()));
        });

    }

//    先把文本方式稳定下来，再决定是否做 QRCode
//    private void processQRCodeAndQRCodeImageView(String qrContent) {
//        ImageView bma_main_qrCode_imageView = findViewById(R.id.bma_main_qrCode_imageView);
//        FrameLayout bma_main_qrCode_frameLayout = findViewById(R.id.bma_main_qrCode_frameLayout);
//
//        int width = getWindowManager().getDefaultDisplay().getWidth();
//        if (width <= 0) {
//            width = 300;
//        }
//
//        int wh = (int) (width * 0.6);
//        int padding = (width - wh) / 2;
//
//        int frontColor;
//        if (Build.VERSION.SDK_INT < 21) {
//            frontColor = Color.BLACK;
//        } else {
//            // https://stackoverflow.com/questions/27611173/how-to-get-accent-color-programmatically
//            TypedValue typedValue = new TypedValue();
//            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.colorAccent});
//            frontColor = a.getColor(0, 0);
//            a.recycle();
//        }
//
//        final Bitmap qrCode = QRCodeUtil.createQRCodeBitmap(
//                qrContent == null ? "" : qrContent,
//                wh, wh, "L", "1", frontColor, Color.TRANSPARENT);
//
//        bma_main_qrCode_imageView.setImageBitmap(qrCode);
//
//        bma_main_qrCode_frameLayout.setPadding(padding, padding, padding, padding);
//        bma_main_qrCode_frameLayout.setMinimumWidth(wh);
//        bma_main_qrCode_frameLayout.setMinimumHeight(wh);
//
//        bma_main_qrCode_imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String imgPath = getCacheDir() + File.separator + new Date().toString();
//                ApplicationIconUtils.writeBitmapToFile(imgPath, qrCode);
//                startActivity(
//                        new Intent(BackupMainActivity.this, FullScreenImageViewerActivity.class)
//                                .putExtra("imgPath", imgPath)
//                );
//            }
//        });
//    }
//
//    // https://developer.android.google.cn/guide/topics/media/camera.html
//    private boolean checkCameraHardware(Context context) {
//        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
//    }
//
//    // https://developer.android.google.cn/guide/topics/media/camera.html
//
//    /**
//     * A safe way to get an instance of the Camera object.
//     */
//    public static Camera getCameraInstance() {
//        Camera c = null;
//        try {
//            c = Camera.open(); // attempt to get a Camera instance
//        } catch (Exception e) {
//            // Camera is not available (in use or does not exist)
//        }
//        return c; // returns null if camera is unavailable
//    }
//
//}
//
//
//class QRCodeUtil {
//    //部分来自 https://www.jianshu.com/p/b275e818de6a
//
//    /**
//     * 创建二维码位图
//     *
//     * @param content 字符串内容(支持中文)
//     * @param width   位图宽度(单位:px)
//     * @param height  位图高度(单位:px)
//     * @return Bitmap
//     */
//    static Bitmap createQRCodeBitmap(String content, int width, int height) {
//        return createQRCodeBitmap(content, width, height, "UTF-8", "M", "1", Color.BLACK, Color.WHITE);
//    }
//
//    /**
//     * @param content    字符串内容(支持中文)
//     * @param width      位图宽度(单位:px)
//     * @param height     位图高度(单位:px)
//     * @param ec         容错级别
//     * @param margin     空白边距
//     * @param front      前景色的自定义颜色值
//     * @param background 背景色的自定义颜色值
//     * @return Bitmap
//     */
//    static Bitmap createQRCodeBitmap(String content, int width, int height, String ec, String margin, int front, int background) {
//        return createQRCodeBitmap(content, width, height, "UTF-8", ec, margin, front, background);
//    }
//
//    /**
//     * 创建二维码位图 (支持自定义配置和自定义样式)
//     *
//     * @param content          字符串内容
//     * @param width            位图宽度,要求>=0(单位:px)
//     * @param height           位图高度,要求>=0(单位:px)
//     * @param character_set    字符集/字符转码格式。传null时,zxing源码默认使用 "ISO-8859-1"
//     * @param error_correction 容错级别。传null时,zxing源码默认使用 "L"
//     * @param margin           空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"。
//     * @param color_black      黑色色块的自定义颜色值
//     * @param color_white      白色色块的自定义颜色值
//     * @return Bitmap
//     */
//    private static Bitmap createQRCodeBitmap(String content, int width, int height,
//                                             String character_set, String error_correction, String margin,
//                                             int color_black, int color_white) {
//
//        /* 1.参数合法性判断 */
//        if (TextUtils.isEmpty(content)) { // 字符串内容判空
//            return null;
//        }
//
//        if (width < 0 || height < 0) { // 宽和高都需要>=0
//            return null;
//        }
//
//        try {
//            /* 2.设置二维码相关配置,生成BitMatrix(位矩阵)对象 */
//            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
//
//            if (!TextUtils.isEmpty(character_set)) {
//                hints.put(EncodeHintType.CHARACTER_SET, character_set); // 字符转码格式设置
//            }
//
//            if (!TextUtils.isEmpty(error_correction)) {
//                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction); // 容错级别设置
//            }
//
//            if (!TextUtils.isEmpty(margin)) {
//                hints.put(EncodeHintType.MARGIN, margin); // 空白边距设置
//            }
//            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
//
//            /* 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
//            int[] pixels = new int[width * height];
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    if (bitMatrix.get(x, y)) {
//                        pixels[y * width + x] = color_black; // 黑色色块像素设置
//                    } else {
//                        pixels[y * width + x] = color_white; // 白色色块像素设置
//                    }
//                }
//            }
//
//            /* 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象 */
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//            return bitmap;
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    static String decodeQRCodeBitmap(Bitmap qrCode) {
//
//        try {
//
//            int width = qrCode.getWidth();
//            int height = qrCode.getHeight();
//
//            int[] qrCodeImgData = new int[width * height];
//            qrCode.getPixels(qrCodeImgData, 0, width, 0, 0, width, height);
//
//            RGBLuminanceSource rgbLuminanceSource =
//                    new RGBLuminanceSource(width, height, qrCodeImgData);
//
//            Result qrCodeReaderResult =
//                    new QRCodeReader().decode(
//                            new BinaryBitmap(
//                                    new HybridBinarizer(rgbLuminanceSource)
//                            )
//                    );
//
//            return qrCodeReaderResult.getText();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return "";
//    }
}

