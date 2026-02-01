package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.comment_all_in
import io.github.smithjustinn.resources.comment_bad_beat
import io.github.smithjustinn.resources.comment_boom
import io.github.smithjustinn.resources.comment_eagle_eyes
import io.github.smithjustinn.resources.comment_full_house
import io.github.smithjustinn.resources.comment_great_find
import io.github.smithjustinn.resources.comment_high_roller
import io.github.smithjustinn.resources.comment_on_a_roll
import io.github.smithjustinn.resources.comment_one_more
import io.github.smithjustinn.resources.comment_perfect
import io.github.smithjustinn.resources.comment_photographic
import io.github.smithjustinn.resources.comment_pot_odds
import io.github.smithjustinn.resources.comment_sharp
import io.github.smithjustinn.resources.comment_the_nuts
import io.github.smithjustinn.resources.comment_you_got_it
import kotlinx.collections.immutable.persistentListOf

/**
 * Generates comments based on game events.
 */
object GameCommentGenerator {
    private const val ONE_MORE_REMAINING = 1

    fun generateMatchComment(
        moves: Int,
        matchesFound: Int,
        totalPairs: Int,
        combo: Int,
        config: ScoringConfig,
    ): MatchComment {
        if (matchesFound == totalPairs) return MatchComment(Res.string.comment_perfect)

        return when {
            combo > config.theNutsThreshold -> {
                MatchComment(Res.string.comment_the_nuts, persistentListOf(combo))
            }

            combo > config.highRollerThreshold -> {
                MatchComment(Res.string.comment_high_roller, persistentListOf(combo))
            }

            matchesFound == 1 -> {
                MatchComment(Res.string.comment_all_in)
            }

            matchesFound == totalPairs / config.commentPotOddsDivisor -> {
                MatchComment(Res.string.comment_pot_odds)
            }

            moves <= matchesFound * config.commentMovesPerMatchThreshold -> {
                MatchComment(Res.string.comment_photographic)
            }

            matchesFound == totalPairs - ONE_MORE_REMAINING -> {
                MatchComment(Res.string.comment_one_more)
            }

            else -> {
                val randomRes =
                    listOf(
                        Res.string.comment_great_find,
                        Res.string.comment_you_got_it,
                        Res.string.comment_boom,
                        Res.string.comment_eagle_eyes,
                        Res.string.comment_sharp,
                        Res.string.comment_on_a_roll,
                        Res.string.comment_full_house,
                        Res.string.comment_bad_beat,
                    ).random()
                MatchComment(randomRes)
            }
        }
    }
}
