package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculationResult
import com.example.ui.CalculatorMode
import java.util.Locale

@Composable
fun LogDetailsCard(
    result: CalculationResult?,
    mode: CalculatorMode,
    activeBaseDisplay: String,
    modifier: Modifier = Modifier
) {
    if (result == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("details_card"),
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
            Text(
                text = "Mathematical Breakdown & Steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Change-of-Base Formula
            DetailSectionItem(
                icon = Icons.Default.Calculate,
                title = "Change of Base Formula",
                subtitle = "Using Natural Logarithm (ln):",
                content = result.changeOfBaseLn
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailSectionItem(
                icon = Icons.Default.Calculate,
                title = "Common Log Equivalent",
                subtitle = "Using Common Logarithm (log₁₀):",
                content = result.changeOfBaseLog10
            )

            // Step 2: Base 10 Characteristic & Mantissa (if available)
            if (result.characteristic != null && result.mantissa != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailSectionItem(
                    icon = Icons.Default.Science,
                    title = "Scientific & Mantissa Breakdown",
                    subtitle = "Scientific notation of argument: ${result.scientificNotation}",
                    content = "Characteristic (Integer part): ${result.characteristic}\n" +
                            "Mantissa (Fractional part): ${String.format(Locale.US, "%.6f", result.mantissa)}\n" +
                            "Total log₁₀ = ${result.characteristic} + ${String.format(Locale.US, "%.6f", result.mantissa)}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Step 3: Exponential Equation
            DetailSectionItem(
                icon = Icons.Default.Info,
                title = "Exponential Equivalence",
                subtitle = "By definition of logarithm:",
                content = "If log_b(x) = y, then b^y = x\nHere: ${result.exponentialForm}"
            )
        }
    }
}

@Composable
private fun DetailSectionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}
