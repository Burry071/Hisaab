package com.hisaab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.hisaab.domain.llm.CachedLlmService
import com.hisaab.domain.llm.LlmHealthMonitor
import com.hisaab.domain.llm.LlmUsageTracker
import com.hisaab.presentation.navigation.HisaabNavHost
import com.hisaab.presentation.ui.theme.HisaabTheme
import com.hisaab.presentation.viewmodels.AgentViewModel
import com.hisaab.presentation.viewmodels.HomeViewModel
import com.hisaab.presentation.viewmodels.InsightsViewModel
import com.hisaab.presentation.viewmodels.SettingsViewModel
import com.hisaab.presentation.viewmodels.AddTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.hisaab.data.demo.DemoDataSeeder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject

/**
 * MainActivity — single-activity host for the Hisaab app.
 *
 * Responsibilities:
 *   1. Request SMS_READ + RECEIVE + NOTIFICATIONS permissions on first launch.
 *   2. Provide ViewModels to HisaabNavHost.
 *   3. Long-press on balance triggers DemoModeManager.injectDemoData().
 *
 * Edge-to-edge, no ActionBar (set in theme XML).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel           : HomeViewModel           by viewModels()
    private val agentViewModel          : AgentViewModel          by viewModels()
    private val insightsViewModel       : InsightsViewModel       by viewModels()
    private val settingsViewModel       : SettingsViewModel       by viewModels()
    private val addTransactionViewModel : AddTransactionViewModel by viewModels()

    // Permission launcher (batch request)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val smsGranted = grants[Manifest.permission.READ_SMS] == true
        if (smsGranted) agentViewModel.runPipeline()
    }

    @Inject lateinit var healthMonitor: LlmHealthMonitor
    @Inject lateinit var usageTracker: LlmUsageTracker
    @Inject lateinit var demoDataSeeder: DemoDataSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("GEMINI_KEY_CHECK", "Key value is: ${BuildConfig.GEMINI_API_KEY}")

        // Request permissions on first launch; pipeline starts after grant
        requestRequiredPermissions()

        if (BuildConfig.DEBUG) {
            lifecycleScope.launch(Dispatchers.IO) {
                demoDataSeeder.seedIfEmpty()
            }
        }

        setContent {
            HisaabTheme {


                HisaabNavHost(
                    homeViewModel           = homeViewModel,
                    agentViewModel          = agentViewModel,
                    insightsViewModel       = insightsViewModel,
                    settingsViewModel       = settingsViewModel,
                    addTransactionViewModel = addTransactionViewModel,
                    healthMonitor           = healthMonitor,
                    usageTracker            = usageTracker,
                )
            }
        }
    }

    private fun requestRequiredPermissions() {
        val needed = buildList {
            if (!has(Manifest.permission.READ_SMS))    add(Manifest.permission.READ_SMS)
            if (!has(Manifest.permission.RECEIVE_SMS)) add(Manifest.permission.RECEIVE_SMS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !has(Manifest.permission.POST_NOTIFICATIONS)
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
        else agentViewModel.runPipeline()
    }

    private fun has(perm: String) =
        ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
}
