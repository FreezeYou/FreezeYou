package cf.playhi.freezeyou.ui

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.fragment.FirstTimeSetupFragment
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme

class FirstTimeSetupActivity : FreezeYouBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        actionBar?.hide()
        setContent {
            FreezeYouTheme {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.quickSetup),
                        fontSize = 34.sp,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
                    )
                    AndroidView(
                        factory = { context ->
                            FragmentContainerView(context).apply {
                                id = R.id.first_time_setup_container
                                addOnAttachStateChangeListener(
                                    object : View.OnAttachStateChangeListener {
                                        override fun onViewAttachedToWindow(view: View) {
                                            attachSetupFragment()
                                        }

                                        override fun onViewDetachedFromWindow(view: View) = Unit
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    Button(onClick = { finish() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.finish))
                    }
                }
            }
        }
    }

    private fun attachSetupFragment() {
        if (supportFragmentManager.isStateSaved) return
        if (supportFragmentManager.findFragmentByTag(SETUP_FRAGMENT_TAG) != null ||
            supportFragmentManager.findFragmentById(R.id.first_time_setup_container) != null
        ) {
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.first_time_setup_container,
                FirstTimeSetupFragment(),
                SETUP_FRAGMENT_TAG
            )
            .commit()
    }

    private companion object {
        const val SETUP_FRAGMENT_TAG = "first-time-setup"
    }
}
