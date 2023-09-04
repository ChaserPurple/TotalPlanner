package com.example.totalplanner.data

import androidx.room.TypeConverter

class MyDateConverter{
    @TypeConverter
    fun toDate(d: String): MyDate{
        val arr = d.split(" ")
        return MyDate(
            day = arr[0],
            month = Month.values().filter{it.ordinal == arr[1].toInt()}[0],
            year = arr[2],
            hour = arr[3],
            minute = arr[4],
            isAfternoon = arr[5].toBooleanStrict(),
        )
    }

    @TypeConverter
    fun fromDate(d: MyDate): String{
        return "${d.day} ${d.month.ordinal} ${d.year} ${d.hour} ${d.minute} ${d.isAfternoon}"
    }
}