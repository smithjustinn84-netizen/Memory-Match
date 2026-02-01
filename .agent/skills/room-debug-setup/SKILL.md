---
name: room-debug-setup
description: Configures Room database for debugging mode (destructive migration) or production mode (manual migrations) in KMP projects.
---

# Room Debug Setup Skill

This skill helps you toggle between **development/debugging mode** and **production mode** for Room database in Kotlin Multiplatform projects.

## When to Use This Skill

- **Development Phase**: When you want to iterate quickly on database schema without writing migrations.
- **Pre-Release Testing**: When you need to reset the database on every schema change.
- **Production Preparation**: When you're ready to ship and need to re-enable schema export and manual migrations.

## Prerequisites

- Room database already set up in a KMP project
- Database builder configuration in platform-specific DI modules

## Workflow

### Step 1: Identify Current Configuration

Search for the following files:
1. Database definition file (usually `AppDatabase.kt` with `@Database` annotation)
2. Database builder locations (search for `Room.databaseBuilder` or `Room.inMemoryDatabaseBuilder`)
3. Build configuration (search for `room { schemaDirectory(...) }` in `build.gradle.kts`)

### Step 2: Determine Mode

Ask the user which mode they want:
- **Debug Mode**: Destructive migration, no schema export
- **Production Mode**: Manual migrations, schema export enabled

### Step 3: Apply Debug Mode Configuration

If the user wants **Debug Mode**:

#### 3.1 Update Database Definition
In the `@Database` annotation:
- Set `exportSchema = false`
- Optionally reset `version = 1` (only if no production users exist)
- Remove or comment out all `Migration` objects in the companion object

Example:
```kotlin
@Database(
    entities = [/* ... */],
    version = 1, // Reset to 1 for clean slate
    exportSchema = false, // Disable schema export
)
```

#### 3.2 Update Build Configuration
In the data module's `build.gradle.kts`:
- Remove or comment out the `room { schemaDirectory(...) }` block

#### 3.3 Update Database Builders
In all platform-specific DI modules (Android, iOS, JVM):
- Replace `.addMigrations(...)` with `.fallbackToDestructiveMigration(dropAllTables = true)`

Example:
```kotlin
Room.databaseBuilder<AppDatabase>(...)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .fallbackToDestructiveMigration(dropAllTables = true) // Add this
    .build()
```

#### 3.4 Clean Up Schema Files
- Delete the `schemas/` directory in the data module (if it exists)
- Delete the `build/intermediates/room/schemas/` directory (if it exists)

### Step 4: Apply Production Mode Configuration

If the user wants **Production Mode**:

#### 4.1 Update Database Definition
In the `@Database` annotation:
- Set `exportSchema = true`
- Set appropriate `version` number
- Add `Migration` objects for all version transitions

Example:
```kotlin
@Database(
    entities = [/* ... */],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_VERSION = 2
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE ...")
            }
        }
    }
}
```

#### 4.2 Update Build Configuration
In the data module's `build.gradle.kts`:
- Add the `room { schemaDirectory(...) }` block

```kotlin
room {
    schemaDirectory("$projectDir/schemas")
}
```

#### 4.3 Update Database Builders
In all platform-specific DI modules:
- Replace `.fallbackToDestructiveMigration(...)` with `.addMigrations(...)`

Example:
```kotlin
Room.databaseBuilder<AppDatabase>(...)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .addMigrations(
        AppDatabase.MIGRATION_1_2,
        // Add all migrations
    )
    .build()
```

### Step 5: Verify Configuration

Run compilation checks to ensure no broken references:
```bash
./gradlew :shared:data:compileCommonMainKotlinMetadata
./gradlew :sharedUI:compileKotlinJvm
```

### Step 6: Document the Change

Create a note in the project (e.g., in `README.md` or a `DEVELOPMENT.md` file) indicating:
- Current database mode (Debug or Production)
- How to toggle between modes
- Warning about data loss when switching to debug mode

## Important Notes

> [!WARNING]
> **Debug Mode will WIPE all existing data** when the database version changes or schema is modified. Only use this mode during development.

> [!IMPORTANT]
> Before releasing to production, **always switch back to Production Mode** and write proper migrations.

> [!TIP]
> Keep a backup of your migration code even in debug mode. You can comment it out instead of deleting it.

## Common Issues

### Issue: "Migration didn't properly handle X"
**Solution**: You're likely in production mode. Switch to debug mode to skip migrations during development.

### Issue: "Schema export directory not found"
**Solution**: Create the directory or disable schema export in debug mode.

### Issue: "Data is being wiped unexpectedly"
**Solution**: Check if you're in debug mode with `fallbackToDestructiveMigration` enabled.

## Related Skills

- `room-kmp-setup`: Initial Room setup in KMP projects
- `room-database-updater`: Updating Room database schema with migrations
