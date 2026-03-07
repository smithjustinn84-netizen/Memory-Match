package io.github.smithjustinn.services

import android.app.Activity
import co.touchlab.kermit.Logger
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.smithjustinn.domain.services.AdService

/**
 * Android implementation of AdService using Google Mobile Ads SDK.
 */
class AndroidAdService(
    private val activityProvider: ActivityProvider,
) : AdService {
    private val logger = Logger.withTag("AndroidAdService")
    private var rewardedAd: RewardedAd? = null

    private val _isAdLoaded = MutableStateFlow(false)
    override val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    override fun loadRewardedAd(adUnitId: String) {
        val activity = activityProvider.activity.value
        if (activity == null) {
            logger.w { "Failed to load rewarded ad: activity is null" }
            return
        }

        val adRequest = AdRequest.Builder().build()
        logger.i { "Loading rewarded ad with unit ID: $adUnitId" }
        RewardedAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    logger.i { "Rewarded ad loaded successfully" }
                    rewardedAd = ad
                    _isAdLoaded.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logger.w { "Failed to load rewarded ad: ${error.message} (code: ${error.code})" }
                    rewardedAd = null
                    _isAdLoaded.value = false
                }
            },
        )
    }

    override fun showRewardedAd(onRewardEarned: (Int) -> Unit) {
        val activity = activityProvider.activity.value
        if (activity == null) {
            logger.w { "Failed to show rewarded ad: activity is null" }
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            logger.w { "Failed to show rewarded ad: ad is not loaded" }
            return
        }

        logger.i { "Showing rewarded ad" }
        ad.show(activity) { rewardItem ->
            logger.i { "User earned reward: ${rewardItem.amount} ${rewardItem.type}" }
            onRewardEarned(rewardItem.amount)
            _isAdLoaded.value = false
            rewardedAd = null
        }
    }
}
