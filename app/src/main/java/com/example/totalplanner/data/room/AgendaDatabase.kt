package com.example.totalplanner.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.totalplanner.data.ColorConverter
import com.example.totalplanner.data.MyDateConverter

@Database(entities = [Event::class, Task::class], version = 5)
@TypeConverters(MyDateConverter::class, ColorConverter::class)
abstract class AgendaDatabase : RoomDatabase() {
    abstract fun getEventDao(): EventDAO
    abstract fun getTaskDao(): TaskDAO

    companion object {
        @Volatile
        private var Instance: AgendaDatabase? = null

        fun getDatabase(context: Context): AgendaDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AgendaDatabase::class.java, "agenda")
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}