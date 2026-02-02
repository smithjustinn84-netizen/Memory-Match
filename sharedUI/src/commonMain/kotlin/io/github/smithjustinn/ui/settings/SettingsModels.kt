package io.github.smithjustinn.ui.settings

data class SettingsUIState(
    val isPeekEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isWalkthroughCompleted: Boolean = false,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 1.0f,
)

sealed class SettingsUiEvent {
    data object PlayClick : SettingsUiEvent()
}
