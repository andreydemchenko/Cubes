package ru.turbopro.cubes.data.source.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import ru.turbopro.cubes.ERR_UPLOAD
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.source.EventDataSource
import ru.turbopro.cubes.data.utils.StoreEventDataStatus
import java.util.*

class EventsRepository(
    private val eventsRemoteSource: EventDataSource,
    private val eventsLocalSource: EventDataSource
) : EventsRepoInterface{

    companion object {
        private const val TAG = "EventsRepository"
    }

    override suspend fun refreshEvents(): StoreEventDataStatus? {
        Log.d(TAG, "Updating Events in Room")
        return updateEventsFromRemoteSource()
    }

    override fun observeEvents(): LiveData<Result<List<Event>>?> {
        return eventsLocalSource.observeEvents()
    }

    override suspend fun getEventById(eventId: String, forceUpdate: Boolean): Result<Event> {
        if (forceUpdate) {
            updateEventFromRemoteSource(eventId)
        }
        return eventsLocalSource.getEventById(eventId)
    }

    override suspend fun getEventByDate(eventDate: String, forceUpdate: Boolean): Result<Event> {
        if (forceUpdate) {
            updateEventFromRemoteSource(eventDate)
        }
        return eventsLocalSource.getEventByDate(eventDate)
    }

    override suspend fun insertEvent(newEvent: Event): Result<Boolean> {
        return supervisorScope {
            val localRes = async {
                Log.d(TAG, "onInsertEvent: adding event to local source")
                eventsLocalSource.insertEvent(newEvent)
            }
            val remoteRes = async {
                Log.d(TAG, "onInsertEvent: adding event to remote source")
                eventsRemoteSource.insertEvent(newEvent)
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
                val downloadUrl = eventsRemoteSource.uploadImage(uri, fileName)
                urlList.add(downloadUrl.toString())
            } catch (e: Exception) {
                eventsRemoteSource.revertUpload(fileName)
                Log.d(TAG, "exception: message = $e")
                urlList = mutableListOf()
                urlList.add(ERR_UPLOAD)
                return@label
            }
        }
        return urlList
    }

    override suspend fun updateEvent(event: Event): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onUpdate: updating event in remote source")
                eventsRemoteSource.updateEvent(event)
            }
            val localRes = async {
                Log.d(TAG, "onUpdate: updating event in local source")
                eventsLocalSource.insertEvent(event)
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
                    val downloadUrl = eventsRemoteSource.uploadImage(uri, fileName)
                    urlList.add(downloadUrl.toString())
                } catch (e: Exception) {
                    eventsRemoteSource.revertUpload(fileName)
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
                eventsRemoteSource.deleteImage(imgUrl)
            }
        }
        return urlList
    }

    override suspend fun deleteEventById(eventId: String): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting event from remote source")
                eventsRemoteSource.deleteEvent(eventId)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting event from local source")
                eventsLocalSource.deleteEvent(eventId)
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

    private suspend fun updateEventsFromRemoteSource(): StoreEventDataStatus? {
        var res: StoreEventDataStatus? = null
        try {
            val remoteEvents = eventsRemoteSource.getAllEvents()
            if (remoteEvents is Result.Success) {
                Log.d(TAG, "pro list = ${remoteEvents.data}")
                eventsLocalSource.deleteAllEvents()
                eventsLocalSource.insertMultipleEvents(remoteEvents.data)
                res = StoreEventDataStatus.DONE
            } else {
                res = StoreEventDataStatus.ERROR
                if (remoteEvents is Result.Error)
                    throw remoteEvents.exception
            }
        } catch (e: Exception) {
            Log.d(TAG, "onUpdateEventsFromRemoteSource: Exception occurred, ${e.message}")
        }

        return res
    }

    private suspend fun updateEventFromRemoteSource(eventId: String): StoreEventDataStatus? {
        var res: StoreEventDataStatus? = null
        try {
            val remoteEvent = eventsRemoteSource.getEventById(eventId)
            if (remoteEvent is Result.Success) {
                eventsLocalSource.insertEvent(remoteEvent.data)
                res = StoreEventDataStatus.DONE
            } else {
                res = StoreEventDataStatus.ERROR
                if (remoteEvent is Result.Error)
                    throw remoteEvent.exception
            }
        } catch (e: Exception) {
            Log.d(TAG, "onUpdateEventFromRemoteSource: Exception occurred, ${e.message}")
        }
        return res
    }
}