package ru.turbopro.cubes.data.source.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import ru.turbopro.cubes.ERR_UPLOAD
import ru.turbopro.cubes.data.Achievement
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.source.AchievementDataSource
import ru.turbopro.cubes.data.utils.StoreAchievementDataStatus
import java.util.*

class AchievementsRepository(
    private val achievementsRemoteSource: AchievementDataSource,
    private val achievementsLocalSource: AchievementDataSource
) : AchievementsRepoInterface {
    companion object {
        private const val TAG = "AchievementsRepository"
    }

    override suspend fun refreshAchievements(): StoreAchievementDataStatus? {
        Log.d(TAG, "Updating Achievements in Room")
        return updateAchievementsFromRemoteSource()
    }

    override fun observeAchievements(): LiveData<Result<List<Achievement>>?> {
        return achievementsLocalSource.observeAchievements()
    }

    override suspend fun getAchievementById(achievementId: String, forceUpdate: Boolean): Result<Achievement> {
        if (forceUpdate) {
            updateAchievementFromRemoteSource(achievementId)
        }
        return achievementsLocalSource.getAchievementById(achievementId)
    }

    override suspend fun insertAchievement(newAchievement: Achievement): Result<Boolean> {
        return supervisorScope {
            val localRes = async {
                Log.d(TAG, "onInsertAchievement: adding achievement to local source")
                achievementsLocalSource.insertAchievement(newAchievement)
            }
            val remoteRes = async {
                Log.d(TAG, "onInsertAchievement: adding achievement to remote source")
                achievementsRemoteSource.insertAchievement(newAchievement)
            }
            try {
                localRes.await()
                remoteRes.await()
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun insertImages(imgList: List<Uri>): List<String> {
        var urlList = mutableListOf<String>()
        imgList.forEach label@{ uri ->
            val uniId = UUID.randomUUID().toString()
            val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
            try {
                val downloadUrl = achievementsRemoteSource.uploadImage(uri, fileName)
                urlList.add(downloadUrl.toString())
            } catch (e: Exception) {
                achievementsRemoteSource.revertUpload(fileName)
                Log.d(TAG, "exception: message = $e")
                urlList = mutableListOf()
                urlList.add(ERR_UPLOAD)
                return@label
            }
        }
        return urlList
    }

    override suspend fun updateAchievement(achievement: Achievement): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onUpdate: updating achievement in remote source")
                achievementsRemoteSource.updateAchievement(achievement)
            }
            val localRes = async {
                Log.d(TAG, "onUpdate: updating achievement in local source")
                achievementsLocalSource.insertAchievement(achievement)
            }
            try {
                remoteRes.await()
                localRes.await()
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateImages(newList: List<Uri>, oldList: List<String>): List<String> {
        var urlList = mutableListOf<String>()
        newList.forEach label@{ uri ->
            if (!oldList.contains(uri.toString())) {
                val uniId = UUID.randomUUID().toString()
                val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
                try {
                    val downloadUrl = achievementsRemoteSource.uploadImage(uri, fileName)
                    urlList.add(downloadUrl.toString())
                } catch (e: Exception) {
                    achievementsRemoteSource.revertUpload(fileName)
                    Log.d(TAG, "exception: message = $e")
                    urlList = mutableListOf()
                    urlList.add(ERR_UPLOAD)
                    return@label
                }
            } else {
                urlList.add(uri.toString())
            }
        }
        oldList.forEach { imgUrl ->
            if (!newList.contains(imgUrl.toUri())) {
                achievementsRemoteSource.deleteImage(imgUrl)
            }
        }
        return urlList
    }

    override suspend fun deleteAchievementById(achievementId: String): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting achievement from remote source")
                achievementsRemoteSource.deleteAchievement(achievementId)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting achievement from local source")
                achievementsLocalSource.deleteAchievement(achievementId)
            }
            try {
                remoteRes.await()
                localRes.await()
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    private suspend fun updateAchievementsFromRemoteSource(): StoreAchievementDataStatus? {
        var res: StoreAchievementDataStatus? = null
        try {
            val remoteAchievements = achievementsRemoteSource.getAllAchievements()
            if (remoteAchievements is Result.Success) {
                Log.d(TAG, "pro list = ${remoteAchievements.data}")
                achievementsLocalSource.deleteAllAchievements()
                achievementsLocalSource.insertMultipleAchievements(remoteAchievements.data)
                res = StoreAchievementDataStatus.DONE
            } else {
                res = StoreAchievementDataStatus.ERROR
                if (remoteAchievements is Result.Error)
                    throw remoteAchievements.exception
            }
        } catch (e: Exception) {
            Log.d(TAG, "onUpdateAchievementsFromRemoteSource: Exception occurred, ${e.message}")
        }

        return res
    }

    private suspend fun updateAchievementFromRemoteSource(achievementId: String): StoreAchievementDataStatus? {
        var res: StoreAchievementDataStatus? = null
        try {
            val remoteAchievement = achievementsRemoteSource.getAchievementById(achievementId)
            if (remoteAchievement is Result.Success) {
                achievementsLocalSource.insertAchievement(remoteAchievement.data)
                res = StoreAchievementDataStatus.DONE
            } else {
                res = StoreAchievementDataStatus.ERROR
                if (remoteAchievement is Result.Error)
                    throw remoteAchievement.exception
            }
        } catch (e: Exception) {
            Log.d(TAG, "onUpdateAchievementFromRemoteSource: Exception occurred, ${e.message}")
        }
        return res
    }
}