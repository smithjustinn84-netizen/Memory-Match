package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.LeaderboardDao
import io.github.smithjustinn.data.local.LeaderboardEntity
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


@Inject
class LeaderboardRepositoryImpl(
    private val dao: LeaderboardDao,
    private val logger: Logger
) : LeaderboardRepository {
    override fun getTopEntries(pairCount: Int): Flow<List<LeaderboardEntry>> =
        dao.getTopEntries(pairCount)
            .map { entities ->
                entities.map { it.toDomain() }
            }
            .catch { e ->
                logger.e(e) { "Error fetching leaderboard for difficulty: $pairCount" }
                emit(emptyList())
            }

    override suspend fun addEntry(entry: LeaderboardEntry) {
        try {
            dao.insertEntry(entry.toEntity())
        } catch (e: Exception) {
            logger.e(e) { "Error adding leaderboard entry: $entry" }
        }
    }

    private fun LeaderboardEntity.toDomain(): LeaderboardEntry = LeaderboardEntry(
        id = id,
        pairCount = pairCount,
        score = score,
        timeSeconds = timeSeconds,
        moves = moves,
        timestamp = timestamp
    )

    private fun LeaderboardEntry.toEntity(): LeaderboardEntity = LeaderboardEntity(
        id = id,
        pairCount = pairCount,
        score = score,
        timeSeconds = timeSeconds,
        moves = moves,
        timestamp = timestamp
    )
}
