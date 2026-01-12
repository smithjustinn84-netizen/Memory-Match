package io.github.smithjustinn.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(entities = [GameStatsEntity::class, LeaderboardEntity::class], version = 3)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `leaderboard` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `pairCount` INTEGER NOT NULL, `score` INTEGER NOT NULL, `timeSeconds` INTEGER NOT NULL, `moves` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                // No schema changes needed for switching to TypeConverter if the underlying type (INTEGER/Long) remains the same.
                // However, we increment the version to ensure the new TypeConverter logic is applied and to follow best practices.
            }
        }
    }
}

// The Room compiler generates the actual implementation.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
