package com.example.totalplanner.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: Event) : Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM events WHERE taskID = :taskID ORDER BY startDate")
    suspend fun getEventOfTask(taskID: Int) : List<Event>

    @Query("SELECT * FROM events ORDER BY startDate")
    fun getAllEvents() : Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id ORDER BY startDate")
    suspend fun getEventByID(id: Int): Event
}