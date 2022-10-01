package ru.turbopro.cubes.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.source.EventDataSource

class EventsLocalDataSource internal constructor(
    private val eventsDao: EventsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EventDataSource{

    override fun observeEvents(): LiveData<Result<List<Event>>?> {
        return try {
            Transformations.map(eventsDao.observeEvents()) {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Result.Error(e)
            }
        }
    }

    override suspend fun getAllEvents(): Result<List<Event>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(eventsDao.getAllEvents())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getEventById(eventId: String): Result<Event> =
        withContext(ioDispatcher) {
            try {
                val event = eventsDao.getEventById(eventId)
                if (event != null) {
                    return@withContext Result.Success(event)
                } else {
                    return@withContext Result.Error(Exception("Event Not Found!"))
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }

    override suspend fun getEventByDate(eventDate: String): Result<Event> =
        withContext(ioDispatcher) {
            try {
                val event = eventsDao.getEventByDate(eventDate)
                if (event != null) {
                    return@withContext Result.Success(event)
                } else {
                    return@withContext Result.Error(Exception("Event Not Found!"))
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }

    override suspend fun insertEvent(newEvent: Event) = withContext(ioDispatcher) {
        eventsDao.insert(newEvent)
    }

    override suspend fun updateEvent(evData: Event) = withContext(ioDispatcher) {
        eventsDao.insert(evData)
    }

    override suspend fun insertMultipleEvents(data: List<Event>) = withContext(ioDispatcher) {
        eventsDao.insertListOfEvents(data)
    }

    override suspend fun deleteEvent(eventId: String): Unit = withContext(ioDispatcher) {
        eventsDao.deleteEventById(eventId)
    }

    override suspend fun deleteAllEvents() = withContext(ioDispatcher) {
        eventsDao.deleteAllEvents()
    }
}