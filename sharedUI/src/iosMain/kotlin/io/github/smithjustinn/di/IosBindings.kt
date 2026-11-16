package io.github.smithjustinn.di

import dev.zacsweers.metro.Binds
import io.github.smithjustinn.data.repositories.DailyChallengeRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStateRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStatsRepositoryImpl
import io.github.smithjustinn.data.repositories.JsonShopItemRepository
import io.github.smithjustinn.data.repositories.LeaderboardRepositoryImpl
import io.github.smithjustinn.data.repositories.PlayerEconomyRepositoryImpl
import io.github.smithjustinn.data.repositories.SettingsRepositoryImpl
import io.github.smithjustinn.domain.repositories.DailyChallengeRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.repositories.ShopItemRepository
import io.github.smithjustinn.domain.usecases.economy.DefaultEarnCurrencyUseCase
import io.github.smithjustinn.domain.usecases.economy.EarnCurrencyUseCase

/**
 * Explicit bindings for the iOS graph.
 *
 * Metro cannot aggregate `@ContributesBinding` declarations from other modules on
 * Native targets before Kotlin 2.3.20, so the bindings that Android and JVM pick up
 * automatically are declared here by hand. Every `@ContributesBinding(AppScope::class)`
 * class needs an entry. Delete this file once the project is on Kotlin 2.3.20+.
 */
interface IosBindings {
    @Binds
    val DailyChallengeRepositoryImpl.bindDailyChallengeRepository: DailyChallengeRepository

    @Binds
    val GameStateRepositoryImpl.bindGameStateRepository: GameStateRepository

    @Binds
    val GameStatsRepositoryImpl.bindGameStatsRepository: GameStatsRepository

    @Binds
    val JsonShopItemRepository.bindShopItemRepository: ShopItemRepository

    @Binds
    val LeaderboardRepositoryImpl.bindLeaderboardRepository: LeaderboardRepository

    @Binds
    val PlayerEconomyRepositoryImpl.bindPlayerEconomyRepository: PlayerEconomyRepository

    @Binds
    val SettingsRepositoryImpl.bindSettingsRepository: SettingsRepository

    @Binds
    val DefaultEarnCurrencyUseCase.bindEarnCurrencyUseCase: EarnCurrencyUseCase
}
