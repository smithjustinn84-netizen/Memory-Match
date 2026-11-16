package io.github.smithjustinn.services

import io.github.smithjustinn.domain.services.AdService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop (JVM) no-op implementation of AdService.
 * Rewarded ads are not supported on desktop.
 */
class DesktopAdService : AdService {
    override val isAdLoaded: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    override fun loadRewardedAd(adUnitId: String) {
        // No-op: Desktop doesn't support ads
    }

    override fun showRewardedAd(onRewardEarned: (Int) -> Unit) {
        // No-op: Desktop doesn't support ads
    }
}
