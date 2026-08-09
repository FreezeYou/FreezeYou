package cf.playhi.freezeyou.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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

class SelectShortcutIconActivity : FreezeYouBaseActivity() {
    private var icons by mutableStateOf<List<Drawable?>>(emptyList())
    private var loading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.processSetTheme(this)
        super.onCreate(savedInstanceState)
        ThemeUtils.processActionBar(supportActionBar)
        setContent {
            FreezeYouTheme {
                val iconDescription = androidx.compose.ui.res.stringResource(R.string.icon)
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyVerticalGrid(columns = GridCells.Adaptive(72.dp)) {
                        itemsIndexed(icons) { position, drawable ->
                            DrawableImage(
                                drawable,
                                iconDescription,
                                Modifier.size(72.dp).clickable {
                                    selectIcon(position, drawable)
                                }.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
        loadIcons()
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
                    var bitmap = BitmapFactory.decodeStream(
                        contentResolver.openInputStream(fullPhotoUri)
                    ) ?: run {
                        showToast(this, R.string.failed)
                        return
                    }
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
                } catch (e: Exception) {
                    showToast(this, R.string.failed)
                }
            }
        }
    }

    private fun loadIcons() {
        Thread {
            val loadedIcons = ArrayList<Drawable?>()

            //选择更多（扔第一个，免得被淹没看不到）
            addToIconsArrayList(loadedIcons, ContextCompat.getDrawable(this, R.drawable.grid_add))
        //自带
            addToIconsArrayList(loadedIcons, ContextCompat.getDrawable(this, R.mipmap.ic_launcher_new_round))
        //自带
            addToIconsArrayList(loadedIcons, ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round))
        //自带
            addToIconsArrayList(loadedIcons, ContextCompat.getDrawable(this, R.mipmap.ic_launcher))
        //自带
            addToIconsArrayList(loadedIcons, ContextCompat.getDrawable(this, R.drawable.screenlock))
        //自带
            addToIconsArrayList(loadedIcons, ContextCompat.getDrawable(this, R.drawable.ic_notification))
            val matchUninstalled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PackageManager.MATCH_UNINSTALLED_PACKAGES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_UNINSTALLED_PACKAGES
            }
            val applicationInfoS = packageManager.getInstalledApplications(matchUninstalled)
            for (applicationInfo in applicationInfoS) {
                addToIconsArrayList(
                    loadedIcons,
                    getApplicationIcon(
                        applicationContext,
                        applicationInfo.packageName,
                        applicationInfo,
                        false
                    )
                )
            }
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    icons = loadedIcons
                    loading = false
                }
            }
        }.start()
    }

    private fun selectIcon(position: Int, drawable: Drawable?) {
        if (position == 0) {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
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
    }

    private fun addToIconsArrayList(
        icons: MutableList<Drawable?>,
        drawable: Drawable?
    ) {
        icons.add(drawable)
    }
}
