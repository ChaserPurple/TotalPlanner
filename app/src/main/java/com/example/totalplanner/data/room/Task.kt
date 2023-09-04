package com.example.totalplanner.data.room

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.totalplanner.data.MyColor
import com.example.totalplanner.data.MyDate
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val description: String,
    val deadline: MyDate,
    val color: MyColor
)
