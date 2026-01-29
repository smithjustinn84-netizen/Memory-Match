package io.github.smithjustinn.services

import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.audio_click
import io.github.smithjustinn.resources.audio_deal
import io.github.smithjustinn.resources.audio_flip
import io.github.smithjustinn.resources.audio_highscore
import io.github.smithjustinn.resources.audio_lose
import io.github.smithjustinn.resources.audio_match
import io.github.smithjustinn.resources.audio_mismatch
import io.github.smithjustinn.resources.audio_music
import io.github.smithjustinn.resources.audio_nuts
import io.github.smithjustinn.resources.audio_plink
import io.github.smithjustinn.resources.audio_win
import org.jetbrains.compose.resources.StringResource

interface AudioService {
    fun playEffect(effect: SoundEffect)

    fun startMusic()

    fun stopMusic()

    enum class SoundEffect {
        FLIP,
        MATCH,
        MISMATCH,
        THE_NUTS,
        WIN,
        LOSE,
        HIGH_SCORE,
        CLICK,
        DEAL,
        PLINK,
    }

    companion object {
        fun SoundEffect.toResource(): StringResource =
            when (this) {
                SoundEffect.FLIP -> Res.string.audio_flip

                SoundEffect.MATCH -> Res.string.audio_match

                SoundEffect.MISMATCH -> Res.string.audio_mismatch

                SoundEffect.THE_NUTS -> Res.string.audio_nuts

                // Placeholder until custom sound added
                SoundEffect.WIN -> Res.string.audio_win

                SoundEffect.LOSE -> Res.string.audio_lose

                SoundEffect.HIGH_SCORE -> Res.string.audio_highscore

                SoundEffect.CLICK -> Res.string.audio_click

                SoundEffect.DEAL -> Res.string.audio_deal

                SoundEffect.PLINK -> Res.string.audio_plink
            }

        val MUSIC: StringResource = Res.string.audio_music
    }
}
