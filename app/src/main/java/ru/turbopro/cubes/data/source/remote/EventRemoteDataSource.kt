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
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.source.EventDataSource

class EventRemoteDataSource : EventDataSource {

    private val firebaseDb: FirebaseFirestore = Firebase.firestore
    private val firebaseStorage: FirebaseStorage = Firebase.storage

    private val observableEvents = MutableLiveData<Result<List<Event>>?>()

    private fun storageRef() = firebaseStorage.reference
    private fun eventsCollectionRef() = firebaseDb.collection(EVENT_COLLECTION)

    override suspend fun refreshEvents() {
        observableEvents.value = getAllEvents()
    }

    override fun observeEvents(): LiveData<Result<List<Event>>?> {
        return observableEvents
    }

    override suspend fun getAllEvents(): Result<List<Event>> {
        val resRef = eventsCollectionRef().get().await()
        return if (!resRef.isEmpty) {
            Result.Success(resRef.toObjects(Event::class.java))
        } else {
            Result.Error(Exception("Error getting events!"))
        }
    }

    override suspend fun insertEvent(newEvent: Event) {
        eventsCollectionRef().add(newEvent.toHashMap()).await()
    }

    override suspend fun updateEvent(evData: Event) {
        val resRef =
            eventsCollectionRef().whereEqualTo(EVENT_ID_FIELD, evData.eventId).get().await()
        if (!resRef.isEmpty) {
            val docId = resRef.documents[0].id
            eventsCollectionRef().document(docId).set(evData.toHashMap()).await()
        } else {
            Log.d(TAG, "onUpdateEvent: event with id: $evData.eventId not found!")
        }
    }

    override suspend fun getEventById(eventId: String): Result<Event> {
        val resRef = eventsCollectionRef().whereEqualTo(EVENT_ID_FIELD, eventId).get().await()
        return if (!resRef.isEmpty) {
            Result.Success(resRef.toObjects(Event::class.java)[0])
        } else {
            Result.Error(Exception("Event with id: $eventId Not Found!"))
        }
    }

    override suspend fun getEventByDate(eventDate: String): Result<Event> {
        val resRef = eventsCollectionRef().whereEqualTo(EVENT_DATE_FIELD, eventDate).get().await()
        return if (!resRef.isEmpty) {
            Result.Success(resRef.toObjects(Event::class.java)[0])
        } else {
            Result.Error(Exception("Event with id: $eventDate Not Found!"))
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        Log.d(TAG, "onDeleteEvent: delete event with Id: $eventId initiated")
        val resRef = eventsCollectionRef().whereEqualTo(EVENT_ID_FIELD, eventId).get().await()
        if (!resRef.isEmpty) {
            val event = resRef.documents[0].toObject(Event::class.java)
            val imgUrls = event?.images

            //deleting images first
            imgUrls?.forEach { imgUrl ->
                deleteImage(imgUrl.toString())
            }

            //deleting doc containing event
            val docId = resRef.documents[0].id
            eventsCollectionRef().document(docId).delete().addOnSuccessListener {
                Log.d(TAG, "onDelete: DocumentSnapshot successfully deleted!")
            }.addOnFailureListener { e ->
                Log.w(TAG, "onDelete: Error deleting document", e)
            }
        } else {
            Log.d(TAG, "onDeleteEvent: event with id: $eventId not found!")
        }
    }

    override suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
        val imgRef = storageRef().child("$EVENTS_STORAGE_PATH/$fileName")
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
        val imgRef = storageRef().child("${EVENTS_STORAGE_PATH}/$fileName")
        imgRef.delete().addOnSuccessListener {
            Log.d(TAG, "onRevert: File with name: $fileName deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onRevert: Error deleting file with name = $fileName, error: $e")
        }
    }

    companion object {
        private const val EVENT_COLLECTION = "events"
        private const val EVENT_ID_FIELD = "eventId"
        private const val EVENT_DATE_FIELD = "eventDate"
        private const val EVENTS_STORAGE_PATH = "Events"
        private const val TAG = "EventsRemoteSource"
    }
}