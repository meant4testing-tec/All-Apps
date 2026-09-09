package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class LogIdentity(
    val name: String,
    val formula: String,
    val explanation: String
)

private val IDENTITIES_LIST = listOf(
    LogIdentity(
        name = "Product Rule",
        formula = "log_b(x · y) = log_b(x) + log_b(y)",
        explanation = "The logarithm of a product equals the sum of individual logarithms."
    ),
    LogIdentity(
        name = "Quotient Rule",
        formula = "log_b(x / y) = log_b(x) - log_b(y)",
        explanation = "The logarithm of a quotient equals the difference of the logarithms."
    ),
    LogIdentity(
        name = "Power Rule",
        formula = "log_b(xᵏ) = k · log_b(x)",
        explanation = "Powers inside the argument can be pulled out as multiplicative coefficients."
    ),
    LogIdentity(
        name = "Change of Base Rule",
        formula = "log_b(x) = ln(x) / ln(b) = log₁₀(x) / log₁₀(b)",
        explanation = "Allows conversion of any arbitrary base logarithm to natural or common logs."
    ),
    LogIdentity(
        name = "Logarithm of Base",
        formula = "log_b(b) = 1",
        explanation = "Because b¹ = b, taking log of the base itself always equals 1."
    ),
    LogIdentity(
        name = "Logarithm of One",
        formula = "log_b(1) = 0",
        explanation = "Because b⁰ = 1 for any nonzero base, the logarithm of 1 is always 0."
    ),
    LogIdentity(
        name = "Reciprocal Rule",
        formula = "log_b(1 / x) = -log_b(x)",
        explanation = "The logarithm of a reciprocal is the negative of the original logarithm."
    ),
    LogIdentity(
        name = "Inverse Properties",
        formula = "b^(log_b(x)) = x  and  log_b(bˣ) = x",
        explanation = "Logarithmic and exponential functions with matching bases undo each other."
    ),
    LogIdentity(
        name = "Base Power Rule",
        formula = "log_(bᵏ)(x) = (1 / k) · log_b(x)",
        explanation = "A power on the base pulls out as the reciprocal coefficient."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogIdentitiesSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("identities_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Logarithm Rules & Laws",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(IDENTITIES_LIST) { identity ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = identity.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = identity.formula,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = identity.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
