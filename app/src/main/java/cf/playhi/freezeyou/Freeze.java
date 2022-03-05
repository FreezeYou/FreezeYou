package cf.playhi.freezeyou;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.showInRecents;
import static cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.needConfirmWhenFreezeUseShortcutAutoFUF;
import static cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.openImmediatelyAfterUnfreezeUseShortcutAutoFUF;
import static cf.playhi.freezeyou.storage.key.DefaultSharedPreferenceStorageBooleanKeys.shortcutAutoFUF;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.utils.FUFUtils.checkMRootFrozen;
import static cf.playhi.freezeyou.utils.FUFUtils.checkRootFrozen;
import static cf.playhi.freezeyou.utils.FUFUtils.processFreezeAction;
import static cf.playhi.freezeyou.utils.FUFUtils.processUnfreezeAction;
import static cf.playhi.freezeyou.utils.FUFUtils.realGetFrozenStatus;
import static cf.playhi.freezeyou.utils.Support.shortcutMakeDialog;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
public class Freeze extends FreezeYouBaseActivity {
    private Intent mStartedIntent;
    private String mPkgName;
    private boolean mAutoRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, true);
        super.onCreate(savedInstanceState);
        loadStartedIntentAndPkgName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLocked()) go();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isLocked()) finish();
    }

    private void loadStartedIntentAndPkgName() {
        mStartedIntent = getIntent();

        if ("freezeyou".equals(mStartedIntent.getScheme())) {
            Uri dataUri = mStartedIntent.getData();
            mPkgName = (dataUri == null) ? null : dataUri.getQueryParameter("pkgName");
            mAutoRun = false;
        } else {
            mPkgName = mStartedIntent.getStringExtra("pkgName");
            mAutoRun = mStartedIntent.getBooleanExtra("auto", true);
        }

        setUnlockLogoPkgName(mPkgName);
    }

    private void go() {
        if (mStartedIntent != null) {
            String target = mStartedIntent.getStringExtra("target");
            String tasks = mStartedIntent.getStringExtra("tasks");

            if (mPkgName == null) {
                showToast(getApplicationContext(), R.string.invalidArguments);
                Freeze.this.finish();
            } else if ("".equals(mPkgName)) {
                showToast(getApplicationContext(), R.string.invalidArguments);
                Freeze.this.finish();
            }

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if (mAutoRun && sp.getBoolean(shortcutAutoFUF.name(), shortcutAutoFUF.defaultValue())) {
                if (realGetFrozenStatus(this, mPkgName, getPackageManager())) {
                    processUnfreezeAction(
                            this, mPkgName, target, tasks, true,
                            sp.getBoolean(
                                    openImmediatelyAfterUnfreezeUseShortcutAutoFUF.name(),
                                    openImmediatelyAfterUnfreezeUseShortcutAutoFUF.defaultValue()
                            ),
                            this, true);
                } else {
                    if (sp.getBoolean(
                            needConfirmWhenFreezeUseShortcutAutoFUF.name(),
                            needConfirmWhenFreezeUseShortcutAutoFUF.defaultValue())
                    ) {
                        processDialog(mPkgName, target, tasks, false, 2);
                    } else {
                        processFreezeAction(this, mPkgName, target, tasks,
                                true, this, true);
                    }
                }
            } else if ((!checkRootFrozen(Freeze.this, mPkgName, null))
                    && (!checkMRootFrozen(Freeze.this, mPkgName))) {
                processDialog(mPkgName, target, tasks, mAutoRun, 2);
            } else {
                processDialog(mPkgName, target, tasks, mAutoRun, 1);
            }
            if (Build.VERSION.SDK_INT >= 21) {
                setTaskDescription(
                        new ActivityManager.TaskDescription(
                                getApplicationLabel(
                                        this,
                                        null,
                                        null,
                                        mPkgName)
                                        + " - "
                                        + getString(R.string.app_name),
                                getBitmapFromDrawable(
                                        getApplicationIcon(
                                                this,
                                                mPkgName,
                                                getApplicationInfoFromPkgName(
                                                        mPkgName,
                                                        this
                                                ),
                                                false)
                                )
                        )
                );
            }
        }
    }

    private void processDialog(String pkgName, String target, String tasks, boolean auto, int ot) {
        shortcutMakeDialog(
                Freeze.this,
                getApplicationLabel(Freeze.this, null, null, pkgName),
                getString(R.string.chooseDetailAction),
                Freeze.this,
                getApplicationInfoFromPkgName(pkgName, this),
                pkgName,
                target,
                tasks,
                ot,
                auto,
                true);
    }

    @Override
    public void finish() {
        if (Build.VERSION.SDK_INT >= 21 && !showInRecents.getValue(null)) {
            finishAndRemoveTask();
        }
        super.finish();
    }
}
