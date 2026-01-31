package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CircuitStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCircuitStats(stats: CircuitStatsEntity)

    @Query("SELECT * FROM circuit_stats WHERE isActive = 1 LIMIT 1")
    fun getActiveCircuitRun(): Flow<CircuitStatsEntity?>

    @Query("UPDATE circuit_stats SET isActive = 0 WHERE runId = :runId")
    suspend fun deactivateRun(runId: String)

    @Query("DELETE FROM circuit_stats")
    suspend fun clearAll()
}
