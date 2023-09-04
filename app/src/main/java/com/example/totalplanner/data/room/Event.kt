package com.example.totalplanner.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.totalplanner.data.MyColor
import com.example.totalplanner.data.MyDate

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,

    val name: String,
    val description: String,
    val color: MyColor,
    val startDate: MyDate,
    val endDate: MyDate,
    val taskID: Long
)
