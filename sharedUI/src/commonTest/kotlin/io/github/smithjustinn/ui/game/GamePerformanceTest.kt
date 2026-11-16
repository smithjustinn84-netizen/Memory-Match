package io.github.smithjustinn.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.AppTheme
import io.github.smithjustinn.ui.game.components.cards.ShimmerEffect
import io.github.smithjustinn.ui.game.components.grid.GridBackground
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FRAMES = 30
private const val FRAME_MS = 16L
private const val SETTLE_FRAMES = 3

/**
 * Guards the frame-time fixes on the game table: effects that animate for the whole game must
 * not make their neighbours redraw, and stored positions must not change while the table shakes.
 */
@OptIn(ExperimentalTestApi::class)
class GamePerformanceTest {
    @Test
    fun spotlightDoesNotRedrawItsSiblings() =
        runComposeUiTest {
            assertEquals(0, siblingDrawsWhileAnimating { GridBackground(modifier = Modifier.fillMaxSize()) })
        }

    @Test
    fun shimmerDoesNotRedrawItsSiblings() =
        runComposeUiTest {
            assertEquals(0, siblingDrawsWhileAnimating { ShimmerEffect() })
        }

    @Test
    fun tablePositionsIgnoreTheScreenShake() =
        runComposeUiTest {
            val shake = mutableFloatStateOf(0f)
            val rootPositions = mutableSetOf<Offset>()
            val tablePositions = mutableSetOf<Offset>()
            mainClock.autoAdvance = false
            setContent {
                val origin = remember { TableOrigin() }
                Box(Modifier.size(200.dp).graphicsLayer { translationX = shake.floatValue }) {
                    Box(Modifier.fillMaxSize().onPlaced { origin.coordinates = it }) {
                        Box(
                            Modifier.size(50.dp).onGloballyPositioned {
                                rootPositions += it.positionInRoot()
                                tablePositions += it.positionInTable(origin)
                            },
                        )
                    }
                }
            }
            repeat(FRAMES) { frame ->
                shake.floatValue = frame.toFloat()
                mainClock.advanceTimeBy(FRAME_MS)
            }

            // The shake does move the node on screen, which is what used to be stored...
            assertTrue(rootPositions.size > 1, "expected the shake to change root positions")
            // ...but its position on the table stays put.
            assertEquals(setOf(Offset.Zero), tablePositions)
        }

    /** Draws [content] next to a counting sibling under one shared layer, as on the game table. */
    private fun ComposeUiTest.siblingDrawsWhileAnimating(content: @Composable () -> Unit): Int {
        var siblingDraws = 0
        mainClock.autoAdvance = false
        setContent {
            AppTheme {
                Box(Modifier.size(200.dp).graphicsLayer { translationX = 0f }) {
                    Box(Modifier.fillMaxSize().drawBehind { siblingDraws++ })
                    content()
                }
            }
        }
        repeat(SETTLE_FRAMES) { mainClock.advanceTimeBy(FRAME_MS) }
        val settled = siblingDraws
        assertTrue(settled > 0, "the sibling never drew, so the test would prove nothing")
        repeat(FRAMES) { mainClock.advanceTimeBy(FRAME_MS) }
        return siblingDraws - settled
    }
}
