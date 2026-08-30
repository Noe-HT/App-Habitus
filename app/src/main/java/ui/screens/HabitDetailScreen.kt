package com.app.habitus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.habitus.R
import com.app.habitus.ui.theme.Radius
import com.app.habitus.ui.theme.Spacing
import com.app.habitus.viewmodel.HabitViewModel

@Composable
fun HabitDetailScreen(
    habitId: Int = 1,
    viewModel: HabitViewModel = viewModel(),
    onEditClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val habit by viewModel.getHabitById(habitId).observeAsState()
    val currentHabit = habit
    val streak by viewModel.currentStreak.observeAsState(0)
    val completion by viewModel.completionPercentage.observeAsState(0)
    val totalDays by viewModel.totalCompletedDays.observeAsState(0)

    LaunchedEffect(habitId) {
        viewModel.getHabitStreak(habitId)
        viewModel.getCompletionPercentage(habitId)
        viewModel.getTotalCompletedDays(habitId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera simple, consistente con el resto de la app: solo
        // flecha atrás + acción de editar, sin tarjeta envolvente.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.action_edit),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = Spacing.lg)
                    .clickable(onClick = onEditClick)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            HeroStreakCard(
                icon = currentHabit?.icon ?: "📝",
                name = currentHabit?.name ?: stringResource(R.string.loading_generic),
                streak = if (streak <= 0) 0 else streak,
                durationLabel = currentHabit?.let {
                    formatDuration(it.durationHours, it.durationMinutes)
                }
            )

            currentHabit?.takeIf { it.description.isNotBlank() }?.let { safeHabit ->
                Column {
                    Text(
                        text = stringResource(R.string.label_description),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = safeHabit.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            Text(
                text = stringResource(R.string.label_stats),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = "🎯",
                    title = stringResource(R.string.label_completed_pct),
                    value = "${if (completion <= 0) 0 else completion}%"
                )

                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = "📅",
                    title = stringResource(R.string.label_total_days),
                    value = "$totalDays"
                )
            }

            currentHabit?.let { safeHabit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.xl),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(Radius.md)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⏰", style = MaterialTheme.typography.headlineSmall)
                        Column {
                            Text(
                                text = stringResource(R.string.label_reminder),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = safeHabit.reminderTime,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta hero de racha: usa el verde de marca real ([MaterialTheme]
 * `primary`), no un color fijo aparte — así el "momento estrella" de la
 * pantalla queda coherente con el resto de la identidad de la app. El
 * número de racha usa la escala `display`, reservada para los datos con
 * más impacto visual de toda la aplicación.
 */
@Composable
private fun HeroStreakCard(
    icon: String,
    name: String,
    streak: Int,
    durationLabel: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(Radius.lg)
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Text(
                text = stringResource(R.string.habit_name_with_icon, icon, name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )

            Column(modifier = Modifier.padding(top = Spacing.sm)) {
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = pluralStringResource(R.plurals.streak_days, streak, streak),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            durationLabel?.let {
                Text(
                    text = "${stringResource(R.string.label_duration)}: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun DetailMetricCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(Radius.md)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
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