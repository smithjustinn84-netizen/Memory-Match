package io.github.smithjustinn.domain.services

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific service for loading and showing rewarded ads.
 */
interface AdService {
    /**
     * Observable state of whether an ad is currently loaded and ready to be shown.
     */
    val isAdLoaded: StateFlow<Boolean>

    /**
     * Loads a rewarded ad with the given ad unit ID.
     * @param adUnitId The platform-specific ad unit identifier
     */
    fun loadRewardedAd(adUnitId: String)

    /**
     * Shows the loaded rewarded ad.
     * @param onRewardEarned Callback invoked when the user earns the reward (bonus seconds)
     */
    fun showRewardedAd(onRewardEarned: (Int) -> Unit)
}
