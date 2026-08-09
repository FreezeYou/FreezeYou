package cf.playhi.freezeyou.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getApplicationIcon
import cf.playhi.freezeyou.utils.ApplicationIconUtils.getBitmapFromDrawable
import cf.playhi.freezeyou.utils.ThemeUtils
import cf.playhi.freezeyou.utils.ToastUtils.showToast
import cf.playhi.freezeyou.ui.compose.DrawableImage
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import java.io.FileNotFoundException

class SelectShortcutIconActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.processSetTheme(this)
        super.onCreate(savedInstanceState)
        ThemeUtils.processActionBar(supportActionBar)
        init()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 21 && data != null) {
            val fullPhotoUri = data.data
            if (fullPhotoUri != null) {
                val contentResolver = contentResolver
                try {
                    var bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(fullPhotoUri))
                    if (bitmap.byteCount > getBitmapFromDrawable(
                            ContextCompat.getDrawable(this, R.mipmap.ic_launcher_new_round)
                        ).byteCount * 5
                    ) {
                        val width = bitmap.width
                        val height = bitmap.height
                        val matrix = Matrix()
                        val scaleWidth = 192.toFloat() / width
                        val scaleHeight = 192.toFloat() / height
                        matrix.postScale(scaleWidth, scaleHeight)
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
                    }
                    setResult(
                        RESULT_OK,
                        Intent().putExtra("Icon", bitmap)
                    )
                    finish()
                } catch (e: FileNotFoundException) {
                    showToast(this, R.string.failed)
                }
            }
        }
    }

    private fun init() {
        val icons = ArrayList<Drawable?>()

        //选择更多（扔第一个，免得被淹没看不到）
        addToIconsArrayList(icons, ContextCompat.getDrawable(this, R.drawable.grid_add))
        //自带
        addToIconsArrayList(icons, ContextCompat.getDrawable(this, R.mipmap.ic_launcher_new_round))
        //自带
        addToIconsArrayList(icons, ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round))
        //自带
        addToIconsArrayList(icons, ContextCompat.getDrawable(this, R.mipmap.ic_launcher))
        //自带
        addToIconsArrayList(icons, ContextCompat.getDrawable(this, R.drawable.screenlock))
        //自带
        addToIconsArrayList(icons, ContextCompat.getDrawable(this, R.drawable.ic_notification))
        val applicationInfoS =
            packageManager.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        for (applicationInfo in applicationInfoS) {
            addToIconsArrayList(
                icons,
                getApplicationIcon(
                    this@SelectShortcutIconActivity,
                    applicationInfo.packageName,
                    applicationInfo,
                    false
                )
            )
        }
        setContent {
            FreezeYouTheme {
                LazyVerticalGrid(columns = GridCells.Adaptive(72.dp)) {
                    itemsIndexed(icons) { position, drawable ->
                        DrawableImage(
                            drawable,
                            null,
                            Modifier.size(72.dp).clickable {
                                if (position == 0) {
                                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "image/*"
                                    }
                                    if (intent.resolveActivity(packageManager) != null) {
                                        @Suppress("DEPRECATION")
                                        startActivityForResult(intent, 21)
                                    }
                                } else {
                                    setResult(
                                        RESULT_OK,
                                        Intent().putExtra("Icon", getBitmapFromDrawable(drawable))
                                    )
                                    finish()
                                }
                            }.padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    private fun addToIconsArrayList(
        icons: MutableList<Drawable?>,
        drawable: Drawable?
    ) {
        icons.add(drawable)
    }
}
