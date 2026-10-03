package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScreenDestination
import com.example.model.ThemeMode
import com.example.ui.ExpenseViewModel
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.BreakdownScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettlementScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ExpenseSplitTheme
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val isDark = when (uiState.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ExpenseSplitTheme(darkTheme = isDark) {
                ExpenseSplitApp(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ExpenseSplitApp(
    viewModel: ExpenseViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.notificationMessage) {
        uiState.notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    val breakdowns = remember(uiState.expenses, uiState.flatmates) {
        viewModel.getBreakdowns()
    }
    val obligations = remember(breakdowns, uiState.settledObligationIds) {
        viewModel.getObligations()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Screen Switcher bar to preview all 5 screens effortlessly
            ScreenQuickSwitcherBar(
                currentScreen = uiState.currentScreen,
                onSelectScreen = { viewModel.navigateTo(it) }
            )
        },
        bottomBar = {
            // Hide bottom navigation bar when on Add Expense screen for clean form experience
            if (uiState.currentScreen != ScreenDestination.ADD_EXPENSE) {
                AppBottomNavigationBar(
                    currentScreen = uiState.currentScreen,
                    onSelectScreen = { viewModel.navigateTo(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                ScreenDestination.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onNavigate = { viewModel.navigateTo(it) },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                }

                ScreenDestination.ADD_EXPENSE -> {
                    AddExpenseScreen(
                        uiState = uiState,
                        onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onSaveExpense = { item, amount, paidBy, date, category, splitAmong ->
                            viewModel.addExpense(item, amount, paidBy, date, category, splitAmong)
                        }
                    )
                }

                ScreenDestination.SETTLEMENT -> {
                    SettlementScreen(
                        uiState = uiState,
                        obligations = obligations,
                        onToggleSettle = { viewModel.toggleSettleObligation(it) }
                    )
                }

                ScreenDestination.BREAKDOWN -> {
                    BreakdownScreen(
                        uiState = uiState,
                        breakdowns = breakdowns
                    )
                }

                ScreenDestination.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onShowPrivacyDialog = { viewModel.setShowPrivacyDialog(it) },
                        onShowClearDataDialog = { viewModel.setShowClearDataDialog(it) },
                        onShowAddFlatmateDialog = { viewModel.setShowAddFlatmateDialog(it) },
                        onAddFlatmate = { viewModel.addFlatmate(it) },
                        onRemoveFlatmate = { viewModel.removeFlatmate(it) },
                        onClearAllData = { viewModel.clearAllData() },
                        onResetSampleData = { viewModel.loadSampleData() }
                    )
                }
            }
        }
    }
}

/**
 * Top Screen Quick Switcher Bar:
 * Allows user / reviewer to quickly tap any of the 5 requested screens:
 * 1. Home
 * 2. Add Expense
 * 3. Settlement
 * 4. Breakdown
 * 5. Settings
 */
@Composable
fun ScreenQuickSwitcherBar(
    currentScreen: ScreenDestination,
    onSelectScreen: (ScreenDestination) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("screen_quick_switcher"),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScreenDestination.values().forEachIndexed { index, screen ->
                    val isSelected = currentScreen == screen
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectScreen(screen) },
                        label = {
                            Text(
                                text = "${index + 1}. ${screen.title}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("switch_to_${screen.name.lowercase()}")
                    )
                }
            }
        }
    }
}

/**
 * Clean M3 Bottom Navigation Bar
 */
@Composable
fun AppBottomNavigationBar(
    currentScreen: ScreenDestination,
    onSelectScreen: (ScreenDestination) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        // 1. Home
        NavigationBarItem(
            selected = currentScreen == ScreenDestination.HOME,
            onClick = { onSelectScreen(ScreenDestination.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == ScreenDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoLightBg,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("bottom_nav_home")
        )

        // 2. Settlement
        NavigationBarItem(
            selected = currentScreen == ScreenDestination.SETTLEMENT,
            onClick = { onSelectScreen(ScreenDestination.SETTLEMENT) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == ScreenDestination.SETTLEMENT) Icons.Filled.Handshake else Icons.Outlined.Handshake,
                    contentDescription = "Settlement"
                )
            },
            label = {
                Text(
                    text = "Settlement",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoLightBg,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("bottom_nav_settlement")
        )

        // 3. Breakdown
        NavigationBarItem(
            selected = currentScreen == ScreenDestination.BREAKDOWN,
            onClick = { onSelectScreen(ScreenDestination.BREAKDOWN) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == ScreenDestination.BREAKDOWN) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                    contentDescription = "Breakdown"
                )
            },
            label = {
                Text(
                    text = "Breakdown",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoLightBg,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("bottom_nav_breakdown")
        )

        // 4. Settings
        NavigationBarItem(
            selected = currentScreen == ScreenDestination.SETTINGS,
            onClick = { onSelectScreen(ScreenDestination.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == ScreenDestination.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoLightBg,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("bottom_nav_settings")
        )
    }
}
