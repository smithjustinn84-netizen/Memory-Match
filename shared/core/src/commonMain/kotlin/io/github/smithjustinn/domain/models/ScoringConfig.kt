package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Configuration for game scoring and bonuses.
 *
 * All point values and thresholds are tunable for balancing gameplay.
 */
@Serializable
data class ScoringConfig(
    /** Base points awarded for each successful card match. */
    val baseMatchPoints: Int = 100,
    /** Additional points per combo level (stacks with each consecutive match). */
    val comboBonusPoints: Int = 50,
    /** Bonus points per pair when completing the game quickly. */
    val timeBonusPerPair: Int = 50,
    /** Points deducted per second over the target completion time. */
    val timePenaltyPerSecond: Int = 1,
    /** Base value for move efficiency calculation (higher rewards fewer moves). */
    val moveBonusMultiplier: Int = 10000,
    /** Combo streak required to activate heat mode visual effects. */
    val heatModeThreshold: Int = 3,
    /** Points lost when failing a Double Down bet. */
    val doubleDownPenalty: Int = 500,
    /** Minimum combo streak for the "High Roller" achievement. */
    val highRollerThreshold: Int = 1,
    /** Combo streak required for "The Nuts" achievement (very difficult). */
    val theNutsThreshold: Int = 6,
) {
    init {
        require(baseMatchPoints > 0) { "Base match points must be positive" }
        require(heatModeThreshold > 0) { "Heat mode threshold must be positive" }
        require(theNutsThreshold >= highRollerThreshold) {
            "The Nuts threshold must be >= High Roller threshold"
        }
    }
}
