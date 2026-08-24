package com.app.habitus.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.habitus.R
import com.app.habitus.data.models.Habit
import com.app.habitus.ui.theme.AmberHabit
import com.app.habitus.ui.theme.BlueHabit
import com.app.habitus.ui.theme.GreenHabit
import com.app.habitus.ui.theme.PurpleHabit
import com.app.habitus.viewmodel.HabitViewModel
import kotlinx.coroutines.launch
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitId: Int? = null,
    viewModel: HabitViewModel = viewModel(),
    onHabitSaved: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val existingHabit = habitId?.let { viewModel.getHabitById(it) }?.observeAsState()
    var initialized by remember { mutableStateOf(false) }
    val reminderCardBg = MaterialTheme.colorScheme.surfaceVariant
    val reminderCardBorder = MaterialTheme.colorScheme.outline
    val reminderSecondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(GreenHabit) }
    var selectedIcon by remember { mutableStateOf("📝") }
    var reminderTime by remember { mutableStateOf("08:00") }
    var isSaving by remember { mutableStateOf(false) }
    val durationHours = remember { mutableStateOf("0") }
    val durationMinutes = remember { mutableStateOf("30") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = listOf(GreenHabit, BlueHabit, PurpleHabit, AmberHabit)
    val habitIcons = listOf("🏃", "📚", "🧘", "💪", "🧠", "🎵", "🍎", "💤", "⛹️", "🎯", "📝", "🌿")

    val errorHabitNameRequired = stringResource(R.string.error_habit_name_required)
    val msgHabitAdded = stringResource(R.string.msg_habit_added)
    val msgHabitUpdated = stringResource(R.string.msg_habit_updated)


    LaunchedEffect(existingHabit?.value) {
        val habit = existingHabit?.value
        if (habit != null && !initialized) {
            habitName = habit.name
            habitDescription = habit.description
            reminderTime = habit.reminderTime
            selectedColor = hexToColor(habit.color)
            selectedIcon = habit.icon
            durationHours.value = habit.durationHours.toString()
            durationMinutes.value = habit.durationMinutes.toString()
            initialized = true
        }
    }

    /**
     * Abre el selector de hora nativo de Android, precargado con la hora
     * actual del recordatorio (o 08:00 por defecto).
     */
    fun showTimePickerDialog() {
        val parts = reminderTime.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            context,
            { _, hour, minute ->
                reminderTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    hour,
                    minute
                )
            },
            initialHour,
            initialMinute,
            true // Formato 24 horas
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (habitId == null) R.string.add_habit_title_new else R.string.add_habit_title_edit))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text(stringResource(R.string.label_habit_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = habitDescription,
                onValueChange = { habitDescription = it },
                label = { Text(stringResource(R.string.label_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text(
                text = stringResource(R.string.label_icon),
                style = MaterialTheme.typography.bodyMedium
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                habitIcons.chunked(4).forEach { iconRow ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconRow.forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedIcon == icon) MaterialTheme.colorScheme.surfaceVariant else surfaceColor
                                    )
                                    .border(
                                        width = if (selectedIcon == icon) 3.dp else 1.dp,
                                        color = if (selectedIcon == icon) onSurfaceColor else reminderCardBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedIcon = icon },
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 28.sp)
                            }
                        }

                        repeat(4 - iconRow.size) {
                            Box(modifier = Modifier.size(50.dp))
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.label_duration),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = durationHours.value,
                    onValueChange = { durationHours.value = it.filter(Char::isDigit).take(2) },
                    label = { Text(stringResource(R.string.label_hours)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = durationMinutes.value,
                    onValueChange = { durationMinutes.value = it.filter(Char::isDigit).take(2) },
                    label = { Text(stringResource(R.string.label_minutes)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Text(
                text = stringResource(R.string.label_color),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { color ->
                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { selectedColor = color }
                            .border(
                                width = if (selectedColor == color) 3.dp else 0.dp,
                                color = onSurfaceColor,
                                shape = CircleShape
                            ),
                        shape = CircleShape
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                        ) {}
                    }
                }
            }

            // Sección de recordatorio
            Text(
                text = stringResource(R.string.label_reminder_time),
                style = MaterialTheme.typography.bodyMedium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePickerDialog() },
                shape = RoundedCornerShape(8.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = reminderCardBg
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, reminderCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕒",
                        fontSize = 20.sp
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.select_time),
                            style = MaterialTheme.typography.bodySmall,
                            color = reminderSecondaryText
                        )

                        Text(
                            text = reminderTime,
                            style = MaterialTheme.typography.bodyLarge,
                            color = onSurfaceColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Botón de guardar / validación
            Button(
                onClick = {
                    if (habitName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(errorHabitNameRequired)
                        }
                        return@Button
                    }

                    val parsedHours = durationHours.value.toIntOrNull() ?: 0
                    val parsedMinutes = durationMinutes.value.toIntOrNull() ?: 30
                    val normalizedReminder = reminderTime.ifBlank { "08:00" }

                    val habitToSave = Habit(
                        id = habitId ?: 0,
                        name = habitName.trim(),
                        description = habitDescription.trim(),
                        icon = selectedIcon,
                        durationHours = parsedHours,
                        durationMinutes = parsedMinutes,
                        color = colorToHex(selectedColor),
                        reminderTime = normalizedReminder,
                        createdDate = existingHabit?.value?.createdDate ?: System.currentTimeMillis()
                    )

                    isSaving = true

                    if (habitId == null) {
                        viewModel.insertHabit(habitToSave)
                    } else {
                        viewModel.updateHabit(habit = habitToSave)
                    }

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (habitId == null) msgHabitAdded else msgHabitUpdated
                        )
                        onHabitSaved()
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp)
            ) {
                Text(stringResource(if (habitId == null) R.string.action_create_habit else R.string.action_save_changes))
            }
        }
    }
}

fun colorToHex(color: Color): String {
    return String.format(
        "#%02x%02x%02x",
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )
}

private fun hexToColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        GreenHabit
    }
}