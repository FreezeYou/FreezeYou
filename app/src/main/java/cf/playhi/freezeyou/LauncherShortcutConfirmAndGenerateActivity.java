package cf.playhi.freezeyou;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.util.Date;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.utils.ApplicationInfoUtils;
import cf.playhi.freezeyou.utils.MoreUtils;

import static cf.playhi.freezeyou.LauncherShortcutUtils.createShortCut;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class LauncherShortcutConfirmAndGenerateActivity extends FreezeYouBaseActivity {

    private boolean requestFromLauncher;
    private Class<?> targetSelfCls;
    private Drawable finalDrawable;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lscaga_menu, menu);
        String cTheme = ThemeUtils.getUiTheme(this);
        if ("white".equals(cTheme) || "default".equals(cTheme))
            menu.findItem(R.id.lscaga_menu_help).setIcon(R.drawable.ic_action_help_outline);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.lscaga_menu_help:
                MoreUtils.requestOpenWebSite(this,
                        String.format("https://www.zidon.net/%1$s/guide/schedules.html",
                                getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 8:
                if (resultCode == RESULT_OK) {
                    EditText lscaga_target_editText = findViewById(R.id.lscaga_target_editText);
                    EditText lscaga_id_editText = findViewById(R.id.lscaga_id_editText);
                    ImageButton lscaga_icon_imageButton = findViewById(R.id.lscaga_icon_imageButton);
                    lscaga_target_editText.setText(data.getStringExtra("name"));
                    lscaga_id_editText.setText(data.getStringExtra("id"));
                    Bitmap bm = data.getParcelableExtra("icon");
                    if (bm != null) {
                        finalDrawable = new BitmapDrawable(bm);
                        lscaga_icon_imageButton.setImageDrawable(finalDrawable);
                    }
                }
                break;
            case 11:
                if (resultCode == RESULT_OK) {
                    setIntent(data);
                    init();
                }
                break;
            case 21:
                if (resultCode == RESULT_OK) {
                    finalDrawable = new BitmapDrawable((Bitmap) data.getParcelableExtra("Icon"));
                    ImageButton lscaga_icon_imageButton = findViewById(R.id.lscaga_icon_imageButton);
                    lscaga_icon_imageButton.setImageDrawable(finalDrawable);
                }
                break;
            default:
                break;
        }
    }

    private String checkAndAvoidNull(String s, String alternate) {
        return s == null ? (alternate == null ? "" : alternate) : s;
    }

    private void init() {
        Intent intent = getIntent();

        String name = checkAndAvoidNull(intent.getStringExtra("name"), getString(R.string.name));
        String id = checkAndAvoidNull(intent.getStringExtra("id"), Long.toString(new Date().getTime()));//若桌面（类小部件快捷方式）发起，id 值无需考虑，无需使用。
        String pkgName = checkAndAvoidNull(intent.getStringExtra("pkgName"), getString(R.string.plsSelect));

        Button lscaga_package_button = findViewById(R.id.lscaga_package_button);
        Button lscaga_target_button = findViewById(R.id.lscaga_target_button);
        Button lscaga_generate_button = findViewById(R.id.lscaga_generate_button);
        Button lscaga_cancel_button = findViewById(R.id.lscaga_cancel_button);
        Button lscaga_simulate_button = findViewById(R.id.lscaga_simulate_button);
        EditText lscaga_package_editText = findViewById(R.id.lscaga_package_editText);
        EditText lscaga_displayName_editText = findViewById(R.id.lscaga_displayName_editText);
        EditText lscaga_target_editText = findViewById(R.id.lscaga_target_editText);
        EditText lscaga_task_editText = findViewById(R.id.lscaga_task_editText);
        EditText lscaga_id_editText = findViewById(R.id.lscaga_id_editText);
        ImageButton lscaga_icon_imageButton = findViewById(R.id.lscaga_icon_imageButton);

        processSelectedPackageEditText(pkgName, lscaga_package_editText);

        processSelectPackageButton(lscaga_package_button);

        processDisplayNameEditText(name, lscaga_displayName_editText);

        processSelectedTargetEditText(pkgName, lscaga_target_editText);

        processChangeIconImageButton(pkgName, lscaga_icon_imageButton);

        processSelectTargetButton(pkgName, lscaga_target_button);

        processTaskEditText(lscaga_task_editText);

        processIDEditText(id, lscaga_id_editText);

        processCancelButton(lscaga_cancel_button);

        processSimulateButton(lscaga_package_editText, lscaga_target_editText, lscaga_task_editText, lscaga_simulate_button);

        processGenerateButton(lscaga_generate_button, lscaga_package_editText, lscaga_displayName_editText, lscaga_target_editText, lscaga_id_editText, lscaga_task_editText);

    }

    private void processDisplayNameEditText(String name, EditText lscaga_displayName_editText) {
        lscaga_displayName_editText.setText(name);
    }

    private void processSelectedTargetEditText(final String pkgName, EditText lscaga_target_editText) {
        lscaga_target_editText.setText(R.string.launch);
        lscaga_target_editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSelectTargetActivityForResult(pkgName);
            }
        });
    }

    private void processSelectedPackageEditText(String pkgName, EditText lscaga_package_editText) {
        lscaga_package_editText.setText(pkgName);
        lscaga_package_editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSelectPackageActivityForResult();
            }
        });
    }

    private void processTaskEditText(EditText lscaga_task_editText) {
        lscaga_task_editText.setText("");
    }

    private void processIDEditText(String id, EditText lscaga_id_editText) {
        lscaga_id_editText.setText(id);
    }

    private void processChangeIconImageButton(String pkgName, ImageButton lscaga_icon_imageButton) {
        int widthAndHeight = (int) (getResources().getDisplayMetrics().widthPixels * 0.35);
        if (widthAndHeight <= 0)
            widthAndHeight = 1;
        ViewGroup.LayoutParams layoutParams = lscaga_icon_imageButton.getLayoutParams();
        layoutParams.height = widthAndHeight;
        layoutParams.width = widthAndHeight;
        lscaga_icon_imageButton.setLayoutParams(layoutParams);
        if (pkgName != null) {
            switch (pkgName) {
                case "cf.playhi.freezeyou.extra.fuf":
                case "OF":
                case "UF":
                case "OO":
                case "OOU":
                case "FOQ":
                    finalDrawable = getResources().getDrawable(R.mipmap.ic_launcher_round);
                    break;
                case "cf.playhi.freezeyou.extra.oklock":
                    finalDrawable = getResources().getDrawable(R.drawable.screenlock);
                    break;
                default:
                    finalDrawable = getString(R.string.plsSelect).equals(pkgName)
                            ?
                            getResources().getDrawable(R.drawable.grid_add)
                            :
                            getApplicationIcon(
                                    this,
                                    pkgName,
                                    ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, this),
                                    false
                            );
                    break;
            }

            lscaga_icon_imageButton.setImageDrawable(finalDrawable);
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
                startSelectPackageActivityForResult();
            }
        });
    }

    private void processSelectTargetButton(final String pkgName, Button lscaga_target_button) {
        lscaga_target_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSelectTargetActivityForResult(pkgName);
            }
        });
    }

    private void processGenerateButton(Button lscaga_generate_button, final EditText lscaga_package_editText, final EditText lscaga_displayName_editText, final EditText lscaga_target_editText, final EditText lscaga_id_editText, final EditText lscaga_task_editText) {

        lscaga_generate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                String pkgName = lscaga_package_editText.getText().toString();
                String title = lscaga_displayName_editText.getText().toString();
                String target = lscaga_target_editText.getText().toString();
                String tasks = lscaga_task_editText.getText().toString();
                if (getString(R.string.launch).equals(target))
                    target = null;
                if (requestFromLauncher) {
                    Intent shortcutIntent = new Intent(LauncherShortcutConfirmAndGenerateActivity.this, Freeze.class);
                    shortcutIntent.putExtra("pkgName", pkgName);
                    shortcutIntent.putExtra("target", target);
                    shortcutIntent.putExtra("tasks", tasks);
                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, getBitmapFromDrawable(finalDrawable));
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    createShortCut(
                            title,
                            pkgName,
                            finalDrawable,
                            targetSelfCls,
                            lscaga_id_editText.getText().toString(),
                            context,
                            target,
                            tasks
                    );
                }
            }
        });

    }

    private void processCancelButton(Button lscaga_cancel_button) {
        lscaga_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void processSimulateButton(final EditText lscaga_package_editText, final EditText lscaga_target_editText, final EditText lscaga_task_editText, final Button lscaga_simulate_button) {
        lscaga_simulate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pkgName = lscaga_package_editText.getText().toString();
                String target = lscaga_target_editText.getText().toString();
                String tasks = lscaga_task_editText.getText().toString();
                if (getString(R.string.launch).equals(target))
                    target = null;
                startActivity(
                        new Intent(LauncherShortcutConfirmAndGenerateActivity.this, Freeze.class)
                                .putExtra("pkgName", pkgName)
                                .putExtra("target", target)
                                .putExtra("tasks", tasks)
                );
            }
        });
    }

    private void startSelectPackageActivityForResult() {
        startActivityForResult(
                new Intent(LauncherShortcutConfirmAndGenerateActivity.this, FUFLauncherShortcutCreator.class)
                        .putExtra("returnPkgName", true),
                11);
    }

    private void startSelectTargetActivityForResult(final String pkgName) {
        try {
            ActivityInfo[] activityInfoS =
                    getPackageManager().getPackageInfo(
                            pkgName, PackageManager.GET_ACTIVITIES).activities;
            startActivityForResult(
                    new Intent(
                            LauncherShortcutConfirmAndGenerateActivity.this,
                            SelectTargetActivityActivity.class)
                            .putExtra("pkgName", pkgName),
                    8);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            showToast(LauncherShortcutConfirmAndGenerateActivity.this, R.string.packageNotFound);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(LauncherShortcutConfirmAndGenerateActivity.this,
                    R.string.failed + File.separator + e.getLocalizedMessage());
        }
    }

}
