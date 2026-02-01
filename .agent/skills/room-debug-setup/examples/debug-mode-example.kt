// Example: Room Database in Debug Mode

// 1. Database Definition (AppDatabase.kt)
@Database(
    entities = [
        GameStatsEntity::class,
        LeaderboardEntity::class,
        SettingsEntity::class,
    ],
    version = 1, // Reset to 1 for clean slate
    exportSchema = false, // Disable schema export
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_VERSION = 1
        // No migrations needed in debug mode
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
            .fallbackToDestructiveMigration(dropAllTables = true) // Debug mode
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
            .fallbackToDestructiveMigration(dropAllTables = true) // Debug mode
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
            .fallbackToDestructiveMigration(dropAllTables = true) // Debug mode
            .build()
    }
}

// 5. Build Configuration (build.gradle.kts)
// Comment out or remove the room block:
// room {
//     schemaDirectory("$projectDir/schemas")
// }
