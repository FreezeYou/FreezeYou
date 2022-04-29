package cf.playhi.freezeyou;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.lifecycle.ViewModelProvider;

import cf.playhi.freezeyou.app.FreezeYouBaseActivity;
import cf.playhi.freezeyou.fuf.FUFSinglePackage;
import cf.playhi.freezeyou.viewmodel.DialogData;
import cf.playhi.freezeyou.viewmodel.FreezeActivityViewModel;
import cf.playhi.freezeyou.viewmodel.PlayAnimatorData;

import static cf.playhi.freezeyou.fuf.FUFSinglePackage.ACTION_MODE_UNFREEZE;
import static cf.playhi.freezeyou.storage.key.DefaultMultiProcessMMKVStorageBooleanKeys.showInRecents;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.utils.ApplicationInfoUtils.getApplicationInfoFromPkgName;
import static cf.playhi.freezeyou.utils.ApplicationLabelUtils.getApplicationLabel;
import static cf.playhi.freezeyou.utils.FUFUtils.getFUFRelatedToastString;
import static cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

// Needs to be retained for compatibility
// with old FreezeYou structures and settings.
public class Freeze extends FreezeYouBaseActivity {
    private FreezeActivityViewModel viewModel;
    private ImageView applicationIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        processSetTheme(this, true);
        super.onCreate(savedInstanceState);
        initApplicationIconImageView();
        viewModel = new ViewModelProvider(this).get(FreezeActivityViewModel.class);
        viewModel.getPkgName().observe(this, pkgName -> {
            setUnlockLogoPkgName(pkgName);
            updateTaskDescription(pkgName);
        });
        viewModel.getToastStringId().observe(this, id -> showToast(this, id));
        viewModel.getExecuteResult().observe(this, result ->
                showToast(
                        this,
                        getFUFRelatedToastString(
                                this,
                                result.getFreezeYouFUFSinglePackage().getActionMode() == ACTION_MODE_UNFREEZE
                                        ?
                                        result.getFreezeYouFUFSinglePackage()
                                                .checkAndStartTaskAndTargetAndActivity(result.getResult())
                                        :
                                        result.getResult()
                        )
                )
        );
        viewModel.getFinishMe().observe(this, finishMe -> {
            if (finishMe) finish();
        });
        viewModel.getPlayAnimator().observe(this, this::onPlayAnimator);
        viewModel.getShowDialog().observe(this, this::buildAndShowFUFDialog);
        viewModel.loadStartedIntentAndPkgName(getIntent());
    }

    private void onPlayAnimator(PlayAnimatorData playAnimatorData) {
        applicationIconImageView.setImageDrawable(
                getApplicationIcon(
                        this,
                        playAnimatorData.getPkgName(),
                        getApplicationInfoFromPkgName(playAnimatorData.getPkgName(), this),
                        false
                )
        );
        if (playAnimatorData.getFreezing()) {
            onFreezeStart();
        } else {
            onUnfreezeStart();
        }
    }

    private void updateTaskDescription(String pkgName) {
        if (Build.VERSION.SDK_INT >= 21) {
            setTaskDescription(
                    new ActivityManager.TaskDescription(
                            getApplicationLabel(
                                    this,
                                    null,
                                    null,
                                    pkgName
                            )
                                    + " - "
                                    + getString(R.string.app_name),
                            getBitmapFromDrawable(
                                    getApplicationIcon(
                                            this,
                                            pkgName,
                                            getApplicationInfoFromPkgName(
                                                    pkgName,
                                                    this
                                            ),
                                            false
                                    )
                            )
                    )
            );
        }
    }

    private void initApplicationIconImageView() {
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(480, 480);
        imageView.setLayoutParams(layoutParams);
        ((FrameLayout) findViewById(android.R.id.content)).addView(imageView);
        applicationIconImageView = imageView;
    }

    private void onUnfreezeStart() {
        long animDuration = viewModel.getAverageTimeCosts();
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(
                applicationIconImageView, "alpha", 0.2f, 1f);
        fadeAnim.setDuration(animDuration);
        fadeAnim.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(
                applicationIconImageView, View.SCALE_X, 0.6f, 1f);
        scaleXAnim.setDuration(animDuration);
        scaleXAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(
                applicationIconImageView, View.SCALE_Y, 0.6f, 1f);
        scaleYAnim.setDuration(animDuration);
        scaleYAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator greyAnim = ValueAnimator.ofFloat(0f, 1f);
            greyAnim.addUpdateListener(animation -> {
                float animatedValue = (float) animation.getAnimatedValue();
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(animatedValue);
                applicationIconImageView.setColorFilter(new ColorMatrixColorFilter(matrix));
            });
            greyAnim.setDuration(animDuration);
            greyAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.play(greyAnim).with(fadeAnim).with(scaleXAnim).with(scaleYAnim);
        } else {
            animatorSet.play(fadeAnim).with(scaleXAnim).with(scaleYAnim);
        }
        animatorSet.start();
    }

    private void onFreezeStart() {
        long animDuration = viewModel.getAverageTimeCosts();
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(
                applicationIconImageView, "alpha", 1f, 0f);
        fadeAnim.setDuration(animDuration);
        fadeAnim.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(
                applicationIconImageView, View.SCALE_X, 1f, 0.6f);
        scaleXAnim.setDuration(animDuration);
        scaleXAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(
                applicationIconImageView, View.SCALE_Y, 1f, 0.6f);
        scaleYAnim.setDuration(animDuration);
        scaleYAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator greyAnim = ValueAnimator.ofFloat(1f, 0f);
            greyAnim.addUpdateListener(animation -> {
                float animatedValue = (float) animation.getAnimatedValue();
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(animatedValue);
                applicationIconImageView.setColorFilter(new ColorMatrixColorFilter(matrix));
            });
            greyAnim.setDuration(animDuration);
            greyAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.play(greyAnim).with(fadeAnim).with(scaleXAnim).with(scaleYAnim);
        } else {
            animatorSet.play(fadeAnim).with(scaleXAnim).with(scaleYAnim);
        }
        animatorSet.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLocked()) viewModel.go();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isLocked()) finish();
    }

    @Override
    public void finish() {
        if (Build.VERSION.SDK_INT >= 21 && !showInRecents.getValue(null)) {
            finishAndRemoveTask();
        }
        super.finish();
    }

    private void buildAndShowFUFDialog(DialogData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(
                        getApplicationIcon(
                                this,
                                data.getPkgName(),
                                getApplicationInfoFromPkgName(data.getPkgName(), this),
                                true
                        )
                )
                .setMessage(getString(R.string.chooseDetailAction))
                .setTitle(
                        getApplicationLabel(
                                this,
                                null,
                                null,
                                data.getPkgName()
                        )
                )
                .setNeutralButton(R.string.cancel, (dialogInterface, i) -> finish())
                .setOnCancelListener(dialogInterface -> finish());
        if (data.getFrozen()) {
            builder.setPositiveButton(R.string.unfreeze, (dialogInterface, i) ->
                    viewModel.fufAction(
                            data.getPkgName(), data.getTarget(), data.getTasks(),
                            false, true
                    )
            );
        } else {
            builder.setPositiveButton(R.string.launch, (dialogInterface, i) -> {
                int result = viewModel.checkAndStartTaskAndTargetAndActivityOfUnfrozenApp(data);
                if (result != FUFSinglePackage.ERROR_NO_ERROR_SUCCESS
                        && result != FUFSinglePackage.ERROR_NO_ERROR_CAUGHT_UNKNOWN_RESULT) {
                    showToast(this, getFUFRelatedToastString(this, result));
                }
                finish();
            });
            builder.setNegativeButton(R.string.freeze, (dialogInterface, i) ->
                    viewModel.fufAction(
                            data.getPkgName(), data.getTarget(), data.getTasks(),
                            false, false
                    )
            );
        }
        builder.show();
    }
}
