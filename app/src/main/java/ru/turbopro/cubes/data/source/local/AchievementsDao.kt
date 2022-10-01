package ru.turbopro.cubes.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.turbopro.cubes.data.Achievement

@Dao
interface AchievementsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListOfAchievements(events: List<Achievement>)

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<Achievement>

    @Query("SELECT * FROM achievements")
    fun observeAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE achievId = :achievementId")
    suspend fun getAchievementById(achievementId: String): Achievement?

    @Query("DELETE FROM achievements WHERE achievId = :achievementId")
    suspend fun deleteAchievementById(achievementId: String): Int

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()
}