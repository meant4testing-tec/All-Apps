package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LogDetailsCard
import com.example.ui.components.LogDisplayCard
import com.example.ui.components.LogGraphCard
import com.example.ui.components.LogHistorySheet
import com.example.ui.components.LogIdentitiesSheet
import com.example.ui.components.LogInputPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogCalculatorScreen(
    viewModel: LogCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyEntries by viewModel.historyEntries.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Functions,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Log Calculator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Precision & Properties",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setIdentitiesSheetOpen(true) },
                        modifier = Modifier.testTag("button_identities")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Logarithm Rules & Laws"
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setHistorySheetOpen(true) },
                        modifier = Modifier.testTag("button_history")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyEntries.isNotEmpty()) {
                                    Badge {
                                        Text(historyEntries.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Calculation History"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mode Toggle: Logarithm vs Antilogarithm
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mode_selector")
                ) {
                    SegmentedButton(
                        selected = uiState.mode == CalculatorMode.LOG,
                        onClick = { viewModel.setMode(CalculatorMode.LOG) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("tab_log_mode")
                    ) {
                        Text(
                            text = "Logarithm  log_b(x)",
                            fontWeight = if (uiState.mode == CalculatorMode.LOG) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    SegmentedButton(
                        selected = uiState.mode == CalculatorMode.ANTILOG,
                        onClick = { viewModel.setMode(CalculatorMode.ANTILOG) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("tab_antilog_mode")
                    ) {
                        Text(
                            text = "Antilog  bʸ = x",
                            fontWeight = if (uiState.mode == CalculatorMode.ANTILOG) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Main Result Display
                LogDisplayCard(
                    result = uiState.result,
                    mode = uiState.mode,
                    activeBaseDisplay = uiState.activeBaseDisplay,
                    errorMessage = uiState.errorMessage,
                    onSaveToHistory = { viewModel.commitCurrentToHistory() }
                )

                // Tab Switcher for Views
                val tabs = listOf(
                    "Input Panel" to Icons.Default.Straighten,
                    "Graph Curve" to Icons.AutoMirrored.Filled.ShowChart,
                    "Steps & Breakdown" to Icons.Default.Functions
                )

                PrimaryTabRow(
                    selectedTabIndex = uiState.selectedDetailTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, (label, icon) ->
                        Tab(
                            selected = uiState.selectedDetailTab == index,
                            onClick = { viewModel.setDetailTab(index) },
                            text = { Text(label, fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_view_$index")
                        )
                    }
                }

                // Active View Content
                when (uiState.selectedDetailTab) {
                    0 -> {
                        // Input Panel with Base selection & Keypad
                        LogInputPanel(
                            mode = uiState.mode,
                            basePreset = uiState.basePreset,
                            customBaseInput = uiState.customBaseInput,
                            argumentInput = uiState.argumentInput,
                            onPresetSelected = { viewModel.setBasePreset(it) },
                            onCustomBaseChanged = { viewModel.onCustomBaseChanged(it) },
                            onArgumentChanged = { viewModel.onArgumentChanged(it) },
                            onAppendArgument = { viewModel.appendToArgument(it) },
                            onBackspaceArgument = { viewModel.backspaceArgument() },
                            onClearAll = { viewModel.clearAll() }
                        )
                    }
                    1 -> {
                        // Graph Curve Visualization
                        LogGraphCard(
                            result = uiState.result,
                            mode = uiState.mode,
                            activeBaseValue = uiState.activeBaseValue,
                            activeBaseDisplay = uiState.activeBaseDisplay
                        )
                    }
                    2 -> {
                        // Steps, Characteristic & Mantissa
                        LogDetailsCard(
                            result = uiState.result,
                            mode = uiState.mode,
                            activeBaseDisplay = uiState.activeBaseDisplay
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Reference Rules & Identities Bottom Sheet
    if (uiState.isIdentitiesSheetOpen) {
        LogIdentitiesSheet(
            onDismiss = { viewModel.setIdentitiesSheetOpen(false) }
        )
    }

    // Room Database History Bottom Sheet
    if (uiState.isHistorySheetOpen) {
        LogHistorySheet(
            entries = historyEntries,
            onSelectEntry = { viewModel.restoreFromHistory(it) },
            onDeleteEntry = { viewModel.deleteHistoryEntry(it) },
            onClearAll = { viewModel.clearHistory() },
            onDismiss = { viewModel.setHistorySheetOpen(false) }
        )
    }
}
