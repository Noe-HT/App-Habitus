package com.app.habitus.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.habitus.R
import com.app.habitus.viewmodel.HabitViewModel


private val HeroGreen = Color(0xFF17B37C)

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
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = stringResource(R.string.detail_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = stringResource(R.string.action_edit),
                        style = MaterialTheme.typography.labelMedium,
                        color = HeroGreen,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(onClick = onEditClick)
                    )
                }

                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HeroGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.habit_name_with_icon,
                                    currentHabit?.icon ?: "📝",
                                    currentHabit?.name ?: stringResource(R.string.loading_generic)
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFE8FFF6)
                            )

                            Text(
                                text = pluralStringResource(
                                    R.plurals.streak_days,
                                    if (streak <= 0) 0 else streak,
                                    if (streak <= 0) 0 else streak
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                modifier = Modifier.padding(top = 6.dp)
                            )


                            currentHabit?.let { safeHabit ->
                                Text(
                                    text = stringResource(
                                        R.string.duration_label_value,
                                        stringResource(R.string.label_duration),
                                        formatDuration(safeHabit.durationHours, safeHabit.durationMinutes)
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFD8FFF0),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }

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
                                modifier = Modifier.padding(top = 6.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailMetricCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.label_completed_pct),
                            value = "${if (completion <= 0) 0 else completion}%"
                        )

                        DetailMetricCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.label_total_days),
                            value = "$totalDays"
                        )
                    }

                    currentHabit?.let { safeHabit ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.label_reminder),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = stringResource(R.string.reminder_display, safeHabit.reminderTime),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 6.dp)
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