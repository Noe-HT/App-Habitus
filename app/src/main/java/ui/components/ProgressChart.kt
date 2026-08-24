package com.app.habitus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.app.habitus.ui.theme.GreenHabit

@Composable
fun ProgressChart(
    progress: Float = 0.76f,
    label: String = "Logrado",
    subtitle: String = "Tu progreso de esta semana",
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.outline
    val cardColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(156.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(156.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(
                        (size.width - diameter) / 2,
                        (size.height - diameter) / 2
                    )

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    drawArc(
                        color = GreenHabit,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = primaryTextColor
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}