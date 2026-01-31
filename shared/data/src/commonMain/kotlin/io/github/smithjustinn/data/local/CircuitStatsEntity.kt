package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "circuit_stats")
data class CircuitStatsEntity(
    @PrimaryKey val runId: String,
    val currentStageId: Int,
    val bankedScore: Int,
    val currentWager: Int,
    val timestamp: Long,
    val isActive: Boolean = true,
)
