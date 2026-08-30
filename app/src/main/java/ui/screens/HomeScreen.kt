package com.app.habitus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.habitus.R
import com.app.habitus.data.models.Habit
import com.app.habitus.ui.components.HabitCard
import com.app.habitus.ui.theme.Radius
import com.app.habitus.ui.theme.Spacing
import com.app.habitus.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HabitViewModel = viewModel(),
    onAddHabitClick: () -> Unit = {},
    onHabitClick: (Int) -> Unit = {}
) {
    val habits = viewModel.allHabits.observeAsState(emptyList())
    val completionState = viewModel.habitCompletionState.observeAsState(emptyMap())
    val streaks = viewModel.habitStreaks.observeAsState(emptyMap())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(habits.value) {
        if (habits.value.isNotEmpty()) {
            viewModel.loadHabitsState(habits.value)
        }
    }

    if (showDeleteDialog && habitToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                habitToDelete = null
            },
            title = {
                Text(text = stringResource(R.string.delete_habit_title))
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_habit_confirm, habitToDelete?.name ?: "")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        habitToDelete?.let(viewModel::deleteHabit)
                        showDeleteDialog = false
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        habitToDelete = null
                    }
                ) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    val completedToday = habits.value.count { completionState.value[it.id] == true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
    ) {
        HeaderSection(
            totalHabits = habits.value.size,
            completedToday = completedToday
        )

        if (habits.value.isEmpty()) {
            EmptyState(onAddHabitClick = onAddHabitClick)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                // El espacio para la barra de navegación inferior ya lo
                // reserva el padding del Scaffold aplicado en NavGraph;
                // aquí solo dejamos un pequeño margen extra de scroll.
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = habits.value,
                    key = { habit -> habit.id }
                ) { habit ->
                    HabitCard(
                        habit = habit,
                        isMarked = completionState.value[habit.id] == true,
                        streakDays = streaks.value[habit.id] ?: 0,
                        onHabitClick = { onHabitClick(habit.id) },
                        onMarkClick = { viewModel.completeHabit(habit.id) },
                        onDeleteClick = {
                            habitToDelete = habit
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(totalHabits: Int, completedToday: Int) {
    val locale = Locale.getDefault()

    // El reloj se mantiene aislado en su propio composable: solo este bloque
    // se recompone al pasar el tiempo, no toda la pantalla de inicio. Como
    // solo se muestran minutos, basta con refrescar cada minuto en vez de
    // cada segundo, reduciendo drásticamente las recomposiciones.
    var currentDateTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = Date()
            delay(60_000L)
        }
    }

    Text(
        text = stringResource(R.string.home_title),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface
    )

    val formattedMonth = SimpleDateFormat("d MMMM", locale).format(currentDateTime)
    // "20 agosto" es el estilo correcto en español (minúscula); en otros
    // idiomas (p. ej. inglés, "20 August") no se debe forzar a minúsculas.
    val displayDate = if (locale.language == "es") formattedMonth.lowercase(locale) else formattedMonth

    Text(
        text = stringResource(R.string.home_today_prefix, displayDate),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp, bottom = Spacing.lg)
    )

    // Resumen de progreso del día: antes esta información solo vivía en la
    // pestaña de Progreso; ahora es lo primero que se ve al abrir la app,
    // que es justo lo que el usuario quiere saber en los primeros segundos.
    if (totalHabits > 0) {
        TodayProgressCard(totalHabits = totalHabits, completedToday = completedToday)
        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}

@Composable
private fun TodayProgressCard(totalHabits: Int, completedToday: Int) {
    val progress = if (totalHabits == 0) 0f else completedToday / totalHabits.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "todayProgress")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_completed_of_total, completedToday, totalHabits),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Barra de progreso propia (no la de Material) para controlar
            // exactamente grosor y color, consistente con el resto del
            // sistema visual.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                        RoundedCornerShape(Radius.pill)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(Radius.pill))
                ) {}
            }
        }
    }
}

@Composable
private fun EmptyState(onAddHabitClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌱", style = MaterialTheme.typography.displaySmall)
            }

            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.lg)
            )

            Text(
                text = stringResource(R.string.empty_state_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onAddHabitClick,
                shape = RoundedCornerShape(Radius.pill),
                modifier = Modifier.padding(top = Spacing.lg)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.action_create_habit),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}