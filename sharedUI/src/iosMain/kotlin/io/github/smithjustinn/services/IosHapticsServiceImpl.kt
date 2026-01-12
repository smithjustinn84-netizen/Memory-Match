package io.github.smithjustinn.services

import dev.zacsweers.metro.Inject
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

@Inject
class IosHapticsServiceImpl : HapticsService {
    override fun vibrateMatch() {
        val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
        generator.prepare()
        generator.impactOccurred()
    }

    override fun vibrateMismatch() {
        val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
        generator.prepare()
        generator.impactOccurred()
    }
}
