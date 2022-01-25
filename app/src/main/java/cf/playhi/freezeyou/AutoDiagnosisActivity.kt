package cf.playhi.freezeyou

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
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.SimpleAdapter
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import cf.playhi.freezeyou.ThemeUtils.getUiTheme
import cf.playhi.freezeyou.ThemeUtils.processActionBar
import cf.playhi.freezeyou.ThemeUtils.processSetTheme
import cf.playhi.freezeyou.app.FreezeYouBaseActivity
import cf.playhi.freezeyou.databinding.AutodiagnosisBinding
import cf.playhi.freezeyou.utils.AccessibilityUtils.openAccessibilitySettings
import cf.playhi.freezeyou.utils.MoreUtils.requestOpenWebSite
import cf.playhi.freezeyou.utils.NotificationUtils.startAppNotificationSettingsSystemActivity
import cf.playhi.freezeyou.utils.VersionUtils.checkUpdate

class AutoDiagnosisActivity : FreezeYouBaseActivity() {
    private lateinit var binding: AutodiagnosisBinding
    private val viewModel: AutoDiagnosisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        processSetTheme(this)
        super.onCreate(savedInstanceState)
        binding = AutodiagnosisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        processActionBar(supportActionBar)

        viewModel.getLoadingProgress().observe(this) {
            binding.adgProgressBar.visibility = View.VISIBLE
            if (it == -1) {
                binding.adgListView.adapter = null
                binding.adgProgressBar.isIndeterminate = true
            } else {
                binding.adgProgressBar.isIndeterminate = false

                if (Build.VERSION.SDK_INT >= 24) {
                    binding.adgProgressBar.setProgress(it, true)
                } else {
                    binding.adgProgressBar.progress = it
                }
                if (it == 100) {
                    binding.adgProgressBar.visibility = View.GONE
                    binding.adgListView.adapter = SimpleAdapter(
                        this,
                        viewModel.getProblemsList().value,
                        R.layout.adg_list_item,
                        arrayOf("title", "sTitle", "status", "id"),
                        intArrayOf(
                            R.id.adgli_title_textView,
                            R.id.adgli_subTitle_textView,
                            R.id.adgli_status_imageView
                        )
                    )
                }
            }
        }

        binding.adgListView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val s =
                    (binding.adgListView.adapter?.getItem(position) as Map<*, *>?)?.get("id") as String?
                if (s != null) {
                    when (s) {
                        "-30" -> checkUpdate(this@AutoDiagnosisActivity)
                        "1" -> openAccessibilitySettings(this@AutoDiagnosisActivity)
                        "2" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        "4" -> if (Build.VERSION.SDK_INT >= 23) {
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
                        "6" -> startAppNotificationSettingsSystemActivity(
                            this@AutoDiagnosisActivity,
                            "cf.play" + "hi.free" + "zeyou",
                            applicationInfo.uid
                        )
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val cTheme = getUiTheme(this@AutoDiagnosisActivity)
            if ("white" == cTheme || "default" == cTheme) {
                menu.findItem(R.id.menu_autoDiagnosis_refresh)
                    .setIcon(R.drawable.ic_action_refresh_light)
                menu.findItem(R.id.menu_autoDiagnosis_help)
                    .setIcon(R.drawable.ic_action_help_outline_light)
            }
        }
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