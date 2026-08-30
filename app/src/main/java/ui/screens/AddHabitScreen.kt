package com.app.habitus.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.habitus.R
import com.app.habitus.data.models.Habit
import com.app.habitus.ui.theme.AmberHabit
import com.app.habitus.ui.theme.BlueHabit
import com.app.habitus.ui.theme.CoralHabit
import com.app.habitus.ui.theme.GreenHabit
import com.app.habitus.ui.theme.PurpleHabit
import com.app.habitus.ui.theme.Radius
import com.app.habitus.ui.theme.RoseHabit
import com.app.habitus.ui.theme.Spacing
import com.app.habitus.ui.theme.SkyHabit
import com.app.habitus.ui.theme.TealHabit
import com.app.habitus.viewmodel.HabitViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Iconos disponibles para un hábito, agrupados por categoría para que se
 * puedan escanear de un vistazo en vez de recorrer una cuadrícula plana
 * de 28 elementos sin ningún punto de referencia.
 */
private data class IconCategory(val labelRes: Int, val icons: List<String>)

private val HABIT_ICON_CATEGORIES = listOf(
    IconCategory(R.string.icon_category_fitness, listOf("🏃", "🏋️", "🧘", "💪", "🚴", "🏊", "🚶", "⛹️")),
    IconCategory(R.string.icon_category_learning, listOf("📚", "🧠", "📝", "🎓", "💻", "🗣️")),
    IconCategory(R.string.icon_category_wellness, listOf("😴", "💧", "🥗", "🍎", "🌞")),
    IconCategory(R.string.icon_category_creativity, listOf("🎵", "🎨", "📷", "✍️")),
    IconCategory(R.string.icon_category_home_goals, listOf("🧹", "🌿", "🎯", "💰", "🙏"))
)

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
    val colors = listOf(
        GreenHabit, BlueHabit, PurpleHabit, AmberHabit,
        RoseHabit, TealHabit, CoralHabit, SkyHabit
    )

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
                reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
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
                .padding(horizontal = Spacing.lg)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // Vista previa en vivo: el icono y color elegidos se ven aquí
            // arriba, animados, antes incluso de rellenar el nombre. Le da
            // a la pantalla el "momento de color" que el resto de la app
            // ya tiene, y de paso el usuario ve su hábito antes de crearlo.
            HabitPreview(icon = selectedIcon, color = selectedColor)

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                TonalTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = stringResource(R.string.label_habit_name),
                    singleLine = true
                )

                TonalTextField(
                    value = habitDescription,
                    onValueChange = { habitDescription = it },
                    label = stringResource(R.string.label_description),
                    minLines = 3
                )
            }

            FormSection(icon = "", title = stringResource(R.string.label_icon_category)) {
                IconPicker(
                    categories = HABIT_ICON_CATEGORIES,
                    selectedIcon = selectedIcon,
                    accentColor = selectedColor,
                    onIconSelected = { selectedIcon = it }
                )
            }

            FormSection(icon = "⏱️", title = stringResource(R.string.label_duration)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    TonalTextField(
                        value = durationHours.value,
                        onValueChange = { durationHours.value = it.filter(Char::isDigit).take(2) },
                        label = stringResource(R.string.label_hours),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    TonalTextField(
                        value = durationMinutes.value,
                        onValueChange = { durationMinutes.value = it.filter(Char::isDigit).take(2) },
                        label = stringResource(R.string.label_minutes),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            FormSection(icon = "🎨", title = stringResource(R.string.label_color)) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    colors.chunked(4).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            rowColors.forEach { color ->
                                ColorSwatch(
                                    color = color,
                                    selected = selectedColor == color,
                                    onClick = { selectedColor = color }
                                )
                            }
                        }
                    }
                }
            }

            FormSection(icon = "⏰", title = stringResource(R.string.label_reminder_time)) {
                ReminderCard(
                    reminderTime = reminderTime,
                    onClick = { showTimePickerDialog() }
                )
            }

            Button(
                onClick = {
                    if (habitName.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar(errorHabitNameRequired) }
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
                shape = RoundedCornerShape(Radius.pill),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl)
            ) {
                Text(stringResource(if (habitId == null) R.string.action_create_habit else R.string.action_save_changes))
            }
        }
    }
}

/**
 * Círculo grande con el icono sobre el color elegidos, con transición
 * animada de color. Es la vista previa del hábito que se está creando.
 */
@Composable
private fun HabitPreview(icon: String, color: Color) {
    val animatedColor by animateColorAsState(targetValue = color, label = "habitPreviewColor")

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(animatedColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 40.sp)
        }
    }
}

/** Agrupa una sección del formulario bajo un título, con icono opcional. */
@Composable
private fun FormSection(icon: String? = null, title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!icon.isNullOrBlank()) {
                Text(text = icon, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = Spacing.xs)
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        content()
    }
}

/**
 * Campo de texto con fondo tonal suave en vez del borde fino por defecto
 * de Material — más coherente con el lenguaje de tarjetas del resto de
 * la app, menos "formulario web genérico".
 */
@Composable
private fun TonalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(Radius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun IconPicker(
    categories: List<IconCategory>,
    selectedIcon: String,
    accentColor: Color,
    onIconSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        categories.forEach { category ->
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = stringResource(category.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconRowGrid(
                    icons = category.icons,
                    selectedIcon = selectedIcon,
                    accentColor = accentColor,
                    onIconSelected = onIconSelected
                )
            }
        }
    }
}

@Composable
private fun IconRowGrid(
    icons: List<String>,
    selectedIcon: String,
    accentColor: Color,
    onIconSelected: (String) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val unselectedBorder = Color.Transparent

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        icons.chunked(4).forEach { iconRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                iconRow.forEach { icon ->
                    val isSelected = selectedIcon == icon
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) accentColor.copy(alpha = 0.18f) else surfaceColor,
                        label = "iconBg"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) accentColor else unselectedBorder,
                        label = "iconBorder"
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(bgColor, CircleShape)
                            .border(width = 2.dp, color = borderColor, shape = CircleShape)
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 22.sp)
                    }
                }

                repeat(4 - iconRow.size) {
                    Box(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color, CircleShape)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Tarjeta de recordatorio acentuada con el color cálido de la app: el
 * recordatorio es lo que impulsa al usuario a actuar cada día, así que
 * se le da el mismo tono de "energía" que a la racha.
 */
@Composable
private fun ReminderCard(reminderTime: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🕒", fontSize = 22.sp)

            Column {
                Text(
                    text = stringResource(R.string.select_time),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )

                Text(
                    text = reminderTime,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
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