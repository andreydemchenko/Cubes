package ru.turbopro.cubes.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.turbopro.cubes.data.Event

@Dao
interface EventsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListOfEvents(events: List<Event>)

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM events")
    fun observeEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE eventId = :eventId")
    suspend fun getEventById(eventId: String): Event?

    @Query("SELECT * FROM events WHERE date = :date")
    suspend fun getEventByDate(date: String): Event?

    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteEventById(eventId: String): Int

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}