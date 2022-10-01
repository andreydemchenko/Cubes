package ru.turbopro.cubes.data.source

import android.net.Uri
import androidx.lifecycle.LiveData
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.Result

interface EventDataSource {

    fun observeEvents(): LiveData<Result<List<Event>>?>

    suspend fun getAllEvents(): Result<List<Event>>

    suspend fun refreshEvents() {}

    suspend fun getEventById(eventId: String): Result<Event>

    suspend fun getEventByDate(eventDate: String): Result<Event>

    suspend fun insertEvent(newEvent: Event)

    suspend fun updateEvent(evData: Event)

    suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
        return null
    }

    fun revertUpload(fileName: String) {}
    fun deleteImage(imgUrl: String) {}
    suspend fun deleteEvent(eventId: String)
    suspend fun deleteAllEvents() {}
    suspend fun insertMultipleEvents(data: List<Event>) {}
}