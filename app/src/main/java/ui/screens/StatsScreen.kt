package com.app.habitus.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.habitus.R
import com.app.habitus.ui.components.ProgressChart
import com.app.habitus.ui.theme.Radius
import com.app.habitus.ui.theme.Spacing
import com.app.habitus.viewmodel.HabitViewModel

@Composable
fun StatsScreen(
    viewModel: HabitViewModel = viewModel()
) {
    val habits = viewModel.allHabits.observeAsState(emptyList())
    val completionState = viewModel.habitCompletionState.observeAsState(emptyMap())

    LaunchedEffect(habits.value) {
        if (habits.value.isNotEmpty()) {
            viewModel.loadHabitsState(habits.value)
        }
    }

    val completedTodayCount = habits.value.count { habit ->
        completionState.value[habit.id] == true
    }

    val totalHabits = habits.value.size

    val overallProgress = if (totalHabits == 0) {
        0
    } else {
        (completedTodayCount * 100) / totalHabits
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                MiniStatCard(
                    icon = "📊",
                    title = stringResource(R.string.stats_today),
                    value = "$overallProgress%",
                    modifier = Modifier.weight(1f)
                )
                MiniStatCard(
                    icon = "✅",
                    title = stringResource(R.string.stats_completed),
                    value = "$completedTodayCount/$totalHabits",
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.stats_daily_average),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            ProgressChart(
                progress = (overallProgress / 100f).coerceIn(0f, 1f),
                label = stringResource(R.string.stats_achieved_today),
                subtitle = stringResource(R.string.stats_completed_of_total, completedTodayCount, totalHabits)
            )

            if (habits.value.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.stats_habits_completed_today),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val completedHabits = habits.value.filter { habit ->
                    completionState.value[habit.id] == true
                }

                if (completedHabits.isEmpty()) {
                    Text(
                        text = stringResource(R.string.stats_no_habit_completed_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        completedHabits.take(3).forEach { habit ->
                            TopHabitItem(habitName = habit.name, habitIcon = habit.icon)
                        }
                    }
                }
            }

            if (habits.value.isEmpty()) {
                Text(
                    text = stringResource(R.string.stats_no_habits_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.xl)
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Radius.md))
            .padding(Spacing.lg)
    ) {
        Column {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun TopHabitItem(
    habitName: String,
    habitIcon: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Radius.md))
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = habitIcon, style = MaterialTheme.typography.titleMedium)
            Text(
                text = habitName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "✓",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}