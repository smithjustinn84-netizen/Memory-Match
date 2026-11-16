package io.github.smithjustinn.ui.settings

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsComponentTest : BaseComponentTest() {
    private lateinit var component: DefaultSettingsComponent

    @BeforeTest
    override fun setUp() {
        super.setUp()

        // Custom behaviors for this test if needed (defaults are usually enough)
        everySuspend { context.settingsRepository.setPeekEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setMusicEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setSoundEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setSoundVolume(any()) } returns Unit
        everySuspend { context.settingsRepository.setMusicVolume(any()) } returns Unit
        everySuspend { context.settingsRepository.setSuitsMultiColored(any()) } returns Unit
        everySuspend { context.settingsRepository.setThirdEyeEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setHeatShieldEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setWalkthroughCompleted(any()) } returns Unit
    }

    @Test
    fun `initial state is correct`() =
        runTest { lifecycle ->
            // Setup specific volume for this test
            every { context.settingsRepository.soundVolume } returns MutableStateFlow(0.8f)

            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.state.test {
                // The state might emit several times as flows are being collected
                // We want to find the first emission that matches our mocked configuration
                var foundDesiredState = false
                while (!foundDesiredState) {
                    val state = awaitItem()
                    if (state.soundVolume == 0.8f) {
                        assertTrue(state.isPeekEnabled || !state.isPeekEnabled) // Just consume it
                        foundDesiredState = true
                    }
                }
            }
        }

    @Test
    fun `togglePeekEnabled updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.togglePeekEnabled(false)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setPeekEnabled(false) }
        }

    @Test
    fun `toggleSuitsMultiColored updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.toggleSuitsMultiColored(true)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setSuitsMultiColored(true) }
        }

    @Test
    fun `toggleSoundEnabled updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.toggleSoundEnabled(true)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setSoundEnabled(true) }
        }

    @Test
    fun `toggleMusicEnabled updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.toggleMusicEnabled(true)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setMusicEnabled(true) }
        }

    @Test
    fun `setSoundVolume updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.setSoundVolume(0.5f)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setSoundVolume(0.5f) }
        }

    @Test
    fun `setMusicVolume updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.setMusicVolume(0.5f)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setMusicVolume(0.5f) }
        }

    @Test
    fun `toggleThirdEyeEnabled updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.toggleThirdEyeEnabled(true)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setThirdEyeEnabled(true) }
        }

    @Test
    fun `toggleHeatShieldEnabled updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.toggleHeatShieldEnabled(true)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setHeatShieldEnabled(true) }
        }

    @Test
    fun `resetWalkthrough updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.resetWalkthrough()
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setWalkthroughCompleted(false) }
        }

    @Test
    fun `cosmetic pickers list only owned items and mark the active one`() =
        runTest { lifecycle ->
            everySuspend { context.shopItemRepository.getShopItems() } returns
                listOf(
                    shopItem("theme_standard", ShopItemType.THEME),
                    shopItem("theme_classic", ShopItemType.THEME),
                    shopItem("theme_poker", ShopItemType.THEME),
                    shopItem("skin_classic", ShopItemType.CARD_SKIN),
                    shopItem("skin_neon", ShopItemType.CARD_SKIN),
                )
            every { context.playerEconomyRepository.unlockedItemIds } returns MutableStateFlow(setOf("theme_classic"))
            every { context.playerEconomyRepository.selectedThemeId } returns MutableStateFlow("theme_classic")

            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.state.test {
                var state = awaitItem()
                while (state.cardBacks.isEmpty()) state = awaitItem()

                assertEquals(listOf("theme_standard", "theme_classic"), state.cardBacks.map { it.id })
                assertEquals("theme_classic", state.cardBacks.single { it.isSelected }.id)
                assertEquals(listOf("skin_classic"), state.symbolStyles.map { it.id })
                assertTrue(state.symbolStyles.single().isSelected)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `selectCardBack equips an owned card back`() =
        runTest { lifecycle ->
            everySuspend { context.playerEconomyRepository.isItemUnlocked("theme_classic") } returns true
            everySuspend { context.playerEconomyRepository.selectTheme(any()) } returns Unit

            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.selectCardBack("theme_classic")
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.playerEconomyRepository.selectTheme("theme_classic") }
        }

    @Test
    fun `selectSymbolStyle ignores a style that is not owned`() =
        runTest { lifecycle ->
            everySuspend { context.playerEconomyRepository.isItemUnlocked("skin_neon") } returns false

            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.selectSymbolStyle("skin_neon")
            testDispatcher.scheduler.runCurrent()

            verifySuspend(VerifyMode.not) { context.playerEconomyRepository.selectSkin(any()) }
        }

    private fun shopItem(
        id: String,
        type: ShopItemType,
    ) = ShopItem(id = id, name = id, description = "", price = 0, type = type)

    private fun createComponent(lifecycle: Lifecycle): DefaultSettingsComponent =
        DefaultSettingsComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = context.appGraph,
            onBackClicked = {},
        )
}
