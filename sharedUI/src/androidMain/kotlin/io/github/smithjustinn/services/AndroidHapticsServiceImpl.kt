package io.github.smithjustinn.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dev.zacsweers.metro.Inject

@Inject
class AndroidHapticsServiceImpl(
    private val context: Context
) : HapticsService {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun vibrateMatch() {
        vibrate(longArrayOf(0, 50), intArrayOf(0, 255))
    }

    override fun vibrateMismatch() {
        vibrate(longArrayOf(0, 100, 50, 100), intArrayOf(0, 255, 0, 255))
    }

    private fun vibrate(timings: LongArray, amplitudes: IntArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings.sum())
        }
    }
}
