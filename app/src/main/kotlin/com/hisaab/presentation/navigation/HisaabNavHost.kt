package com.hisaab.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hisaab.domain.model.BudgetAction
import com.hisaab.domain.model.BudgetState
import com.hisaab.domain.llm.LlmHealthMonitor
import com.hisaab.domain.llm.LlmUsageTracker
import com.hisaab.presentation.ui.HisaabBottomNav
import com.hisaab.presentation.ui.accounts.AccountsScreen
import com.hisaab.presentation.ui.accounts.demoAccounts
import com.hisaab.presentation.ui.agent.AgentScreen
import com.hisaab.presentation.ui.agent.ConflictDetailScreen
import com.hisaab.presentation.ui.home.HomeScreen
import com.hisaab.presentation.ui.insights.InsightsScreen
import com.hisaab.presentation.ui.settings.LlmProviderScreen
import com.hisaab.presentation.ui.simulation.SimulationScreen
import com.hisaab.presentation.ui.transactions.TransactionsScreen
import com.hisaab.presentation.ui.onboarding.HisaabOnboardingScreen
import com.hisaab.presentation.ui.add.AddScreen
import com.hisaab.presentation.ui.add.EntryType
import com.hisaab.presentation.viewmodels.AgentViewModel
import com.hisaab.presentation.viewmodels.HomeViewModel
import com.hisaab.presentation.viewmodels.InsightsViewModel
import com.hisaab.presentation.viewmodels.SettingsViewModel
import com.hisaab.presentation.viewmodels.AddTransactionViewModel
import java.math.BigDecimal

/**
 * HisaabNavHost — wires all screens to their navigation routes.
 *
 * Routes defined in HisaabRoutes object:
 *   home, transactions, insights, agent, agent/conflict/{conflictId},
 *   accounts, settings/llm, simulation/{actionType}
 *
 * Bottom nav is shown for: home, transactions, insights, agent.
 * Back-stack screens (conflict, simulation, settings) hide the bottom nav.
 */
@Composable
fun HisaabNavHost(
    homeViewModel          : HomeViewModel,
    agentViewModel         : AgentViewModel,
    insightsViewModel      : InsightsViewModel,
    settingsViewModel      : SettingsViewModel,
    addTransactionViewModel: AddTransactionViewModel,
    healthMonitor          : LlmHealthMonitor,
    usageTracker           : LlmUsageTracker,
) {
    val navController = rememberNavController()
    val currentEntry  by navController.currentBackStackEntryAsState()
    val currentRoute  = currentEntry?.destination?.route

    // Routes that show the bottom nav
    val bottomNavRoutes = setOf(
        HisaabRoutes.HOME, HisaabRoutes.TRANSACTIONS,
        HisaabRoutes.INSIGHTS, HisaabRoutes.AGENT,
    )
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                HisaabBottomNav(
                    navController = navController,
                    onFabClick    = {
                        navController.navigate(HisaabRoutes.addTransactionRoute("EXPENSE"))
                    },
                )
            }
        },
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = HisaabRoutes.ONBOARDING,
            modifier         = Modifier.padding(innerPadding),
        ) {

            // ── ONBOARDING ────────────────────────────────────────────────────
            composable(HisaabRoutes.ONBOARDING) {
                HisaabOnboardingScreen(
                    onStartClick = {
                        navController.navigate(HisaabRoutes.HOME) {
                            popUpTo(HisaabRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            // ── HOME ──────────────────────────────────────────────────────────
            composable(HisaabRoutes.HOME) {
                HomeScreen(
                    viewModel        = homeViewModel,
                    onSeeAllInsights = { navController.navigate(HisaabRoutes.INSIGHTS) },
                    onSeeAllTxns     = { navController.navigate(HisaabRoutes.TRANSACTIONS) },
                    onConflictTap    = { id -> navController.navigate(HisaabRoutes.conflictRoute(id)) },
                    onAgentTap       = { navController.navigate(HisaabRoutes.AGENT) },
                )
            }

            // ── TRANSACTIONS ──────────────────────────────────────────────────
            composable(HisaabRoutes.TRANSACTIONS) {
                val state by homeViewModel.uiState.collectAsState()
                val txns  = (state as? com.hisaab.presentation.viewmodels.HomeUiState.Success)
                    ?.recentTransactions ?: emptyList()
                TransactionsScreen(
                    transactions = txns,
                    onBack       = { navController.popBackStack() },
                )
            }

            // ── INSIGHTS ──────────────────────────────────────────────────────
            composable(HisaabRoutes.INSIGHTS) {
                val insights by insightsViewModel.insights.collectAsState()
                InsightsScreen(
                    insights   = insights,
                    onBack     = { navController.popBackStack() },
                    onSimulate = { action ->
                        // Navigate to simulation with action encoded as type+category
                        navController.navigate("${HisaabRoutes.SIMULATION_BASE}/${action.type.name}/${action.targetCategory.name}")
                    },
                )
            }

            // ── AGENT (Judge screen) ──────────────────────────────────────────
            composable(HisaabRoutes.AGENT) {
                AgentScreen(
                    viewModel    = agentViewModel,
                    healthMonitor = healthMonitor,
                    usageTracker = usageTracker,
                    onRunAgain   = { agentViewModel.runPipeline() },
                )
            }

            // ── CONFLICT DETAIL ───────────────────────────────────────────────
            composable(
                route     = HisaabRoutes.AGENT_CONFLICT,
                arguments = listOf(navArgument("conflictId") { type = NavType.StringType }),
            ) { backStack ->
                val conflictId = backStack.arguments?.getString("conflictId") ?: return@composable
                val homeState  = (homeViewModel.uiState.collectAsState().value
                    as? com.hisaab.presentation.viewmodels.HomeUiState.Success)
                val conflict   = homeState?.conflicts?.find { it.incomingId == conflictId }
                    ?: return@composable

                ConflictDetailScreen(
                    conflict           = conflict,
                    onAcceptResolution = {
                        homeViewModel.resolveConflict(conflictId, accepted = true)
                        navController.popBackStack()
                    },
                    onReviewManually   = {
                        homeViewModel.resolveConflict(conflictId, accepted = false)
                        navController.popBackStack()
                    },
                    onBack             = { navController.popBackStack() },
                )
            }

            // ── SIMULATION ────────────────────────────────────────────────────
            composable(
                route     = "${HisaabRoutes.SIMULATION_BASE}/{actionType}/{category}",
                arguments = listOf(
                    navArgument("actionType") { type = NavType.StringType },
                    navArgument("category")   { type = NavType.StringType },
                ),
            ) { backStack ->
                val actionTypeName = backStack.arguments?.getString("actionType") ?: "REDUCE_CATEGORY"
                val categoryName   = backStack.arguments?.getString("category")   ?: "FOOD"

                val action = com.hisaab.domain.model.BudgetAction(
                    type            = com.hisaab.domain.model.ActionType.valueOf(actionTypeName),
                    targetCategory  = com.hisaab.domain.model.InsightCategory.valueOf(categoryName),
                    targetAmount    = BigDecimal("300"),
                    rationale       = "Cut daily spending by PKR 300",
                    projectedSaving = BigDecimal("2400"),
                    effortScore     = 2,
                    impactScore     = 5,
                )
                val state = BudgetState(
                    totalBalance         = BigDecimal("234580"),
                    monthlyIncome        = BigDecimal("80000"),
                    categorySpends       = mapOf(
                        com.hisaab.domain.model.InsightCategory.FOOD to BigDecimal("18400"),
                        com.hisaab.domain.model.InsightCategory.TRANSPORT to BigDecimal("5000"),
                    ),
                    daysRemainingInMonth = 6,
                )

                SimulationScreen(
                    action       = action,
                    currentState = state,
                    onApply      = { result ->
                        homeViewModel.applySimulation(result)
                        navController.navigate(HisaabRoutes.HOME) {
                            popUpTo(HisaabRoutes.HOME) { inclusive = true }
                        }
                    },
                    onTryAnother = { navController.popBackStack() },
                    onBack       = { navController.popBackStack() },
                )
            }

            // ── ACCOUNTS ──────────────────────────────────────────────────────
            composable(HisaabRoutes.ACCOUNTS) {
                AccountsScreen(
                    accounts = demoAccounts,
                    onBack   = { navController.popBackStack() },
                )
            }

            // ── SETTINGS / LLM ────────────────────────────────────────────────
            composable(HisaabRoutes.SETTINGS_LLM) {
                LlmProviderScreen(viewModel = settingsViewModel)
            }

            // ── ADD TRANSACTION ───────────────────────────────────────────────
            composable(
                route     = HisaabRoutes.ADD_TRANSACTION,
                arguments = listOf(navArgument("initialType") {
                    type         = NavType.StringType
                    defaultValue = "EXPENSE"
                }),
            ) { backStack ->
                val typeName    = backStack.arguments?.getString("initialType") ?: "EXPENSE"
                val initialType = runCatching { EntryType.valueOf(typeName) }.getOrDefault(EntryType.EXPENSE)

                AddScreen(
                    initialType   = initialType,
                    viewModel     = addTransactionViewModel,
                    homeViewModel = homeViewModel,
                    navController = navController,
                    onBack        = { navController.popBackStack() },
                )
            }
        }
    }
}
