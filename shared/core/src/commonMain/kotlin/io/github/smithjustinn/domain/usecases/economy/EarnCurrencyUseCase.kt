package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository

class EarnCurrencyUseCase(
    private val repository: PlayerEconomyRepository,
) {
    suspend operator fun invoke(amount: Long) {
        if (amount > 0) {
            repository.addCurrency(amount)
        }
    }
}
