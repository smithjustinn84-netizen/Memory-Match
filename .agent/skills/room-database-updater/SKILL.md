---
name: room-database-updater
description: Updates a Room database, including versioning and migrations. Use when you need to modify the database schema (add columns, tables, etc.).
---

# Room Database Updater Skill

This skill provides a step-by-step guide for updating an existing Room database and implementing multiplatform migrations.

## Workflow

### 1. Identify Schema Changes
Determine what changes are needed (e.g., adding a column to an entity, creating a new table).

### 2. Update Version Number
Increment the `DATABASE_VERSION` constant in your database class (e.g., `AppDatabase.kt`).

```kotlin
companion object {
    const val DATABASE_VERSION = 8 // Incremented from 7
    // ...
}
```

### 3. Create Migration Object
Define a new `Migration` object in the companion object of your database class. Use `SQLiteConnection.execSQL` for the migration logic.

```kotlin
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE my_table ADD COLUMN new_col TEXT NOT NULL DEFAULT ''")
    }
}
```

### 4. Register Migration in Platform Modules
Add the new migration object to the `addMigrations` call in each platform's UI module (e.g., `AndroidUiModule.kt`, `IosUiModule.kt`, `JvmUiModule.kt`).

```kotlin
.addMigrations(
    AppDatabase.MIGRATION_1_2,
    // ...
    AppDatabase.MIGRATION_6_7,
    AppDatabase.MIGRATION_7_8, // Add new migration here
)
```

### 5. Verify Migration
Always test the migration by running the app and ensuring existing data is preserved and the new schema is applied. Consider writing a Room migration test if the schema changes are complex.

## Resources
- [Migration Snippet](file:///Users/justinsmith/.gemini/antigravity/skills/room-database-updater/resources/migration-snippet.kt)
- [Migration Registry Snippet](file:///Users/justinsmith/.gemini/antigravity/skills/room-database-updater/resources/migration-registry.kt)

## Common Migration Commands
- **Add Column**: `ALTER TABLE table_name ADD COLUMN column_name TYPE NOT NULL DEFAULT default_value`
- **Create Table**: `CREATE TABLE IF NOT EXISTS table_name (id INTEGER PRIMARY KEY, ...)`
