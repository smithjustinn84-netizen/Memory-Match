package io.github.smithjustinn.ui.start

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.difficulty_casual
import io.github.smithjustinn.resources.difficulty_master
import io.github.smithjustinn.resources.difficulty_shark
import io.github.smithjustinn.resources.difficulty_tourist
import kotlin.test.Test
import kotlin.test.assertEquals

class DifficultyUIMapperTest {
    @Test
    fun testDisplayNameResMapping() {
        assertEquals(Res.string.difficulty_tourist, DifficultyType.TOURIST.displayNameRes)
        assertEquals(Res.string.difficulty_casual, DifficultyType.CASUAL.displayNameRes)
        assertEquals(Res.string.difficulty_master, DifficultyType.MASTER.displayNameRes)
        assertEquals(Res.string.difficulty_shark, DifficultyType.SHARK.displayNameRes)
    }
}
