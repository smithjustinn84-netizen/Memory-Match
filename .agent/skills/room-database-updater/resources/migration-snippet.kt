val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("YOUR SQL HERE")
    }
}
