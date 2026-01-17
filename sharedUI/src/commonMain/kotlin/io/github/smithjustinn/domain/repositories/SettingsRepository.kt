package io.github.smithjustinn.domain.repositories

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val isPeekEnabled: StateFlow<Boolean>
    val isSoundEnabled: StateFlow<Boolean>
    val isWalkthroughCompleted: StateFlow<Boolean>
    
    suspend fun setPeekEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setWalkthroughCompleted(completed: Boolean)
}
