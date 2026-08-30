package com.app.habitus.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.habitus.R
import com.app.habitus.data.models.Habit
import com.app.habitus.ui.theme.Radius
import com.app.habitus.ui.theme.Spacing
import com.app.habitus.ui.theme.Spark
import com.app.habitus.ui.theme.SparkSoft

@Composable
fun HabitCard(
    habit: Habit,
    isMarked: Boolean,
    streakDays: Int = 0,
    onMarkClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onHabitClick: () -> Unit = {}
) {
    val habitColor = habitColorFromHex(habit.color)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Micro-interacción: la tarjeta se "encoge" ligeramente al pulsarla,
    // como feedback táctil inmediato de que ha registrado el toque.
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardPressScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .scale(pressScale),
        shape = RoundedCornerShape(Radius.lg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onHabitClick,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Insignia circular de color: el icono del hábito vive dentro
                // de un círculo de su color, dándole mucha más presencia que
                // la fina franja lateral que tenía antes.
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(habitColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.icon, fontSize = 24.sp)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Spacing.md, top = 2.dp)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    Text(
                        text = formatDuration(
                            hours = habit.durationHours,
                            minutes = habit.durationMinutes
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                StreakBadge(streakDays = streakDays)

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            CompleteButton(
                isMarked = isMarked,
                onMarkClick = onMarkClick,
                modifier = Modifier.padding(top = Spacing.md)
            )
        }
    }
}

/**
 * Badge de racha: neutro mientras la racha está a 0 (nada que celebrar
 * todavía), y se "enciende" con el acento cálido [Spark] en cuanto hay
 * al menos un día — la racha es el logro, y el color lo comunica antes
 * de que el usuario lea el número.
 */
@Composable
private fun StreakBadge(streakDays: Int) {
    val hasStreak = streakDays > 0
    val bgColor by animateColorAsState(
        targetValue = if (hasStreak) SparkSoft else MaterialTheme.colorScheme.surfaceVariant,
        label = "streakBadgeBg"
    )
    val contentColor = if (hasStreak) Spark else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .background(bgColor, RoundedCornerShape(Radius.sm))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = pluralStringResource(R.plurals.streak_badge, streakDays, streakDays),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * Botón "Completar" con transición animada de color e icono al marcar
 * el hábito: el cambio de estado más gratificante de toda la app ahora
 * tiene feedback visual real, no solo un cambio de texto instantáneo.
 */
@Composable
private fun CompleteButton(
    isMarked: Boolean,
    onMarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isMarked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
        label = "completeButtonBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isMarked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
        label = "completeButtonContent"
    )

    Button(
        onClick = onMarkClick,
        enabled = !isMarked,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        AnimatedContent(targetState = isMarked, label = "completeButtonContentSwap") { marked ->
            Text(
                text = stringResource(if (marked) R.string.action_completed else R.string.action_complete),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun formatDuration(hours: Int, minutes: Int): String {
    return when {
        hours > 0 && minutes > 0 -> stringResource(R.string.duration_hours_minutes, hours, minutes)
        hours > 0 -> stringResource(R.string.duration_hours, hours)
        else -> stringResource(R.string.duration_minutes, minutes)
    }
}

private fun habitColorFromHex(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFF1FAE72)
    }
}