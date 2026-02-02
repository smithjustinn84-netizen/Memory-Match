package io.github.smithjustinn.services

class JvmHapticsServiceImpl : HapticsService {
    override fun performHapticFeedback(type: HapticFeedbackType) {
        // No-op for Desktop
    }

    override fun vibrateMatch() {
        // No-op for Desktop
    }

    override fun vibrateMismatch() {
        // No-op for Desktop
    }

    override fun vibrateTick() {
        // No-op for Desktop
    }

    override fun vibrateWarning() {
        // No-op for Desktop
    }

    override fun vibrateHeat() {
        // No-op for Desktop
    }
}
