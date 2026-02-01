package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository

interface EarnCurrencyUseCase {
    suspend fun execute(amount: Long)
}

class DefaultEarnCurrencyUseCase(
    private val repository: PlayerEconomyRepository,
) : EarnCurrencyUseCase {
    override suspend fun execute(amount: Long) {
        if (amount > 0) {
            repository.addCurrency(amount)
        }
    }
}
