package com.app.habitus.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val icon: String = "📝", // NUEVO: Icono/emoji del hábito
    val color: String,
    val durationHours: Int = 0,
    val durationMinutes: Int = 30,
    val reminderTime: String = "08:00",
    val createdDate: Long = System.currentTimeMillis()
)