package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraph
import io.github.smithjustinn.screens.DifficultyScreenModel
import io.github.smithjustinn.screens.GameScreenModel
import io.github.smithjustinn.screens.StatsScreenModel
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.IosHapticsServiceImpl
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.AppDatabaseConstructor
import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.LeaderboardDao
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.data.repositories.GameStatsRepositoryImpl
import io.github.smithjustinn.data.repositories.LeaderboardRepositoryImpl
import platform.Foundation.NSHomeDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@DependencyGraph
interface IosAppGraph : AppGraph {
    override val difficultyScreenModel: DifficultyScreenModel
    override val gameScreenModel: GameScreenModel
    override val statsScreenModel: StatsScreenModel
    override val hapticsService: HapticsService
    override val gameStatsRepository: GameStatsRepository
    override val leaderboardRepository: LeaderboardRepository

    @Provides
    fun provideHapticsService(impl: IosHapticsServiceImpl): HapticsService = impl

    @Provides
    fun provideDatabase(): AppDatabase {
        val dbFile = NSHomeDirectory() + "/memory_match.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = { AppDatabaseConstructor.initialize() }
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .build()
    }

    @Provides
    fun provideGameStatsDao(database: AppDatabase): GameStatsDao = database.gameStatsDao()

    @Provides
    fun provideLeaderboardDao(database: AppDatabase): LeaderboardDao = database.leaderboardDao()

    @Provides
    fun provideGameStatsRepository(impl: GameStatsRepositoryImpl): GameStatsRepository = impl

    @Provides
    fun provideLeaderboardRepository(impl: LeaderboardRepositoryImpl): LeaderboardRepository = impl
}

fun createIosGraph(): AppGraph = createGraph<IosAppGraph>()
