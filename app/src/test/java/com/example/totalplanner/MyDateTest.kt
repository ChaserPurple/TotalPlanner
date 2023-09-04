package com.example.totalplanner

import com.example.totalplanner.data.Month
import com.example.totalplanner.data.MyDate
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MyDateTest {
    @Test
    fun beforeAfterTest() {
        val b = MyDate(
            day = "17",
            month = Month.AUGUST,
            year = "2023",
            hour = "6",
            minute = "26",
            isAfternoon = false
        )
        val a = MyDate(
            day = "18",
            month = Month.AUGUST,
            year = "2023",
            hour = "5",
            minute = "26",
            isAfternoon = true
        )
        assertEquals(a.after(b), true)
        assertEquals(b.after(a), false)
        assertEquals(a.before(b), false)
        assertEquals(b.before(a), true)
    }
}