package io.github.smithjustinn.domain.models

/**
 * Represents the three stages of "The High Roller Circuit".
 */
enum class CircuitStage(
    val id: Int,
    val pairCount: Int,
    val bustPenalty: Double,
    val potGrowthMultiplier: Int,
) {
    QUALIFIER(id = 1, pairCount = 6, bustPenalty = 0.1, potGrowthMultiplier = 1),
    SEMI_FINAL(id = 2, pairCount = 10, bustPenalty = 0.25, potGrowthMultiplier = 2),
    GRAND_FINALE(id = 3, pairCount = 12, bustPenalty = 0.5, potGrowthMultiplier = 5),
    ;

    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: QUALIFIER
    }
}
