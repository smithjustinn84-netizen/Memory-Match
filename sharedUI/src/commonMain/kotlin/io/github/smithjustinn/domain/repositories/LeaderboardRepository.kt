package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getTopEntries(pairCount: Int): Flow<List<LeaderboardEntry>>
    suspend fun addEntry(entry: LeaderboardEntry)
}
