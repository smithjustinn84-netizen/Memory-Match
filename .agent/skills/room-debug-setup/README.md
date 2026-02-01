# Room Debug Setup Skill

This skill helps you configure Room database for **debugging mode** (destructive migration) or **production mode** (manual migrations) in Kotlin Multiplatform projects.

## Quick Start

### Switching to Debug Mode (Current Configuration)

Your project is currently in **Debug Mode**, which means:
- ✅ Database schema changes automatically wipe and recreate tables
- ✅ No need to write migration code during development
- ✅ Faster iteration on database schema
- ⚠️ **All data is lost** when schema changes

### Switching to Production Mode

When you're ready to release your app, use this skill to switch to production mode:

```
Use the room-debug-setup skill to switch to production mode
```

This will:
- Enable schema export for version tracking
- Require manual migrations for schema changes
- Preserve user data between app updates

## What This Skill Does

The `room-debug-setup` skill automates the process of:

1. **Identifying** your current Room configuration
2. **Updating** the `@Database` annotation settings
3. **Modifying** all platform-specific database builders (Android, iOS, JVM)
4. **Managing** schema export configuration in `build.gradle.kts`
5. **Verifying** the changes compile correctly

## When to Use

| Scenario                                       | Mode       | Command                                |
| ---------------------------------------------- | ---------- | -------------------------------------- |
| **Development** - Iterating on database schema | Debug      | Already configured ✓                   |
| **Pre-Release** - Testing with real data       | Production | Ask agent to switch to production mode |
| **Production** - App released to users         | Production | Ask agent to switch to production mode |

## Examples

### Example 1: Switch to Production Mode
```
I'm ready to release the app. Use the room-debug-setup skill to configure Room for production.
```

### Example 2: Back to Debug Mode
```
I need to make more schema changes. Use the room-debug-setup skill to switch back to debug mode.
```

### Example 3: Check Current Mode
```
What Room mode am I currently in? Check the database configuration.
```

## Files Modified by This Skill

When you use this skill, it will modify:

- `shared/data/src/commonMain/kotlin/.../AppDatabase.kt` - Database definition
- `shared/data/build.gradle.kts` - Schema export configuration
- `sharedUI/src/androidMain/kotlin/.../AndroidUiModule.kt` - Android database builder
- `sharedUI/src/iosMain/kotlin/.../IosUiModule.kt` - iOS database builder
- `sharedUI/src/jvmMain/kotlin/.../JvmUiModule.kt` - JVM database builder

## Important Notes

> [!WARNING]
> **Debug Mode will WIPE all existing data** when the database version changes. Only use during development.

> [!IMPORTANT]
> Before releasing to production, **always switch to Production Mode** to preserve user data.

## See Also

- **Main Skill Documentation**: [SKILL.md](./SKILL.md)
- **Debug Mode Example**: [examples/debug-mode-example.kt](./examples/debug-mode-example.kt)
- **Production Mode Example**: [examples/production-mode-example.kt](./examples/production-mode-example.kt)

## Related Skills

- `room-kmp-setup` - Initial Room setup in KMP projects
- `room-database-updater` - Updating Room database schema with migrations
