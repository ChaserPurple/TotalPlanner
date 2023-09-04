package com.example.totalplanner.data

import android.util.Log
import androidx.annotation.StringRes
import com.example.totalplanner.R
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.abs

enum class Month(@StringRes val monthName: Int, val maxDay: Int, val key: Int){
    JANUARY(monthName = R.string.january, maxDay = 31, key = 11),
    FEBRUARY(monthName = R.string.february, maxDay = 28, key = 12),
    MARCH(monthName = R.string.march, maxDay = 31, key = 1),
    APRIL(monthName = R.string.april, maxDay = 30, key = 2),
    MAY(monthName = R.string.may, maxDay = 31, key = 3),
    JUNE(monthName = R.string.june, maxDay = 30, key = 4),
    JULY(monthName = R.string.july, maxDay = 31, key = 5),
    AUGUST(monthName = R.string.august, maxDay = 31, key = 6),
    SEPTEMBER(monthName = R.string.september, maxDay = 30, key = 7),
    OCTOBER(monthName = R.string.october, maxDay = 31, key = 8),
    NOVEMBER(monthName = R.string.november, maxDay = 30, key = 9),
    DECEMBER(monthName = R.string.december, maxDay = 31, key = 10)
}

enum class Weekday(@StringRes val dayName: Int){
    SUNDAY(R.string.sunday),
    MONDAY(R.string.monday),
    TUESDAY(R.string.tuesday),
    WEDNESDAY(R.string.wednesday),
    THURSDAY(R.string.thursday),
    FRIDAY(R.string.friday),
    SATURDAY(R.string.saturday)
}

class MyDate(
    var month: Month = Month.values().filter{it.ordinal == Calendar.getInstance().get(Calendar.MONTH)}[0],
    var day: String = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString(),
    var year: String = Calendar.getInstance().get(Calendar.YEAR).toString(),
    var hour: String = if(Calendar.getInstance().get(Calendar.HOUR) == 0) "12"
    else (Calendar.getInstance().get(Calendar.HOUR)).toString(),
    var minute: String = if(Calendar.getInstance().get(Calendar.MINUTE) >= 10)
        Calendar.getInstance().get(Calendar.MINUTE).toString()
    else
        "0${Calendar.getInstance().get(Calendar.MINUTE)}",
    var isAfternoon: Boolean = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 11
): Comparable<MyDate> {
    //Overrides
    override fun equals(other: Any?): Boolean {
        if(other is MyDate){
            return other.day == this.day && other.month == this.month &&
                    other.year == this.year && other.minute == this.minute &&
                    other.hour == this.hour && other.isAfternoon == this.isAfternoon
        } else return false
    }
    override fun hashCode(): Int {
        return (month.toString() + day + year + hour + minute + isAfternoon.toString()).hashCode()
    }
    override fun compareTo(other: MyDate): Int{
        return if(before(other)) -1
               else if (after(other)) 1
               else 0
    }
    fun toString(americanDate: Boolean, hour24: Boolean): String {
        var ret = ""
        if(hour24)
            ret += to24String()
        else
            ret += to12String()
        ret += " "
        if(americanDate)
            ret += toAmericanString()
        else
            ret += toEuropeanString()
        return ret
    }

    /*
     * Returns a string that is a numeric representation of the day but not time
     */
    fun toAmericanString(): String{
        return if(isValid())
            "${month.ordinal + 1}/$day/$year"
        else
            "Invalid Date"
    }
    /*
     * Returns a string that is a numeric representation of the day but not time
     */
    fun toEuropeanString(): String{
        return if(isValid())
            "$day/${month.ordinal + 1}/$year"
        else
            "Invalid Date"
    }
    /*
     * Returns a string that is a numeric representation of the time but not day
     * Uses the 12 hour format
     */
    fun to12String(): String{
        minute = minute.toInt().toString()
        if (minute.length == 1)
            minute = "0$minute"
        hour = hour.toInt().toString()
        var ret = "$hour:$minute"
        ret += if (isAfternoon) "pm"
        else "am"
        return ret
    }
    /*
     * Returns a string that is a numeric representation of the day time not day
     * Uses the 24 hour format
     */
    fun to24String(): String{
        minute = minute.toInt().toString()
        if (minute.length == 1)
            minute = "0$minute"
        var h = hour.toInt()
        if(h == 12)
            h = 0
        if(isAfternoon)
            h += 12
        return "$h:$minute"
    }
    /*
     * Acts as a copy constructor, but specific arguments can be passed in to
     * override certain members of the Date being copied
     */
    fun copy(
        month: Month = this.month,
        day: String = this.day,
        year: String = this.year,
        hour: String = this.hour,
        minute: String = this.minute,
        isAfternoon: Boolean = this.isAfternoon
    ): MyDate{
        return MyDate(
            month = month,
            day = day,
            year = year,
            hour = hour,
            minute = minute,
            isAfternoon = isAfternoon
        )
    }

    /*
     * Returns true if two dates are on the same date even if it's a diff time
     */
    fun sameDay(other: MyDate): Boolean{
        return other.day.toInt() == day.toInt() &&
                other.month == month && other.year.toInt() == year.toInt()
    }
    /*
     * Returns a the weekday of a date
     * Precondition: this is a valid date
     */
    fun getWeekday(): Weekday{
        val y = year.toInt()
        var sum: Int = day.toInt()
        sum += ((2.6 * month.key - .2)).toInt()
        sum -= y / 50
        sum += y % 100
        sum += (y % 100) / 4
        sum += y / 400
        sum = abs(sum % 7)
        return Weekday.values()[sum]
    }
    /*
     * Finds the most recent Sunday before this date, including this date
     * Returns a default MyDate if invalid
     */
    fun lastSunday(): MyDate{
        if(!isValid())
            return MyDate()
        else{
            if(getWeekday() != Weekday.SUNDAY)
                return(addDays(-getWeekday().ordinal))
            return copy()
        }
    }
    /*
     * Returns a date n days later than this, more computationally expensive
     * the more months are added
     * Precondition: date is valid
     */
    fun addDays(n: Int): MyDate{
        var d = (day.toInt() + n)
        var m = month
        var y = year.toInt()
        while(d > m.maxDay){
            d -= m.maxDay
            if(m != Month.DECEMBER)
                //Don't need modulus, that case is in the else
                m = Month.values()[m.ordinal + 1]
            else{
                m = Month.JANUARY
                y = year.toInt() + 1
            }
        }
        while(d < 1){
            if(m != Month.JANUARY)
            //Don't need modulus, that case is in the else
                m = Month.values()[m.ordinal - 1]
            else{
                m = Month.DECEMBER
                y = year.toInt() - 1
            }
            d += m.maxDay
        }
        return copy(
            day = d.toString(),
            month = m,
            year = y.toString()
        )
    }
    /*
     * returns this number of minutes between this and other, always a positive
     * number
     * Precondition: both dates are valid
     */
    fun minuteDifference(other: MyDate): Int{
        var oh = if(other.hour.toInt() == 12) 0 else other.hour.toInt()
        if(other.isAfternoon)
            oh += 12
        var h = if(hour.toInt() == 12) 0 else hour.toInt()
        if(isAfternoon)
            h += 12
        val first = GregorianCalendar(
            year.toInt(), month.ordinal, day.toInt(),
            h, minute.toInt()
        ).timeInMillis / 60000L
        val second = GregorianCalendar(
            other.year.toInt(), other.month.ordinal, other.day.toInt(),
            oh, other.minute.toInt()
        ).timeInMillis / 60000L
        return abs(first - second).toInt()
    }

    /*
     * Determines whether this date is later than the date that's passed in
     * Precondition: both dates are valid
     */
    fun after(other: MyDate): Boolean {
        if(other.year.toInt() < year.toInt()) return true
        else if(other.year.toInt() > year.toInt()) return false
        if(other.month.ordinal < month.ordinal) return true
        else if(other.month.ordinal > month.ordinal) return false
        if(other.day.toInt() < day.toInt()) return true
        else if(other.day.toInt() > day.toInt()) return false
        if(!other.isAfternoon && isAfternoon) return true
        else if(other.isAfternoon && !isAfternoon) return false
        val oh = if(other.hour.toInt() == 12) 0 else other.hour.toInt()
        val h = if(hour.toInt() == 12) 0 else hour.toInt()
        if(oh < h) return true
        else if(oh > h) return false
        return other.minute.toInt() < minute.toInt()
    }
    /*
     * Determines whether this date is earlier than the date that's passed in
     * Precondition: both dates are valid
     */
    fun before(other: MyDate): Boolean {
        if(other.year.toInt() > year.toInt()) return true
        else if(other.year.toInt() < year.toInt()) return false
        if(other.month.ordinal > month.ordinal) return true
        else if(other.month.ordinal < month.ordinal) return false
        if(other.day.toInt() > day.toInt()) return true
        else if(other.day.toInt() < day.toInt()) return false
        if(other.isAfternoon && !isAfternoon) return true
        else if(!other.isAfternoon && isAfternoon) return false
        val oh = if(other.hour.toInt() == 12) 0 else other.hour.toInt()
        val h = if(hour.toInt() == 12) 0 else hour.toInt()
        if(oh > h) return true
        else if(oh < h) return false
        return other.minute.toInt() > minute.toInt()
    }

    /*
     * Determines whether, when transitioned from string values, this date can
     * be translated into a logical/recognizable date, i.e is a valid date
     */
    fun isValid(): Boolean {
        //extract values
        val d = day.toIntOrNull()
        val y = year.toIntOrNull()
        val h = hour.toIntOrNull()
        val m = minute.toIntOrNull()
        //if it's a leap year
        var maxDay = month.maxDay
        if (month == Month.FEBRUARY && isLeapYear())
            maxDay = 29
        //evaluate return
        return if (d == null || y == null || h == null || m == null) false
        else if (d < 1 || d > maxDay) false
        else if (y < 0) false
        else if (h < 1 || h > 12) false
        else !(m < 0 || m > 59)
    }
    /*
     * Determines whether this date is later than or equal to the current time
     * Precondition: this is a valid date
     */
    fun isFuture(): Boolean {
        return after(MyDate())
    }
    /*
     * Returns true if the year is a leap year, otherwise false. It will be
     * false if the string year is nan
     */
    private fun isLeapYear(): Boolean{
        val y = year.toIntOrNull()
        return if(y != null)
            ((y % 100 == 0 && y % 400 == 0) || (y % 100 != 0 && y % 4 == 0))
        else
            false
    }
}
