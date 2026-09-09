package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculationResult
import com.example.ui.CalculatorMode
import com.example.ui.theme.MathAccentAmber
import com.example.ui.theme.MathAccentGreen
import com.example.ui.theme.MathAccentRose
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogGraphCard(
    result: CalculationResult?,
    mode: CalculatorMode,
    activeBaseValue: Double,
    activeBaseDisplay: String,
    modifier: Modifier = Modifier
) {
    val isLogMode = mode == CalculatorMode.LOG
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = outlineColor.copy(alpha = 0.35f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("graph_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLogMode) "Function Curve: f(x) = log${activeBaseDisplay}(x)" else "Function Curve: f(x) = ${activeBaseDisplay}ˣ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Area for Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(206.dp)
                        .testTag("log_curve_canvas")
                ) {
                    val w = size.width
                    val h = size.height

                    // Determine viewport coordinate ranges
                    val targetX = result?.argument ?: 10.0
                    val targetY = result?.resultValue ?: 1.0

                    val maxX = max(10.0, max(targetX * 1.3, activeBaseValue * 1.5))
                    val minX = 0.0

                    // Y bounds: centered around 0
                    val maxY = max(3.0, max(abs(targetY) * 1.4, 2.5))
                    val minY = -maxY

                    // Scale transformations: math coordinates to canvas pixels
                    // Margins
                    val marginX = 28f
                    val plotW = w - marginX - 16f
                    val plotH = h - 24f

                    fun toCanvasX(x: Double): Float {
                        return marginX + ((x - minX) / (maxX - minX)).toFloat() * plotW
                    }

                    fun toCanvasY(y: Double): Float {
                        return 12f + ((maxY - y) / (maxY - minY)).toFloat() * plotH
                    }

                    val originX = toCanvasX(0.0)
                    val originY = toCanvasY(0.0)

                    // Draw Grid Lines
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                    // Horizontal zero line (X-axis)
                    drawLine(
                        color = outlineColor.copy(alpha = 0.8f),
                        start = Offset(marginX, originY),
                        end = Offset(w - 10f, originY),
                        strokeWidth = 2f
                    )

                    // Vertical line (Y-axis / Asymptote)
                    drawLine(
                        color = if (isLogMode) MathAccentRose.copy(alpha = 0.9f) else outlineColor.copy(alpha = 0.8f),
                        start = Offset(originX, 10f),
                        end = Offset(originX, h - 10f),
                        strokeWidth = if (isLogMode) 2f else 2f,
                        pathEffect = if (isLogMode) dashedEffect else null
                    )

                    // Draw horizontal reference lines: y = 1 and y = -1
                    val y1 = toCanvasY(1.0)
                    val yMinus1 = toCanvasY(-1.0)
                    drawLine(
                        color = gridColor,
                        start = Offset(marginX, y1),
                        end = Offset(w - 10f, y1),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(marginX, yMinus1),
                        end = Offset(w - 10f, yMinus1),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )

                    // Plot the curve
                    val curvePath = Path()
                    var isFirst = true

                    val steps = 120
                    for (i in 1..steps) {
                        val t = i.toFloat() / steps
                        val curX = minX + t * (maxX - minX)
                        val curY = if (isLogMode) {
                            if (curX <= 0.0) continue
                            if (activeBaseValue <= 0.0 || abs(activeBaseValue - 1.0) < 1e-9) continue
                            ln(curX) / ln(activeBaseValue)
                        } else {
                            activeBaseValue.pow(curX)
                        }

                        if (curY.isNaN() || curY.isInfinite()) continue

                        val px = toCanvasX(curX)
                        val py = toCanvasY(curY).coerceIn(-50f, h + 50f)

                        if (isFirst) {
                            curvePath.moveTo(px, py)
                            isFirst = false
                        } else {
                            curvePath.lineTo(px, py)
                        }
                    }

                    // Draw curve stroke with a vibrant gradient
                    drawPath(
                        path = curvePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        ),
                        style = Stroke(width = 4.5f)
                    )

                    // Highlight Reference Point (1, 0)
                    if (isLogMode) {
                        val pt10X = toCanvasX(1.0)
                        val pt10Y = toCanvasY(0.0)
                        drawCircle(
                            color = MathAccentGreen,
                            radius = 5f,
                            center = Offset(pt10X, pt10Y)
                        )

                        // Highlight Reference Point (b, 1)
                        if (activeBaseValue in minX..maxX) {
                            val ptB1X = toCanvasX(activeBaseValue)
                            val ptB1Y = toCanvasY(1.0)
                            drawCircle(
                                color = MathAccentAmber,
                                radius = 5f,
                                center = Offset(ptB1X, ptB1Y)
                            )
                        }
                    }

                    // Highlight Current Result Point (targetX, targetY)
                    if (result != null && targetX > 0 && !targetY.isNaN() && !targetY.isInfinite()) {
                        val curPtX = toCanvasX(targetX)
                        val curPtY = toCanvasY(targetY)

                        // Drop lines to axes
                        drawLine(
                            color = primaryColor.copy(alpha = 0.5f),
                            start = Offset(curPtX, originY),
                            end = Offset(curPtX, curPtY),
                            strokeWidth = 1.5f,
                            pathEffect = dashedEffect
                        )
                        drawLine(
                            color = primaryColor.copy(alpha = 0.5f),
                            start = Offset(originX, curPtY),
                            end = Offset(curPtX, curPtY),
                            strokeWidth = 1.5f,
                            pathEffect = dashedEffect
                        )

                        // Outer glowing ring
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.25f),
                            radius = 12f,
                            center = Offset(curPtX, curPtY)
                        )
                        // Inner solid marker
                        drawCircle(
                            color = primaryColor,
                            radius = 6f,
                            center = Offset(curPtX, curPtY)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Graph Legend and Properties
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PropertyBadge(
                    label = "Asymptote: x = 0",
                    indicatorColor = MathAccentRose
                )
                PropertyBadge(
                    label = "Intercept: (1, 0)",
                    indicatorColor = MathAccentGreen
                )
                if (isLogMode && activeBaseValue > 0 && activeBaseValue != 1.0) {
                    PropertyBadge(
                        label = "Base Point: ($activeBaseDisplay, 1)",
                        indicatorColor = MathAccentAmber
                    )
                }
                if (result != null) {
                    PropertyBadge(
                        label = "Current: (${result.argument}, ${result.formattedResult})",
                        indicatorColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyBadge(
    label: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = indicatorColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
