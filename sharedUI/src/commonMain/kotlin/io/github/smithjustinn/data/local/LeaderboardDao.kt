package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard WHERE pairCount = :pairCount ORDER BY timeSeconds ASC, moves ASC LIMIT 10")
    fun getTopEntries(pairCount: Int): Flow<List<LeaderboardEntity>>

    @Insert
    suspend fun insertEntry(entry: LeaderboardEntity)
}
