package ru.turbopro.cubes.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.turbopro.cubes.data.Achievement
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.source.AchievementDataSource

class AchievementsLocalDataSource internal constructor(
    private val achievementsDao: AchievementsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AchievementDataSource {

    override fun observeAchievements(): LiveData<Result<List<Achievement>>?> {
        return try {
            Transformations.map(achievementsDao.observeAchievements()) {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Result.Error(e)
            }
        }
    }

    override suspend fun getAllAchievements(): Result<List<Achievement>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(achievementsDao.getAllAchievements())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAchievementById(achievementId: String): Result<Achievement> =
        withContext(ioDispatcher) {
            try {
                val achievement = achievementsDao.getAchievementById(achievementId)
                if (achievement != null) {
                    return@withContext Result.Success(achievement)
                } else {
                    return@withContext Result.Error(Exception("Achievement Not Found!"))
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }

    override suspend fun insertAchievement(newAchievement: Achievement) = withContext(ioDispatcher) {
        achievementsDao.insert(newAchievement)
    }

    override suspend fun updateAchievement(achData: Achievement) = withContext(ioDispatcher) {
        achievementsDao.insert(achData)
    }

    override suspend fun insertMultipleAchievements(data: List<Achievement>) = withContext(ioDispatcher) {
        achievementsDao.insertListOfAchievements(data)
    }

    override suspend fun deleteAchievement(achievementId: String): Unit = withContext(ioDispatcher) {
        achievementsDao.deleteAchievementById(achievementId)
    }

    override suspend fun deleteAllAchievements() = withContext(ioDispatcher) {
        achievementsDao.deleteAllAchievements()
    }
}