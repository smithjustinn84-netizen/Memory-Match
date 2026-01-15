package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import memory_match.sharedui.generated.resources.Res
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Inject
class IosAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val players = mutableMapOf<String, AVAudioPlayer>()
    private var isSoundEnabled = true

    init {
        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        scope.launch {
            val sounds = listOf("flip.m4a", "match.m4a", "mismatch.m4a", "win.m4a", "click.m4a", "deal.m4a")
            sounds.forEach { path ->
                try {
                    val bytes = Res.readBytes("files/$path")
                    val data = bytes.usePinned { pinned ->
                        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                    }
                    val player = AVAudioPlayer(data = data, error = null)
                    player.prepareToPlay()
                    players[path] = player
                } catch (e: Exception) {
                    logger.e(e) { "Error pre-loading sound: $path" }
                }
            }
        }
    }

    private fun playSound(path: String) {
        if (!isSoundEnabled) return

        val player = players[path] ?: return
        if (player.playing) {
            player.stop()
            player.currentTime = 0.0
        }
        player.play()
    }

    override fun playFlip() = playSound("flip.m4a")
    override fun playMatch() = playSound("match.m4a")
    override fun playMismatch() = playSound("mismatch.m4a")
    override fun playWin() = playSound("win.m4a")
    override fun playClick() = playSound("click.m4a")
    override fun playDeal() = playSound("deal.m4a")
}
