package io.github.smithjustinn.domain.repositories

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val isPeekEnabled: StateFlow<Boolean>
    val isHiddenBoardEnabled: StateFlow<Boolean>
    val movesBeforeShuffle: StateFlow<Int>
    
    suspend fun setPeekEnabled(enabled: Boolean)
    suspend fun setHiddenBoardEnabled(enabled: Boolean)
    suspend fun setMovesBeforeShuffle(moves: Int)
}
