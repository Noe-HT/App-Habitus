package com.app.habitus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.habitus.data.local.HabitRepository
import com.app.habitus.data.models.Habit
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.app.habitus.notifications.NotificationScheduler

/**
 * ViewModel de la capa de hábitos, compartido por todas las pantallas
 * (Home, Detalle, Estadísticas). Expone el estado como [LiveData] y
 * delega toda la persistencia en [HabitRepository]; también coordina la
 * programación/cancelación de recordatorios vía [NotificationScheduler]
 * cuando un hábito se crea, edita o borra.
 */
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application.applicationContext)
    private val appContext = application.applicationContext

    val allHabits: LiveData<List<Habit>> = repository.allHabits

    private val _currentStreak = MutableLiveData(0)
    val currentStreak: LiveData<Int> = _currentStreak

    private val _completionPercentage = MutableLiveData(0)
    val completionPercentage: LiveData<Int> = _completionPercentage

    private val _totalCompletedDays = MutableLiveData(0)
    val totalCompletedDays: LiveData<Int> = _totalCompletedDays

    /** Si el hábito con id X está completado hoy. Se usa en la lista de Home. */
    private val _habitCompletionState = MutableLiveData<Map<Int, Boolean>>(emptyMap())
    val habitCompletionState: LiveData<Map<Int, Boolean>> = _habitCompletionState

    private val _habitStreaks = MutableLiveData<Map<Int, Int>>(emptyMap())
    val habitStreaks: LiveData<Map<Int, Int>> = _habitStreaks

    private val _habitPercentages = MutableLiveData<Map<Int, Int>>(emptyMap())
    val habitPercentages: LiveData<Map<Int, Int>> = _habitPercentages

    fun insertHabit(habit: Habit) = viewModelScope.launch {
        val newId = repository.insertHabit(habit).toInt()

        NotificationScheduler.scheduleDailyReminder(
            context = appContext,
            habitId = newId,
            habitName = habit.name,
            reminderTime = habit.reminderTime
        )
    }

    fun updateHabit(habit: Habit) = viewModelScope.launch {
        repository.updateHabit(habit)

        // El recordatorio se identifica por id, así que reprogramarlo con la
        // política UPDATE ya sustituye correctamente el anterior; no es
        // necesario cancelar por nombre.
        NotificationScheduler.scheduleDailyReminder(
            context = appContext,
            habitId = habit.id,
            habitName = habit.name,
            reminderTime = habit.reminderTime
        )
    }

    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        repository.deleteHabit(habit)
        NotificationScheduler.cancelReminder(appContext, habit.id)

        val completionMap = _habitCompletionState.value.orEmpty().toMutableMap()
        completionMap.remove(habit.id)
        _habitCompletionState.value = completionMap

        val streakMap = _habitStreaks.value.orEmpty().toMutableMap()
        streakMap.remove(habit.id)
        _habitStreaks.value = streakMap

        val percentageMap = _habitPercentages.value.orEmpty().toMutableMap()
        percentageMap.remove(habit.id)
        _habitPercentages.value = percentageMap
    }

    fun getHabitById(habitId: Int): LiveData<Habit> {
        return repository.getHabitById(habitId)
    }

    /** Marca un hábito como completado hoy y refresca su racha/porcentaje en caliente. */
    fun completeHabit(habitId: Int) = viewModelScope.launch {
        repository.completeHabitToday(habitId)

        val completionMap = _habitCompletionState.value.orEmpty().toMutableMap()
        completionMap[habitId] = true
        _habitCompletionState.value = completionMap

        val streakMap = _habitStreaks.value.orEmpty().toMutableMap()
        streakMap[habitId] = repository.getHabitStreak(habitId)
        _habitStreaks.value = streakMap

        val percentageMap = _habitPercentages.value.orEmpty().toMutableMap()
        percentageMap[habitId] = repository.getCompletionPercentage(habitId)
        _habitPercentages.value = percentageMap

        _currentStreak.value = streakMap[habitId] ?: 0
        _completionPercentage.value = percentageMap[habitId] ?: 0
    }

    fun getHabitStreak(habitId: Int) = viewModelScope.launch {
        _currentStreak.value = repository.getHabitStreak(habitId)
    }

    fun getCompletionPercentage(habitId: Int) = viewModelScope.launch {
        _completionPercentage.value = repository.getCompletionPercentage(habitId)
    }

    fun getTotalCompletedDays(habitId: Int) = viewModelScope.launch {
        _totalCompletedDays.value = repository.getTotalCompletedDays(habitId)
    }

    /**
     * Carga el estado de completado/racha/porcentaje de una lista de hábitos
     * de golpe (usada al entrar a Home o Estadísticas). Las consultas de
     * cada hábito se lanzan en paralelo para no multiplicar la latencia por
     * el número de hábitos.
     */
    fun loadHabitsState(habits: List<Habit>) = viewModelScope.launch {
        val completionMap = mutableMapOf<Int, Boolean>()
        val streakMap = mutableMapOf<Int, Int>()
        val percentageMap = mutableMapOf<Int, Int>()

        // Se consulta cada hábito en paralelo (en vez de uno a uno) para no
        // multiplicar la latencia por el número de hábitos.
        coroutineScope {
            habits.map { habit ->
                async {
                    val completed = repository.isHabitCompletedToday(habit.id)
                    val streak = repository.getHabitStreak(habit.id)
                    val percentage = repository.getCompletionPercentage(habit.id)
                    Triple(habit.id, completed, streak to percentage)
                }
            }.forEach { deferred ->
                val (habitId, completed, streakAndPercentage) = deferred.await()
                completionMap[habitId] = completed
                streakMap[habitId] = streakAndPercentage.first
                percentageMap[habitId] = streakAndPercentage.second
            }
        }

        _habitCompletionState.value = completionMap
        _habitStreaks.value = streakMap
        _habitPercentages.value = percentageMap
    }
}