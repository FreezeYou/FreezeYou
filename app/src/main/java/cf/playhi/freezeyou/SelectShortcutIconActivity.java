package cf.playhi.freezeyou;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.utils.ToastUtils.showToast;

public class SelectShortcutIconActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.processSetTheme(this);
        super.onCreate(savedInstanceState);
        ThemeUtils.processActionBar(getActionBar());
        setContentView(R.layout.ssia_main);
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 21 && data != null) {
            Uri fullPhotoUri = data.getData();
            if (fullPhotoUri != null) {
                ContentResolver contentResolver = getContentResolver();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(fullPhotoUri));
                    if (bitmap.getByteCount() > getBitmapFromDrawable(getResources().getDrawable(R.mipmap.ic_launcher_new_round)).getByteCount() * 5) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        Matrix matrix = new Matrix();
                        float scaleWidth = ((float) 192) / width;
                        float scaleHeight = ((float) 192) / height;
                        matrix.postScale(scaleWidth, scaleHeight);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                    }
                    setResult(
                            RESULT_OK,
                            new Intent().putExtra("Icon", bitmap)
                    );
                    finish();
                } catch (FileNotFoundException e) {
                    showToast(this, R.string.failed);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        final ArrayList<HashMap<String, Drawable>> icons = new ArrayList<>();

        //选择更多（扔第一个，免得被淹没看不到）
        addToIconsArrayList(icons, getResources().getDrawable(R.drawable.grid_add));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.mipmap.ic_launcher_new_round));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.mipmap.ic_launcher_round));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.mipmap.ic_launcher));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.drawable.screenlock));
        //自带
        addToIconsArrayList(icons, getResources().getDrawable(R.drawable.ic_notification));

        List<ApplicationInfo> applicationInfoS = getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        if (applicationInfoS != null) {
            for (ApplicationInfo applicationInfo : applicationInfoS) {
                if (applicationInfo != null) {
                    addToIconsArrayList(
                            icons,
                            getApplicationIcon(
                                    SelectShortcutIconActivity.this,
                                    applicationInfo.packageName,
                                    applicationInfo,
                                    false));
                }
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, icons,
                R.layout.ssia_main_grid_item, new String[]{"Icon"},
                new int[]{R.id.ssia_mgi_imageView});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ((ImageView) view).setImageDrawable((Drawable) data);
                    return true;
                } else
                    return false;
            }
        });

        GridView gridView = findViewById(R.id.ssia_main_gridView);

        gridView.setAdapter(simpleAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 21);
                    }
                } else {
                    setResult(
                            RESULT_OK,
                            new Intent()
                                    .putExtra(
                                            "Icon",
                                            getBitmapFromDrawable(icons.get(position).get("Icon"))));
                    finish();
                }
            }
        });

    }

    private void addToIconsArrayList(ArrayList<HashMap<String, Drawable>> icons, Drawable drawable) {
        HashMap<String, Drawable> map = new HashMap<>();
        map.put("Icon", drawable);
        icons.add(map);
    }
}
