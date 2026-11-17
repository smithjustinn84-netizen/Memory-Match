package io.github.smithjustinn.services

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioPitchTest {
    @Test
    fun firstPairPlaysAtRecordedPitch() {
        assertEquals(1f, AudioPitch.comboRate(1))
        assertEquals(1f, AudioPitch.comboRate(0))
    }

    @Test
    fun comboRateClimbsThenHoldsAtAFifth() {
        val rates = (1..10).map(AudioPitch::comboRate)
        rates.zipWithNext().forEach { (a, b) -> assertTrue(b >= a, "rates must not fall: $rates") }
        assertTrue(rates[1] > rates[0])
        assertEquals(1.498f, rates.last(), 0.001f)
        assertEquals(rates[3], rates.last())
    }

    @Test
    fun jitterStaysWithinThreePercent() {
        val random = Random(42)
        repeat(1_000) {
            val rate = AudioPitch.jitter(random)
            assertTrue(rate in 0.97f..1.03f, "rate $rate out of range")
        }
    }
}
