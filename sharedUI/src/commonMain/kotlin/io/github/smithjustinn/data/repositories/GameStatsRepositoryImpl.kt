package io.github.smithjustinn.data.repositories

import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.GameStatsEntity
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
class GameStatsRepositoryImpl(
    private val dao: GameStatsDao
) : GameStatsRepository {
    override fun getStatsForDifficulty(pairCount: Int): Flow<GameStats?> = 
        dao.getStatsForDifficulty(pairCount).map { it?.toDomain() }

    override suspend fun updateStats(stats: GameStats) = 
        dao.insertStats(stats.toEntity())

    private fun GameStatsEntity.toDomain(): GameStats = GameStats(
        pairCount = pairCount,
        bestScore = bestScore,
        bestTimeSeconds = bestTimeSeconds
    )

    private fun GameStats.toEntity(): GameStatsEntity = GameStatsEntity(
        pairCount = pairCount,
        bestScore = bestScore,
        bestTimeSeconds = bestTimeSeconds
    )
}
