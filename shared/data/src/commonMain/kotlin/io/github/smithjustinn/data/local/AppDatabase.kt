package io.github.smithjustinn.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [
        GameStatsEntity::class,
        LeaderboardEntity::class,
        GameStateEntity::class,
        SettingsEntity::class,
        DailyChallengeEntity::class,
        PlayerEconomyEntity::class,
    ],
    version = AppDatabase.DATABASE_VERSION,
    exportSchema = false,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao

    abstract fun leaderboardDao(): LeaderboardDao

    abstract fun gameStateDao(): GameStateDao

    abstract fun settingsDao(): SettingsDao

    abstract fun dailyChallengeDao(): DailyChallengeDao

    abstract fun playerEconomyDao(): PlayerEconomyDao

    @Suppress("MagicNumber")
    companion object {
        const val DATABASE_VERSION = 3

        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE player_economy ADD COLUMN selectedThemeId TEXT NOT NULL DEFAULT 'GEOMETRIC'",
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("DROP TABLE IF EXISTS `settings`")
                    connection.execSQL(
                        "CREATE TABLE IF NOT EXISTS `settings` (" +
                            "`id` INTEGER NOT NULL, " +
                            "`isPeekEnabled` INTEGER NOT NULL, " +
                            "`isSoundEnabled` INTEGER NOT NULL, " +
                            "`isMusicEnabled` INTEGER NOT NULL, " +
                            "`isWalkthroughCompleted` INTEGER NOT NULL, " +
                            "`soundVolume` REAL NOT NULL, " +
                            "`musicVolume` REAL NOT NULL, " +
                            "PRIMARY KEY(`id`))",
                    )
                }
            }
    }
}

// The Room compiler generates the actual implementation.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
