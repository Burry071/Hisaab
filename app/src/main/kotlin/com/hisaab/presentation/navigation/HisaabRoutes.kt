package com.hisaab.presentation.navigation

/**
 * Navigation destinations for the Hisaab app.
 * Extends the existing Cashiro NavHost — add new routes here.
 */
object HisaabRoutes {
    const val SPLASH          = "splash"
    const val ONBOARDING      = "onboarding"
    const val HOME            = "home"
    const val TRANSACTIONS    = "transactions"
    const val INSIGHTS        = "insights"
    const val AGENT           = "agent"                            // AgentScreen (judge screen)
    const val AGENT_CONFLICT  = "agent/conflict/{conflictId}"
    const val ACCOUNTS        = "accounts"                         // Multi-institution net worth
    const val SIMULATION_BASE = "simulation"                       // Base route for simulation deep-links
    const val SETTINGS        = "settings"
    const val SETTINGS_LLM    = "settings/llm"                    // LlmProviderScreen
    const val ADD_TRANSACTION  = "add/{initialType}"               // AddScreen — type=EXPENSE|INCOME|TRANSFER

    fun conflictRoute(conflictId: String) = "agent/conflict/$conflictId"
    fun simulationRoute(actionType: String, category: String) = "$SIMULATION_BASE/$actionType/$category"
    fun addTransactionRoute(initialType: String = "EXPENSE") = "add/$initialType"
}

/**
 * Bottom nav items.
 * The center (+) FAB is not a route — it's an action trigger.
 */
enum class BottomNavItem(
    val route: String,
    val iconName: String,    // maps to Material Icons
    val label: String,
) {
    HOME        (HisaabRoutes.HOME,         "Home",           "Home"),
    TRANSACTIONS(HisaabRoutes.TRANSACTIONS, "Receipt",        "Transactions"),
    INSIGHTS    (HisaabRoutes.INSIGHTS,     "Lightbulb",      "Insights"),
    AGENT       (HisaabRoutes.AGENT,        "SmartToy",       "Agent"),
}
