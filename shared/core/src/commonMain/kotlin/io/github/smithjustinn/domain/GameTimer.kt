package io.github.smithjustinn.domain

import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Encapsulates game timer logic, extracted from [GameStateMachine].
 */
class GameTimer(
    private val scope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val onTick: () -> Unit,
) {
    private var timerJob: Job? = null

    fun start() {
        stop()
        timerJob =
            scope.launch(dispatchers.default) {
                while (true) {
                    delay(TIMER_TICK_INTERVAL_MS)
                    onTick()
                }
            }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
    }

    companion object {
        const val TIMER_TICK_INTERVAL_MS = 1000L
    }
}
