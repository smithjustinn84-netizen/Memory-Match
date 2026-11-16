package io.github.smithjustinn.ui.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.models.ShopItemType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MS = 5000L
private const val UNLOCKED_INDEX = 5
private const val SELECTED_THEME_INDEX = 7
private const val SELECTED_SKIN_INDEX = 8
private const val SHOP_ITEMS_INDEX = 9

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    appGraph: AppGraph,
    private val onBackClicked: () -> Unit,
) : SettingsComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val settingsRepository = appGraph.settingsRepository
    private val playerEconomyRepository = appGraph.playerEconomyRepository
    private val setActiveCosmeticUseCase = appGraph.setActiveCosmeticUseCase
    private val shopItems = MutableStateFlow<List<ShopItem>>(emptyList())

    private val _events = Channel<SettingsUiEvent>(Channel.BUFFERED)
    override val events: Flow<SettingsUiEvent> = _events.receiveAsFlow()

    private val audioSettingsFlow =
        combine(
            settingsRepository.isSoundEnabled,
            settingsRepository.isMusicEnabled,
            settingsRepository.soundVolume,
            settingsRepository.musicVolume,
        ) { sound, music, soundVol, musicVol ->
            AudioSettings(sound, music, soundVol, musicVol)
        }

    override val state: StateFlow<SettingsState> =
        combine(
            settingsRepository.isPeekEnabled,
            settingsRepository.isWalkthroughCompleted,
            settingsRepository.areSuitsMultiColored,
            settingsRepository.isThirdEyeEnabled,
            settingsRepository.isHeatShieldEnabled,
            playerEconomyRepository.unlockedItemIds,
            audioSettingsFlow,
            playerEconomyRepository.selectedThemeId,
            playerEconomyRepository.selectedSkinId,
            shopItems,
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val unlocked = values[UNLOCKED_INDEX] as Set<String>

            @Suppress("UNCHECKED_CAST")
            val items = values[SHOP_ITEMS_INDEX] as List<ShopItem>
            SettingsState(
                cardBacks =
                    items.ownedOptions(
                        type = ShopItemType.THEME,
                        defaultId = CardBackTheme.GEOMETRIC.id,
                        unlocked = unlocked,
                        selectedId = values[SELECTED_THEME_INDEX] as String,
                    ),
                symbolStyles =
                    items.ownedOptions(
                        type = ShopItemType.CARD_SKIN,
                        defaultId = CardSymbolTheme.CLASSIC.id,
                        unlocked = unlocked,
                        selectedId = values[SELECTED_SKIN_INDEX] as String,
                    ),
                isPeekEnabled = values[0] as Boolean,
                isWalkthroughCompleted = values[1] as Boolean,
                areSuitsMultiColored = values[2] as Boolean,
                isThirdEyeEnabled = values[3] as Boolean,
                isHeatShieldEnabled = values[4] as Boolean,
                isFourColorUnlocked = unlocked.contains("feature_four_color_suits"),
                isThirdEyeUnlocked = unlocked.contains("feature_third_eye"),
                isHeatShieldUnlocked = unlocked.contains("feature_heat_shield"),
                isSoundEnabled = (values[6] as AudioSettings).isSoundEnabled,
                isMusicEnabled = (values[6] as AudioSettings).isMusicEnabled,
                soundVolume = (values[6] as AudioSettings).soundVolume,
                musicVolume = (values[6] as AudioSettings).musicVolume,
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = SettingsState(),
        )

    private data class AudioSettings(
        val isSoundEnabled: Boolean,
        val isMusicEnabled: Boolean,
        val soundVolume: Float,
        val musicVolume: Float,
    )

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch { shopItems.value = appGraph.shopItemRepository.getShopItems() }
    }

    override fun selectCardBack(id: String) = equip(id, ShopItemType.THEME)

    override fun selectSymbolStyle(id: String) = equip(id, ShopItemType.CARD_SKIN)

    private fun equip(
        id: String,
        type: ShopItemType,
    ) {
        scope.launch {
            // The use case rejects items the player does not own; settings only lists owned ones.
            runCatching { setActiveCosmeticUseCase(id, type) }
        }
    }

    override fun togglePeekEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setPeekEnabled(enabled)
        }
    }

    override fun toggleSoundEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    override fun toggleMusicEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }

    override fun setSoundVolume(volume: Float) {
        scope.launch {
            settingsRepository.setSoundVolume(volume)
        }
    }

    override fun setMusicVolume(volume: Float) {
        scope.launch {
            settingsRepository.setMusicVolume(volume)
        }
    }

    override fun toggleSuitsMultiColored(enabled: Boolean) {
        scope.launch {
            settingsRepository.setSuitsMultiColored(enabled)
        }
    }

    override fun toggleThirdEyeEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setThirdEyeEnabled(enabled)
        }
    }

    override fun toggleHeatShieldEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setHeatShieldEnabled(enabled)
        }
    }

    override fun resetWalkthrough() {
        scope.launch {
            settingsRepository.setWalkthroughCompleted(false)
        }
    }

    override fun onBack() {
        onBackClicked()
    }
}

private fun List<ShopItem>.ownedOptions(
    type: ShopItemType,
    defaultId: String,
    unlocked: Set<String>,
    selectedId: String,
): List<CosmeticOption> {
    val owned = filter { it.type == type && (it.id == defaultId || it.id in unlocked) }
    // A stored selection can be an enum name rather than an item id; fall back to the default then.
    val activeId = owned.firstOrNull { it.id == selectedId }?.id ?: defaultId
    return owned.map { CosmeticOption(it.id, it.name, it.hexColor, isSelected = it.id == activeId) }
}

// Rename the UI state class if it doesn't match the interface
typealias SettingsState = SettingsUIState
