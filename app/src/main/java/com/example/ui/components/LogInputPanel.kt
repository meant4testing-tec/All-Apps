package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BasePreset
import com.example.ui.CalculatorMode

@Composable
fun LogInputPanel(
    mode: CalculatorMode,
    basePreset: BasePreset,
    customBaseInput: String,
    argumentInput: String,
    onPresetSelected: (BasePreset) -> Unit,
    onCustomBaseChanged: (String) -> Unit,
    onArgumentChanged: (String) -> Unit,
    onAppendArgument: (String) -> Unit,
    onBackspaceArgument: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("input_panel"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Base Selection Title & Chips
            Text(
                text = "Logarithm Base (b)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasePreset.values().forEach { preset ->
                    val selected = basePreset == preset
                    FilterChip(
                        selected = selected,
                        onClick = { onPresetSelected(preset) },
                        label = {
                            Text(
                                text = preset.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("chip_base_${preset.name.lowercase()}")
                    )
                }
            }

            // Custom Base Input Field if custom is chosen
            AnimatedVisibility(
                visible = basePreset == BasePreset.CUSTOM,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = customBaseInput,
                        onValueChange = onCustomBaseChanged,
                        label = { Text("Enter Custom Base (b > 0, b ≠ 1)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        trailingIcon = {
                            if (customBaseInput.isNotEmpty()) {
                                IconButton(onClick = { onCustomBaseChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Base")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_base"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Argument / Exponent Input Field
            Text(
                text = if (mode == CalculatorMode.LOG) "Argument (x > 0)" else "Exponent (y)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = argumentInput,
                onValueChange = onArgumentChanged,
                placeholder = {
                    Text(
                        if (mode == CalculatorMode.LOG) "e.g. 100, 2.718, 0.5" else "e.g. 2, 3, -1"
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                trailingIcon = {
                    if (argumentInput.isNotEmpty()) {
                        IconButton(onClick = onClearAll) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Argument")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_argument"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Values for Argument
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val quickConstants = if (mode == CalculatorMode.LOG) {
                    listOf(
                        "10" to "10",
                        "100" to "100",
                        "1000" to "1000",
                        "e" to "2.71828",
                        "π" to "3.14159",
                        "2" to "2",
                        "8" to "8",
                        "0.5" to "0.5",
                        "1" to "1"
                    )
                } else {
                    listOf(
                        "0" to "0",
                        "1" to "1",
                        "2" to "2",
                        "3" to "3",
                        "-1" to "-1",
                        "-2" to "-2",
                        "0.5" to "0.5"
                    )
                }

                quickConstants.forEach { (label, value) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onArgumentChanged(value) }
                            .testTag("quick_val_$label")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // On-screen Keypad for quick tactile typing
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val keyRows = listOf(
                    listOf("7", "8", "9", "C"),
                    listOf("4", "5", "6", "⌫"),
                    listOf("1", "2", "3", "±"),
                    listOf("0", ".", "00", "e")
                )

                keyRows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKeys.forEach { key ->
                            KeypadButton(
                                text = key,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    when (key) {
                                        "C" -> onClearAll()
                                        "⌫" -> onBackspaceArgument()
                                        "±" -> {
                                            if (argumentInput.startsWith("-")) {
                                                onArgumentChanged(argumentInput.removePrefix("-"))
                                            } else if (argumentInput.isNotEmpty()) {
                                                onArgumentChanged("-$argumentInput")
                                            }
                                        }
                                        "e" -> onArgumentChanged("2.71828")
                                        "00" -> {
                                            onAppendArgument("0")
                                            onAppendArgument("0")
                                        }
                                        else -> onAppendArgument(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAction = text in listOf("C", "⌫", "±")
    val containerColor = if (isAction) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (isAction) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
            .height(48.dp)
            .testTag("keypad_$text")
    ) {
        Box(
            modifier = Modifier.padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (text == "⌫") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}
