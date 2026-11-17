package io.github.smithjustinn.services

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MusicFaderTest {
    private class FakeTrack : MusicTrack {
        var playing = false
        var released = false
        var level = -1f

        override fun play() {
            playing = true
        }

        override fun setVolume(volume: Float) {
            level = volume
        }

        override fun release() {
            released = true
            playing = false
        }
    }

    private class Harness(
        scope: TestScope,
    ) {
        val tracks = mutableListOf<FakeTrack>()
        val fader = MusicFader(scope) { FakeTrack().also { tracks += it } }
    }

    @Test
    fun fadeInEndsAtTargetVolume() =
        runTest {
            val h = Harness(this)
            h.fader.targetVolume = 0.8f
            h.fader.setPlaying(true)
            advanceTimeBy(MusicFader.DEFAULT_FADE_IN_MS / 2)
            val track = h.tracks.single()
            assertTrue(track.playing)
            assertTrue(track.level in 0.1f..0.7f, "mid-fade volume was ${track.level}")

            advanceUntilIdle()
            assertEquals(0.8f, track.level)
        }

    @Test
    fun stopFadesOutThenReleases() =
        runTest {
            val h = Harness(this)
            h.fader.setPlaying(true)
            advanceUntilIdle()
            h.fader.setPlaying(false)
            advanceTimeBy(MusicFader.DEFAULT_FADE_OUT_MS / 2)
            val track = h.tracks.single()
            assertFalse(track.released, "released before the fade finished")

            advanceUntilIdle()
            assertTrue(track.released)
        }

    @Test
    fun startDuringFadeOutKeepsTheSameTrackPlaying() =
        runTest {
            val h = Harness(this)
            h.fader.setPlaying(true)
            advanceUntilIdle()
            h.fader.setPlaying(false)
            advanceTimeBy(MusicFader.DEFAULT_FADE_OUT_MS / 2)
            h.fader.setPlaying(true)
            advanceUntilIdle()

            val track = h.tracks.single()
            assertFalse(track.released)
            assertTrue(track.playing)
            assertEquals(1f, track.level)
        }

    @Test
    fun stopBeforeTheTrackIsBuiltStillReleasesIt() =
        runTest {
            val h = Harness(this)
            h.fader.setPlaying(true)
            h.fader.setPlaying(false)
            advanceUntilIdle()

            assertTrue(h.tracks.all { it.released })
        }

    @Test
    fun volumeChangeAppliesImmediatelyOnceFadedIn() =
        runTest {
            val h = Harness(this)
            h.fader.setPlaying(true)
            advanceUntilIdle()
            h.fader.targetVolume = 0.3f
            assertEquals(0.3f, h.tracks.single().level)
        }
}
