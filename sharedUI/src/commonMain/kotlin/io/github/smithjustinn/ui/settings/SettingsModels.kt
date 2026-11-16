package io.github.smithjustinn.ui.settings

data class SettingsUIState(
    val isPeekEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isWalkthroughCompleted: Boolean = false,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 1.0f,
    val areSuitsMultiColored: Boolean = false,
    val isFourColorUnlocked: Boolean = false,
    val isThirdEyeEnabled: Boolean = false,
    val isThirdEyeUnlocked: Boolean = false,
    val isHeatShieldEnabled: Boolean = false,
    val isHeatShieldUnlocked: Boolean = false,
    val cardBacks: List<CosmeticOption> = emptyList(),
    val symbolStyles: List<CosmeticOption> = emptyList(),
)

/** A card back or symbol style the player owns and can switch to from settings. */
data class CosmeticOption(
    val id: String,
    val name: String,
    val hexColor: String?,
    val isSelected: Boolean,
)

sealed class SettingsUiEvent {
    data object PlayClick : SettingsUiEvent()
}
