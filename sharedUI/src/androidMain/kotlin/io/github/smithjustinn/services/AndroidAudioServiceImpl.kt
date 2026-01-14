package io.github.smithjustinn.services

import android.content.Context
import android.media.MediaPlayer
import dev.zacsweers.metro.Inject

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
        val resId = context.resources.getIdentifier("flip", "raw", context.packageName)
        if (resId != 0) playSound(resId)
    }

    override fun playMatch() {
        val resId = context.resources.getIdentifier("match", "raw", context.packageName)
        if (resId != 0) playSound(resId)
    }

    override fun playMismatch() {
        val resId = context.resources.getIdentifier("mismatch", "raw", context.packageName)
        if (resId != 0) playSound(resId)
    }

    override fun playWin() {
        val resId = context.resources.getIdentifier("win", "raw", context.packageName)
        if (resId != 0) playSound(resId)
    }
}
