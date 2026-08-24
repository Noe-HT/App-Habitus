package com.app.habitus.data.local

import android.content.Context
import androidx.lifecycle.LiveData
import com.app.habitus.data.models.Habit
import com.app.habitus.data.models.HabitLog
import com.app.habitus.domain.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Fuente única de datos de hábitos para el resto de la app.
 *
 * Envuelve el [HabitDao] y expone operaciones de más alto nivel (racha,
 * porcentaje de cumplimiento, etc.) para que la capa de ViewModel no tenga
 * que conocer detalles de Room ni de cómo se calculan estas métricas.
 * Todas las operaciones de escritura/lectura pesada se ejecutan en
 * [Dispatchers.IO].
 */
class HabitRepository(context: Context) {

    private val habitDao = AppDatabase.getDatabase(context).habitDao()

    val allHabits: LiveData<List<Habit>> = habitDao.getAllHabits()

    /** Inserta un hábito nuevo y devuelve el id autogenerado por Room. */
    suspend fun insertHabit(habit: Habit): Long {
        return withContext(Dispatchers.IO) {
            habitDao.insertHabit(habit)
        }
    }

    suspend fun updateHabit(habit: Habit) {
        withContext(Dispatchers.IO) {
            habitDao.updateHabit(habit)
        }
    }

    suspend fun deleteHabit(habit: Habit) {
        withContext(Dispatchers.IO) {
            habitDao.deleteHabit(habit)
        }
    }

    fun getHabitById(habitId: Int): LiveData<Habit> {
        return habitDao.getHabitById(habitId)
    }

    /**
     * Marca un hábito como completado hoy. Es idempotente: si ya existe un
     * registro para hoy, no crea uno duplicado.
     */
    suspend fun completeHabitToday(habitId: Int) {
        withContext(Dispatchers.IO) {
            val todayMillis = getStartOfTodayMillis()
            val existingLog = habitDao.getHabitLogByDate(habitId, todayMillis)

            if (existingLog == null) {
                habitDao.insertHabitLog(
                    HabitLog(
                        habitId = habitId,
                        date = todayMillis,
                        completed = true
                    )
                )
            } else if (!existingLog.completed) {
                habitDao.updateHabitLog(existingLog.copy(completed = true))
            }
        }
    }

    suspend fun isHabitCompletedToday(habitId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val todayMillis = getStartOfTodayMillis()
            habitDao.getHabitLogByDate(habitId, todayMillis)?.completed == true
        }
    }

    /**
     * Calcula la racha actual de días consecutivos completados.
     *
     * Incluye un periodo de gracia: si hoy todavía no se ha completado el
     * hábito, no se considera rota la racha (el día no ha terminado) y se
     * cuenta desde ayer en su lugar. Usa [HabitDao.getCompletedLogsForStreak],
     * que no está limitada a 30 registros como [HabitDao.getLast30Days], para
     * que rachas largas no se congelen artificialmente en 30 días.
     */
    suspend fun getHabitStreak(habitId: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                val logs = habitDao.getCompletedLogsForStreak(habitId)
                val completedDates = logs
                    .map { normalizeDate(it.date) }
                    .toSet()

                StreakCalculator.calculate(
                    completedDates = completedDates,
                    today = getStartOfTodayMillis()
                )
            } catch (_: Exception) {
                0
            }
        }
    }

    /** Porcentaje de días completados de entre los últimos 30. */
    suspend fun getCompletionPercentage(habitId: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                val logs = habitDao.getLast30Days(habitId)
                if (logs.isEmpty()) return@withContext 0
                val completed = logs.count { it.completed }
                (completed * 100) / logs.size
            } catch (_: Exception) {
                0
            }
        }
    }

    /** Número total histórico de días completados (sean o no consecutivos). */
    suspend fun getTotalCompletedDays(habitId: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                habitDao.getCompletedCount(habitId)
            } catch (_: Exception) {
                0
            }
        }
    }

    private fun getStartOfTodayMillis(): Long {
        return Calendar.getInstance().run {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }
    }

    private fun normalizeDate(date: Long): Long {
        return Calendar.getInstance().run {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }
    }

    companion object {
        private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    }
}