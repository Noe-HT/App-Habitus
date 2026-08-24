package com.app.habitus.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.app.habitus.data.models.Habit
import com.app.habitus.data.models.HabitLog

/**
 * Acceso a datos de Room para hábitos y sus registros de cumplimiento
 * ([HabitLog]). No contiene lógica de negocio: eso vive en [HabitRepository].
 */
@Dao
interface HabitDao {

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits ORDER BY createdDate DESC")
    fun getAllHabits(): LiveData<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: Int): LiveData<Habit>

    @Insert
    suspend fun insertHabitLog(log: HabitLog)

    @Update
    suspend fun updateHabitLog(log: HabitLog)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getHabitLogByDate(habitId: Int, date: Long): HabitLog?

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND completed = 1")
    suspend fun getCompletedCount(habitId: Int): Int

    /** Últimos 30 registros de un hábito; usada solo para el % de cumplimiento de los últimos 30 días. */
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC LIMIT 30")
    suspend fun getLast30Days(habitId: Int): List<HabitLog>

    /**
     * Consulta independiente para el cálculo de racha: a diferencia de
     * [getLast30Days] (pensada solo para el % de los últimos 30 días), aquí
     * no queremos un límite tan corto o una racha real de más de 30 días
     * se quedaría congelada en 30 por falta de datos. 400 días (~13 meses)
     * cubre cualquier racha realista sin dejar la consulta sin límite.
     */
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND completed = 1 ORDER BY date DESC LIMIT 400")
    suspend fun getCompletedLogsForStreak(habitId: Int): List<HabitLog>
}