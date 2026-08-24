package com.app.habitus.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.habitus.R
import com.app.habitus.data.models.Habit
import com.app.habitus.ui.components.HabitCard
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        HeaderSection()

        if (habits.value.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                // El espacio para la barra de navegación inferior ya lo
                // reserva el padding del Scaffold aplicado en NavGraph;
                // aquí solo dejamos un pequeño margen extra de scroll.
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
private fun HeaderSection() {
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
        style = MaterialTheme.typography.headlineSmall,
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
        modifier = Modifier.padding(top = 2.dp)
    )

    Text(
        text = SimpleDateFormat("HH:mm", locale).format(currentDateTime),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 14.dp)
    )
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.empty_state_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}