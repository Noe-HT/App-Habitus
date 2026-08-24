package com.app.habitus.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.app.habitus.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HabitReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val defaultHabitName = applicationContext.getString(R.string.default_habit_name)
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: defaultHabitName
        val habitId = inputData.getInt(KEY_HABIT_ID, 0)
        showNotification(habitId, habitName)
        return Result.success()
    }

    private fun showNotification(habitId: Int, habitName: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = applicationContext.getString(R.string.notification_channel_desc)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(applicationContext.getString(R.string.notification_text, habitName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Se usa el id del hábito (no el nombre) para que dos hábitos con el
        // mismo nombre no se sobrescriban la notificación entre sí.
        notificationManager.notify(habitId, notification)
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders_channel"
        const val KEY_HABIT_NAME = "habit_name"
        const val KEY_HABIT_ID = "habit_id"
    }
}

object NotificationScheduler {

    fun scheduleDailyReminder(
        context: Context,
        habitId: Int,
        habitName: String,
        reminderTime: String
    ) {
        val initialDelay = calculateInitialDelay(reminderTime)

        val data = Data.Builder()
            .putString(HabitReminderWorker.KEY_HABIT_NAME, habitName)
            .putInt(HabitReminderWorker.KEY_HABIT_ID, habitId)
            .build()

        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueWorkName(habitId),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelReminder(
        context: Context,
        habitId: Int
    ) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(uniqueWorkName(habitId))
    }

    private fun uniqueWorkName(habitId: Int): String {
        return "habit_reminder_$habitId"
    }

    private fun calculateInitialDelay(reminderTime: String): Long {
        val parts = reminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return target.timeInMillis - now.timeInMillis
    }
}