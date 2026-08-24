package com.app.habitus.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests unitarios puros (JVM, sin Android) para [StreakCalculator].
 *
 * Se trabaja con "días" arbitrarios expresados como múltiplos de
 * [DAY] en lugar de fechas reales, ya que a la función solo le
 * importan las diferencias entre ellas, no el calendario real.
 */
class StreakCalculatorTest {

    private val DAY = 86_400_000L // 24h en millis, igual que en StreakCalculator

    @Test
    fun `sin ningun dia completado, la racha es 0`() {
        val today = 5 * DAY

        val streak = StreakCalculator.calculate(
            completedDates = emptySet(),
            today = today
        )

        assertEquals(0, streak)
    }

    @Test
    fun `hoy completado con dos dias previos consecutivos da racha de 3`() {
        val today = 3 * DAY
        val completed = setOf(3 * DAY, 2 * DAY, 1 * DAY)

        val streak = StreakCalculator.calculate(completed, today)

        assertEquals(3, streak)
    }

    @Test
    fun `si hoy aun no esta completado pero ayer si, no se resetea a 0 (periodo de gracia)`() {
        val today = 5 * DAY
        // Ayer (4*DAY) y anteayer (3*DAY) completados; hoy todavía no.
        val completed = setOf(4 * DAY, 3 * DAY)

        val streak = StreakCalculator.calculate(completed, today)

        assertEquals(2, streak)
    }

    @Test
    fun `si ni hoy ni ayer estan completados, la racha es realmente 0`() {
        val today = 5 * DAY
        // Solo un día suelto, hace tiempo; ni hoy ni ayer completados.
        val completed = setOf(2 * DAY)

        val streak = StreakCalculator.calculate(completed, today)

        assertEquals(0, streak)
    }

    @Test
    fun `un hueco en medio corta la racha en vez de saltarlo`() {
        val today = 4 * DAY
        // Completados: hoy y ayer. Falta anteayer (2*DAY). Hay un día
        // suelto más antiguo (1*DAY) que NO debe contarse porque hay
        // un hueco antes de llegar a él.
        val completed = setOf(4 * DAY, 3 * DAY, 1 * DAY)

        val streak = StreakCalculator.calculate(completed, today)

        assertEquals(2, streak)
    }

    @Test
    fun `una racha larga de 45 dias no se congela en 30`() {
        val today = 45 * DAY
        val completed = (1..45).map { it * DAY }.toSet()

        val streak = StreakCalculator.calculate(completed, today)

        assertEquals(45, streak)
    }

    @Test
    fun `maxLookback limita la racha aunque haya mas dias completados detras`() {
        val today = 10 * DAY
        val completed = (1..10).map { it * DAY }.toSet()

        val streak = StreakCalculator.calculate(
            completedDates = completed,
            today = today,
            maxLookback = 5
        )

        assertEquals(5, streak)
    }
}