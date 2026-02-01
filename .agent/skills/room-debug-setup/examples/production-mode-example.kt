// Example: Room Database in Production Mode

// 1. Database Definition (AppDatabase.kt)
@Database(
    entities = [
        GameStatsEntity::class,
        LeaderboardEntity::class,
        SettingsEntity::class,
    ],
    version = 3, // Incremented version
    exportSchema = true, // Enable schema export
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_VERSION = 3
        
        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE settings ADD COLUMN theme TEXT NOT NULL DEFAULT 'LIGHT'"
                )
            }
        }
        
        // Migration from version 2 to 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS leaderboard (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "playerName TEXT NOT NULL, " +
                        "score INTEGER NOT NULL)"
                )
            }
        }
    }
}

// 2. Android DI Module (AndroidUiModule.kt)
val androidUiModule = module {
    single<AppDatabase> {
        val context = get<Context>()
        val dbFile = context.getDatabasePath("app.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
            )
            .build()
    }
}

// 3. iOS DI Module (IosUiModule.kt)
val iosUiModule = module {
    single<AppDatabase> {
        val dbFile = NSHomeDirectory() + "/app.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = { AppDatabaseConstructor.initialize() },
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
            )
            .build()
    }
}

// 4. JVM DI Module (JvmUiModule.kt)
val jvmUiModule = module {
    single<AppDatabase> {
        val dbFile = File(System.getProperty("user.home"), ".app.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
            )
            .build()
    }
}

// 5. Build Configuration (build.gradle.kts)
room {
    schemaDirectory("$projectDir/schemas")
}

// 6. Schema files will be generated at:
// shared/data/schemas/1.json
// shared/data/schemas/2.json
// shared/data/schemas/3.json
