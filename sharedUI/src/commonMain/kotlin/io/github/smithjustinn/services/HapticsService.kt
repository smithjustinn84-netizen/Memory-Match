package io.github.smithjustinn.services

enum class HapticFeedbackType {
    LIGHT,
    HEAVY,
    LONG_PRESS,
}

interface HapticsService {
    fun performHapticFeedback(type: HapticFeedbackType)

    fun vibrateMatch()

    fun vibrateMismatch()

    fun vibrateTick()

    fun vibrateWarning()

    fun vibrateHeat()
}
