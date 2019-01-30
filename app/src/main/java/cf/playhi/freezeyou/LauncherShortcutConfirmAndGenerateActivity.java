package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.LauncherShortcutUtils.createShortCut;

public class LauncherShortcutConfirmAndGenerateActivity extends Activity {

    Class<?> targetSelfCls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        ThemeUtils.processActionBar(getActionBar());
        setContentView(R.layout.lscaga_main);
        targetSelfCls = ((SerializableClass) getIntent().getSerializableExtra("class")).getStoredClass();
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
            default:
                break;
        }
    }

    private void init() {
        Button lscaga_package_button = findViewById(R.id.lscaga_package_button);
        Button lscaga_target_button = findViewById(R.id.lscaga_target_button);
        Button lscaga_generate_button = findViewById(R.id.lscaga_generate_button);
        EditText lscaga_package_editText = findViewById(R.id.lscaga_package_editText);
        EditText lscaga_displayName_editText = findViewById(R.id.lscaga_displayName_editText);
        EditText lscaga_target_editText = findViewById(R.id.lscaga_target_editText);
        ImageButton lscaga_icon_imageButton = findViewById(R.id.lscaga_icon_imageButton);

        String pkgName = getIntent().getStringExtra("pkgName");
        if (pkgName == null)
            pkgName = "";

        processSelectedPackageEditText(pkgName, lscaga_package_editText);
        processSelectPackageButton(lscaga_package_button);

        processDisplayNameEditText(pkgName, lscaga_displayName_editText);

        processSelectedTargetEditText(pkgName, lscaga_target_editText);

        processChangeIconImageButton(pkgName, lscaga_icon_imageButton);

        processSelectTargetButton(lscaga_target_button);

        processGenerateButton(lscaga_generate_button, lscaga_package_editText, lscaga_displayName_editText, lscaga_icon_imageButton);

    }

    private void processDisplayNameEditText(String pkgName, EditText lscaga_displayName_editText) {
        lscaga_displayName_editText.setText(
                getApplicationLabel(this, null, null, pkgName));
    }

    private void processSelectedTargetEditText(String pkgName, EditText lscaga_target_editText) {
        lscaga_target_editText.setText(R.string.launch);
    }

    private void processSelectedPackageEditText(String pkgName, EditText lscaga_package_editText) {
        lscaga_package_editText.setText(pkgName);
    }

    private void processChangeIconImageButton(String pkgName, ImageButton lscaga_icon_imageButton) {
        lscaga_icon_imageButton.setBackgroundDrawable(
                getApplicationIcon(this, pkgName, null, false));
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

    private void processGenerateButton(Button lscaga_generate_button, EditText lscaga_package_editText, EditText lscaga_displayName_editText, ImageButton lscaga_icon_imageButton) {
        final Context context = getApplicationContext();
        final String pkgName = lscaga_package_editText.getText().toString();
        final String id = "FreezeYou! " + pkgName;
        final String title = lscaga_displayName_editText.getText().toString();
        final Drawable icon = lscaga_icon_imageButton.getBackground();
        lscaga_generate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createShortCut(
                        title,
                        pkgName,
                        icon,
                        targetSelfCls,
                        id,
                        context
                );
            }
        });
    }

}
