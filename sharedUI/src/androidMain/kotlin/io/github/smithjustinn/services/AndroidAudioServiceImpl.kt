package io.github.smithjustinn.services

import android.content.Context
import android.media.MediaPlayer
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.R

@Inject
class AndroidAudioServiceImpl(
    private val context: Context
) : AudioService {

    private fun playSound(resId: Int) {
        try {
            MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            // Log or ignore
        }
    }

    override fun playFlip() {
        playSound(R.raw.flip)
    }

    override fun playMatch() {
        playSound(R.raw.match)
    }

    override fun playMismatch() {
        playSound(R.raw.mismatch)
    }

    override fun playWin() {
        playSound(R.raw.win)
    }

    override fun playClick() {
        playSound(R.raw.click)
    }
}
