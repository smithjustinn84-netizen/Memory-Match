package io.github.smithjustinn.services

enum class HapticFeedbackType {
    LIGHT,
    HEAVY,
}

interface HapticsService {
    fun performHapticFeedback(type: HapticFeedbackType)

    fun vibrateMatch()

    fun vibrateMismatch()

    fun vibrateTick()

    fun vibrateWarning()

    fun vibrateHeat()
}
