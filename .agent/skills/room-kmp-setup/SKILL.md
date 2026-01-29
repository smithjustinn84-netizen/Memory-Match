---
name: room-kmp-setup
description: Sets up Room in a Kotlin Multiplatform project. Use when you need to add local persistence to a KMP project using Room.
---

# Room KMP Setup Skill

This skill provides a comprehensive guide for integrating Room into a Kotlin Multiplatform project.

## Workflow

### 1. Add Plugins and KSP
In your data module's `build.gradle.kts`, add the following plugins:
```kotlin
plugins {
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}
```

### 2. Configure Room and KSP
Configure the Room schema directory and add the compiler to the KSP dependencies for each target:
```kotlin
room {
    schemaDirectory("\$projectDir/schemas")
}

dependencies {
    with(libs.room.compiler) {
        add("kspAndroid", this)
        add("kspJvm", this)
        add("kspIosX64", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }
}
```

### 3. Create the Database Class
Create an abstract class that extends `RoomDatabase`. Use `@ConstructedBy` for the compiler to generate the implementation.

```kotlin
@Database(entities = [MyEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myDao(): MyDao
}

// The Room compiler generates the actual implementation.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
```

### 4. Implement Migrations
Use `SQLiteConnection.execSQL` for multiplatform migrations:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE my_table ADD COLUMN new_col INTEGER NOT NULL DEFAULT 0")
    }
}
```

## Resources
- [AppDatabase.kt Template](file:///Users/justinsmith/.gemini/antigravity/skills/room-kmp-setup/resources/AppDatabase.kt.template)
- [build.gradle.kts Snippet](file:///Users/justinsmith/.gemini/antigravity/skills/room-kmp-setup/resources/build.gradle.kts.snippet)
