package ru.turbopro.cubes.data.source.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import ru.turbopro.cubes.data.Achievement
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.utils.StoreAchievementDataStatus

interface AchievementsRepoInterface {
    suspend fun refreshAchievements(): StoreAchievementDataStatus?
    fun observeAchievements(): LiveData<Result<List<Achievement>>?>
    suspend fun getAchievementById(achievementId: String, forceUpdate: Boolean = false): Result<Achievement>
    suspend fun insertAchievement(newAchievement: Achievement): Result<Boolean>
    suspend fun insertImages(imgList: List<Uri>): List<String>
    suspend fun updateAchievement(achievement: Achievement): Result<Boolean>
    suspend fun updateImages(newList: List<Uri>, oldList: List<String>): List<String>
    suspend fun deleteAchievementById(achievementId: String): Result<Boolean>
}