package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Mutators that modify the game rules for Daily Challenges.
 */
@Serializable
enum class DailyChallengeMutator {
    /** Cards stay face-up for only half the usual time. */
    BLACKOUT,

    /** Every 5 moves, two unmatched cards on the board swap positions. */
    MIRAGE,
}
