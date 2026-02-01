package io.github.smithjustinn.ui.game.components.timer

data class TimerState(
    val time: Long,
    val isLowTime: Boolean,
    val isCriticalTime: Boolean,
)
