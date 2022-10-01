package ru.turbopro.cubes.data.source.remote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import ru.turbopro.cubes.data.Achievement
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.source.AchievementDataSource

class AchievementRemoteDataSource : AchievementDataSource {

    private val firebaseDb: FirebaseFirestore = Firebase.firestore
    private val firebaseStorage: FirebaseStorage = Firebase.storage

    private val observableAchievements = MutableLiveData<Result<List<Achievement>>?>()

    private fun storageRef() = firebaseStorage.reference
    private fun achievementsCollectionRef() = firebaseDb.collection(ACHIEVEMENT_COLLECTION)

    override suspend fun refreshAchievements() {
        observableAchievements.value = getAllAchievements()
    }

    override fun observeAchievements(): LiveData<Result<List<Achievement>>?> {
        return observableAchievements
    }

    override suspend fun getAllAchievements(): Result<List<Achievement>> {
        val resRef = achievementsCollectionRef().get().await()
        return if (!resRef.isEmpty) {
            Result.Success(resRef.toObjects(Achievement::class.java))
        } else {
            Result.Error(Exception("Error getting achievements!"))
        }
    }

    override suspend fun insertAchievement(newAchievement: Achievement) {
        achievementsCollectionRef().add(newAchievement.toHashMap()).await()
    }

    override suspend fun updateAchievement(achData: Achievement) {
        val resRef =
            achievementsCollectionRef().whereEqualTo(ACHIEVEMENT_ID_FIELD, achData.achievId).get().await()
        if (!resRef.isEmpty) {
            val docId = resRef.documents[0].id
            achievementsCollectionRef().document(docId).set(achData.toHashMap()).await()
        } else {
            Log.d(TAG, "onUpdateAchievement: achievement with id: ${achData.achievId} not found!")
        }
    }

    override suspend fun getAchievementById(achievementId: String): Result<Achievement> {
        val resRef = achievementsCollectionRef().whereEqualTo(ACHIEVEMENT_ID_FIELD, achievementId).get().await()
        return if (!resRef.isEmpty) {
            Result.Success(resRef.toObjects(Achievement::class.java)[0])
        } else {
            Result.Error(Exception("Achievement with id: $achievementId Not Found!"))
        }
    }

    override suspend fun deleteAchievement(achievementId: String) {
        Log.d(TAG, "onDeleteEvent: delete event with Id: $achievementId initiated")
        val resRef = achievementsCollectionRef().whereEqualTo(ACHIEVEMENT_ID_FIELD, achievementId).get().await()
        if (!resRef.isEmpty) {
            val event = resRef.documents[0].toObject(Achievement::class.java)
            val imgUrls = event?.achievImageUrl

            //deleting images first
            imgUrls?.forEach { imgUrl ->
                deleteImage(imgUrl.toString())
            }

            //deleting doc containing event
            val docId = resRef.documents[0].id
            achievementsCollectionRef().document(docId).delete().addOnSuccessListener {
                Log.d(TAG, "onDelete: DocumentSnapshot successfully deleted!")
            }.addOnFailureListener { e ->
                Log.w(TAG, "onDelete: Error deleting document", e)
            }
        } else {
            Log.d(TAG, "onDeleteEvent: achievement with id: $achievementId not found!")
        }
    }

    override suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
        val imgRef = storageRef().child("$ACHIEVEMENTS_STORAGE_PATH/$fileName")
        val uploadTask = imgRef.putFile(uri)
        val uriRef = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imgRef.downloadUrl
        }
        return uriRef.await()
    }

    override fun deleteImage(imgUrl: String) {
        val ref = firebaseStorage.getReferenceFromUrl(imgUrl)
        ref.delete().addOnSuccessListener {
            Log.d(TAG, "onDelete: image deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onDelete: Error deleting image, error: $e")
        }
    }

    override fun revertUpload(fileName: String) {
        val imgRef = storageRef().child("${ACHIEVEMENTS_STORAGE_PATH}/$fileName")
        imgRef.delete().addOnSuccessListener {
            Log.d(TAG, "onRevert: File with name: $fileName deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onRevert: Error deleting file with name = $fileName, error: $e")
        }
    }

    companion object {
        private const val ACHIEVEMENT_COLLECTION = "achievements"
        private const val ACHIEVEMENT_ID_FIELD = "ahievId"
        private const val ACHIEVEMENTS_STORAGE_PATH = "Achievements"
        private const val TAG = "AchievementsRemoteSource"
    }
}