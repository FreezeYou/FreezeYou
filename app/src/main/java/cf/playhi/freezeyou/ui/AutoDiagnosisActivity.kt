package cf.playhi.freezeyou.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import cf.playhi.freezeyou.viewmodel.AutoDiagnosisViewModel
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.utils.ThemeUtils.processActionBar
import cf.playhi.freezeyou.utils.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.ui.compose.FreezeYouTheme
import cf.playhi.freezeyou.ui.compose.ResourceDrawableImage
import cf.playhi.freezeyou.utils.AccessibilityUtils.openAccessibilitySettings
import cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite
import cf.playhi.freezeyou.utils.NotificationUtils.startAppNotificationSettingsSystemActivity
import cf.playhi.freezeyou.utils.VersionUtils.checkUpdate

class AutoDiagnosisActivity : FreezeYouBaseActivity() {
    private val viewModel: AutoDiagnosisViewModel by viewModels()
    private var loadingProgress by mutableIntStateOf(-1)
    private var problems by mutableStateOf<List<Map<String, Any>>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        processActionBar(supportActionBar)
        setContent {
            FreezeYouTheme {
                Column(Modifier.fillMaxSize()) {
                    when {
                        loadingProgress < 0 -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        loadingProgress < 100 -> LinearProgressIndicator(
                            progress = { loadingProgress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(problems, key = { it["id"].toString() }) { problem ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { handleProblem(problem["id"] as? String) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f).padding(2.dp)) {
                                    Text(
                                        problem["title"].toString(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(problem["sTitle"].toString())
                                }
                                ResourceDrawableImage(
                                    resourceId = problem["status"] as Int,
                                    contentDescription = stringResource(R.string.status),
                                    modifier = Modifier.size(40.dp).padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        viewModel.getLoadingProgress().observe(this) {
            loadingProgress = it
            if (it < 0) problems = emptyList()
            if (it == 100) {
                problems = viewModel.getProblemsList().value.orEmpty().toList()
            }
        }
    }

    private fun handleProblem(id: String?) {
        when (id) {
                        "-30" -> checkUpdate(this@AutoDiagnosisActivity)
                        "1" -> openAccessibilitySettings(this@AutoDiagnosisActivity)
                        "2" -> {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        "4" -> {
                            val intent =
                                if ((getSystemService(POWER_SERVICE) as PowerManager)
                                        .isIgnoringBatteryOptimizations(packageName)
                                ) {
                                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                } else {
                                    Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:$packageName")
                                    )
                                }

                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        "6" -> if (Build.VERSION.SDK_INT >= 33) {
                            ActivityCompat.requestPermissions(
                                this@AutoDiagnosisActivity,
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                0
                            )
                        } else {
                            startAppNotificationSettingsSystemActivity(
                                this@AutoDiagnosisActivity,
                                "cf.play" + "hi.free" + "zeyou",
                                applicationInfo.uid
                            )
                        }
                        "7" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                            && ActivityCompat
                                .checkSelfPermission(
                                    applicationContext,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@AutoDiagnosisActivity,
                                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                0
                            )
                        }
                        "8" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            intent.data = Uri.parse("package:$packageName")
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDiagnosisData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.autodiagnosis_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_autoDiagnosis_refresh -> {
                viewModel.refreshDiagnosisData()
                true
            }
            R.id.menu_autoDiagnosis_help -> {
                requestOpenWebSite(
                    this@AutoDiagnosisActivity,
                    "https://www.zidon.net/${getString(R.string.correspondingAndAvailableWebsiteUrlLanguageCode)}/faq/",
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
