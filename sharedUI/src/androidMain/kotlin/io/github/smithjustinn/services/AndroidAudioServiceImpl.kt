package io.github.smithjustinn.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import memory_match.sharedui.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

@Inject
class AndroidAudioServiceImpl(
    private val context: Context,
    private val logger: Logger,
    settingsRepository: SettingsRepository
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundMap = ConcurrentHashMap<String, Int>()
    private val loadedSounds = ConcurrentHashMap.newKeySet<Int>()
    private var isSoundEnabled = true

    init {
        // Observe sound settings
        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }

        // Pre-load all sounds
        scope.launch {
            listOf("flip.m4a", "match.m4a", "mismatch.m4a", "win.m4a", "click.m4a", "deal.m4a").forEach { path ->
                loadSound(path)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadSound(path: String): Int? {
        return try {
            val bytes = Res.readBytes("files/$path")
            val tempFile = File(context.cacheDir, path)
            withContext(Dispatchers.IO) {
                FileOutputStream(tempFile).use { it.write(bytes) }
            }
            val id = soundPool.load(tempFile.absolutePath, 1)
            soundMap[path] = id
            id
        } catch (e: Exception) {
            logger.e(e) { "Error loading sound: $path" }
            null
        }
    }

    private fun playSound(path: String) {
        if (!isSoundEnabled) return

        val soundId = soundMap[path]
        if (soundId != null && loadedSounds.contains(soundId)) {
            val streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            if (streamId == 0) {
                playFallback(path)
            }
        } else {
            playFallback(path)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun playFallback(path: String) {
        scope.launch {
            try {
                val tempFile = File(context.cacheDir, path)
                if (tempFile.exists()) {
                    MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        setOnCompletionListener { release() }
                        start()
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Error playing fallback sound: $path" }
            }
        }
    }

    override fun playFlip() = playSound("flip.m4a")
    override fun playMatch() = playSound("match.m4a")
    override fun playMismatch() = playSound("mismatch.m4a")
    override fun playWin() = playSound("win.m4a")
    override fun playClick() = playSound("click.m4a")
    override fun playDeal() = playSound("deal.m4a")
}
