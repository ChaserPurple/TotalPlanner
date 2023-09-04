package com.example.totalplanner.data

import androidx.room.TypeConverter

class ColorConverter {
    @TypeConverter
    fun fromColor(c: MyColor): String{
        return "${c.r} ${c.g} ${c.b} ${c.a}"
    }
    @TypeConverter
    fun toColor(c: String): MyColor {
        val arr = c.split(" ")
        return MyColor(
            r = arr[0].toFloat(),
            g = arr[1].toFloat(),
            b = arr[2].toFloat(),
            a = arr[3].toFloat()
        )
    }
}