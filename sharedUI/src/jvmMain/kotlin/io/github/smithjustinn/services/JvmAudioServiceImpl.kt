package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import memory_match.sharedui.generated.resources.Res
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

@Inject
class JvmAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val clips = ConcurrentHashMap<String, Clip>()
    private var isSoundEnabled = true

    init {
        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        scope.launch {
            val sounds = listOf("flip.wav", "match.wav", "mismatch.wav", "win.wav", "click.wav", "deal.wav")
            sounds.forEach { path ->
                try {
                    val bytes = Res.readBytes("files/$path")
                    val inputStream = ByteArrayInputStream(bytes)
                    val audioStream = AudioSystem.getAudioInputStream(BufferedInputStream(inputStream))
                    val clip = AudioSystem.getClip()
                    clip.open(audioStream)
                    clips[path] = clip
                } catch (e: Exception) {
                    logger.e(e) { "Error pre-loading sound: $path" }
                }
            }
        }
    }

    private fun playSound(path: String) {
        if (!isSoundEnabled) return

        val clip = clips[path] ?: return
        if (clip.isRunning) {
            clip.stop()
        }
        clip.framePosition = 0
        clip.start()
    }

    override fun playFlip() = playSound("flip.wav")
    override fun playMatch() = playSound("match.wav")
    override fun playMismatch() = playSound("mismatch.wav")
    override fun playWin() = playSound("win.wav")
    override fun playClick() = playSound("click.wav")
    override fun playDeal() = playSound("deal.wav")
}
