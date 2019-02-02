package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.FileNotFoundException;
import java.util.Date;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.LauncherShortcutUtils.createShortCut;
import static cf.playhi.freezeyou.ToastUtils.showToast;

public class LauncherShortcutConfirmAndGenerateActivity extends Activity {

    private boolean requestFromLauncher;
    private Class<?> targetSelfCls;
    private String id = Long.toString(new Date().getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        ThemeUtils.processActionBar(getActionBar());

        setContentView(R.layout.lscaga_main);

        Intent intent = getIntent();

        requestFromLauncher = Intent.ACTION_CREATE_SHORTCUT.equals(intent.getAction());

        targetSelfCls =
                requestFromLauncher
                        ?
                        Freeze.class
                        :
                        ((Class<?>) intent.getSerializableExtra("class"));

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 11:
                if (resultCode == RESULT_OK) {
                    setIntent(data);
                    init();
                }
                break;
            case 21:
                if (resultCode == RESULT_OK) {
                    Uri fullPhotoUri = data.getData();
                    if (fullPhotoUri != null) {
                        ContentResolver contentResolver = getContentResolver();
                        try {
                            Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(fullPhotoUri));
                            ImageButton lscaga_icon_imageButton = findViewById(R.id.lscaga_icon_imageButton);
                            lscaga_icon_imageButton.setImageDrawable(new BitmapDrawable(bitmap));
                        } catch (FileNotFoundException e) {
                            showToast(this, R.string.failed);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private String checkAndAvoidNull(String s) {
        return s == null ? "" : s;
    }

    private void init() {
        Intent intent = getIntent();

        String name = checkAndAvoidNull(requestFromLauncher ? getString(R.string.name) : intent.getStringExtra("name"));
        id = checkAndAvoidNull(requestFromLauncher ? "" : intent.getStringExtra("id"));//若桌面（类小部件快捷方式）发起，id 值无需考虑，无需使用。
        String pkgName = checkAndAvoidNull(requestFromLauncher ? getString(R.string.plsSelect) : intent.getStringExtra("pkgName"));

        Button lscaga_package_button = findViewById(R.id.lscaga_package_button);
        Button lscaga_target_button = findViewById(R.id.lscaga_target_button);
        Button lscaga_generate_button = findViewById(R.id.lscaga_generate_button);
        EditText lscaga_package_editText = findViewById(R.id.lscaga_package_editText);
        EditText lscaga_displayName_editText = findViewById(R.id.lscaga_displayName_editText);
        EditText lscaga_target_editText = findViewById(R.id.lscaga_target_editText);
        ImageButton lscaga_icon_imageButton = findViewById(R.id.lscaga_icon_imageButton);


        processSelectedPackageEditText(pkgName, lscaga_package_editText);

        processSelectPackageButton(lscaga_package_button);

        processDisplayNameEditText(name, lscaga_displayName_editText);

        processSelectedTargetEditText(lscaga_target_editText);

        processChangeIconImageButton(pkgName, lscaga_icon_imageButton);

        processSelectTargetButton(lscaga_target_button);

        processGenerateButton(lscaga_generate_button, lscaga_package_editText, lscaga_displayName_editText, lscaga_icon_imageButton);

    }

    private void processDisplayNameEditText(String name, EditText lscaga_displayName_editText) {
        lscaga_displayName_editText.setText(name);
    }

    private void processSelectedTargetEditText(EditText lscaga_target_editText) {
        lscaga_target_editText.setText(R.string.launch);
    }

    private void processSelectedPackageEditText(String pkgName, EditText lscaga_package_editText) {
        lscaga_package_editText.setText(pkgName);
    }

    private void processChangeIconImageButton(String pkgName, ImageButton lscaga_icon_imageButton) {
        if (pkgName != null) {
            Drawable icon;
            switch (pkgName) {
                case "cf.playhi.freezeyou.extra.fuf":
                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                    break;
                case "cf.playhi.freezeyou.extra.oklock":
                    icon = getResources().getDrawable(R.drawable.screenlock);
                    break;
                case "OF":
                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                    break;
                case "UF":
                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                    break;
                case "OO":
                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                    break;
                case "OOU":
                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                    break;
                case "FOQ":
                    icon = getResources().getDrawable(R.mipmap.ic_launcher_round);
                    break;
                default:
                    icon = getApplicationIcon(this, pkgName, null, false);
                    break;
            }

            lscaga_icon_imageButton.setImageDrawable(icon);
            lscaga_icon_imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(
                            new Intent(LauncherShortcutConfirmAndGenerateActivity.this, SelectShortcutIconActivity.class),
                            21);
                }
            });
        }
    }

    private void processSelectPackageButton(Button lscaga_package_button) {
        lscaga_package_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(LauncherShortcutConfirmAndGenerateActivity.this, FUFLauncherShortcutCreator.class)
                                .putExtra("returnPkgName", true),
                        11);
            }
        });
    }

    private void processSelectTargetButton(Button lscaga_target_button) {
        lscaga_target_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void processGenerateButton(Button lscaga_generate_button, final EditText lscaga_package_editText, final EditText lscaga_displayName_editText, final ImageButton lscaga_icon_imageButton) {

        lscaga_generate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable icon = lscaga_icon_imageButton.getDrawable();
                Context context = getApplicationContext();
                String pkgName = lscaga_package_editText.getText().toString();
                String title = lscaga_displayName_editText.getText().toString();
                if (requestFromLauncher) {
                    Intent shortcutIntent = new Intent(LauncherShortcutConfirmAndGenerateActivity.this, Freeze.class);
                    shortcutIntent.putExtra("pkgName", pkgName);
                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(icon));
                    setResult(RESULT_OK, intent);
                } else {
                    createShortCut(
                            title,
                            pkgName,
                            icon,
                            targetSelfCls,
                            id,
                            context
                    );
                }
            }
        });

    }

}
