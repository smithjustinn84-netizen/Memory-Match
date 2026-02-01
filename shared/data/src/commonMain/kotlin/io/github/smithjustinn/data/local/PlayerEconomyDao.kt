package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerEconomyDao {
    @Query("SELECT * FROM player_economy WHERE id = 0")
    fun getPlayerEconomy(): Flow<PlayerEconomyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerEconomy(entity: PlayerEconomyEntity)
}
