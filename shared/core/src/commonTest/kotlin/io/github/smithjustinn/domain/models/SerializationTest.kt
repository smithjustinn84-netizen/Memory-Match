package io.github.smithjustinn.domain.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SerializationTest {
    @Test
    fun testCardDisplaySettingsSerialization() {
        val settings =
            CardDisplaySettings(
                backTheme = CardBackTheme.POKER,
                symbolTheme = CardSymbolTheme.POKER,
                areSuitsMultiColored = true,
            )
        val json = Json.encodeToString(CardDisplaySettings.serializer(), settings)
        val decoded = Json.decodeFromString(CardDisplaySettings.serializer(), json)
        assertEquals(settings, decoded)
    }

    @Test
    fun testGameStatsSerialization() {
        val stats = GameStats(pairCount = 8, bestScore = 1500, bestTimeSeconds = 45)
        val json = Json.encodeToString(GameStats.serializer(), stats)
        val decoded = Json.decodeFromString(GameStats.serializer(), json)
        assertEquals(stats, decoded)
    }

    @Test
    fun testLeaderboardEntrySerialization() {
        val entry =
            LeaderboardEntry(
                id = 123,
                pairCount = 10,
                score = 2500,
                timeSeconds = 120,
                moves = 25,
                timestamp = Instant.fromEpochMilliseconds(123456789),
                gameMode = GameMode.DAILY_CHALLENGE,
            )
        val json = Json.encodeToString(LeaderboardEntry.serializer(), entry)
        val decoded = Json.decodeFromString(LeaderboardEntry.serializer(), json)
        assertEquals(entry, decoded)
    }

    @Test
    fun testDifficultyLevelSerialization() {
        val level = DifficultyLevel(type = DifficultyType.SHARK, pairs = 12)
        val json = Json.encodeToString(DifficultyLevel.serializer(), level)
        val decoded = Json.decodeFromString(DifficultyLevel.serializer(), json)
        assertEquals(level, decoded)
    }
}
