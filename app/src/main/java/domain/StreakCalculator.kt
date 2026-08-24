package com.app.habitus.domain

/**
 * Calcula la racha de días consecutivos completados a partir de un
 * conjunto de fechas (normalizadas a inicio de día, en millis).
 *
 * Se extrae como función pura, sin dependencias de Room ni de Android,
 * precisamente para poder testearla con un test unitario JVM normal
 * (sin emulador ni Robolectric).
 *
 * Reglas:
 * - Si `today` está en `completedDates`, cuenta hacia atrás desde hoy.
 * - Si `today` NO está, no se considera la racha rota todavía (el día
 *   no ha terminado): se cuenta hacia atrás desde ayer en su lugar.
 * - Se detiene en el primer día sin completar.
 * - `maxLookback` evita un bucle infinito en caso de datos corruptos;
 *   400 días (~13 meses) cubre cualquier racha realista.
 */
object StreakCalculator {

    private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

    fun calculate(
        completedDates: Set<Long>,
        today: Long,
        maxLookback: Int = 400
    ): Int {
        var streak = 0
        var checkDate = today

        if (!completedDates.contains(checkDate)) {
            checkDate -= DAY_IN_MILLIS
        }

        repeat(maxLookback) {
            if (completedDates.contains(checkDate)) {
                streak++
                checkDate -= DAY_IN_MILLIS
            } else {
                return streak
            }
        }

        return streak
    }
}