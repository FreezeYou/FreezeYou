package cf.playhi.freezeyou.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.ThemeUtils

class FullScreenImageViewerActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.processSetTheme(this)
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false)
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        val bitmap = intent?.getStringExtra("imgPath")?.let(BitmapFactory::decodeFile)
        if (bitmap == null) {
            finish()
            return
        }
        setContent {
            FreezeYouTheme {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().clickable { finish() }
                )
            }
        }
    }
}
