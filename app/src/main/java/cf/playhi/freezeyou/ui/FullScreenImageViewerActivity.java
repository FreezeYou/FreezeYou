package cf.playhi.freezeyou.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;

import cf.playhi.freezeyou.R;
import cf.playhi.freezeyou.utils.ThemeUtils;
import cf.playhi.freezeyou.app.FreezeYouBaseActivity;

public class FullScreenImageViewerActivity extends FreezeYouBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fsiva_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        if (intent != null) {
            ImageView fsiva_main_imageView = findViewById(R.id.fsiva_main_imageView);
            fsiva_main_imageView.setImageBitmap(
                    BitmapFactory.decodeFile(intent.getStringExtra("imgPath"))
            );
            fsiva_main_imageView.setOnClickListener(v -> finish());
        } else {
            finish();
        }

    }

}
