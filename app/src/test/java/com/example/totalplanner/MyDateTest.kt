package com.example.totalplanner

import com.example.totalplanner.data.Month
import com.example.totalplanner.data.MyDate
import org.junit.Test

import org.junit.Assert.*
import java.util.Calendar

/**
 * Tests methods of the MyDate class for correctness
 */
class MyDateTest {
    /*
     * Tests the before() and after() methods
     * Tests the basic functionality of these methods with inputs well within
     * expected values
     */
    @Test
    fun basicBeforeAfterTest() {
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

    /*
     * Tests the sameDay() method
     * Tests the basic functionality of the method with inputs well within
     * expected values
     */
    @Test
    fun basicSameDayTest(){
        assertEquals(MyDate().sameDay(MyDate(hour = "12", minute = "40")),true)
    }

    /*
     * Tests the getWeekday() method
     * Tests the basic functionality of the method with inputs well within
     * expected values, iterating through each of the seven possible outputs
     */
    @Test
    fun basicGetWeekdayTest(){
        var date = MyDate(
            minute = "00",
            hour = "12",
            isAfternoon = false,
            month = Month.SEPTEMBER,
            day = "3",
            year = "2023"
        )
        for(i in 0..6){
            assertEquals(date.getWeekday().ordinal,i)
            date = date.addDays(1)
        }
    }

    /*
     * Tests the lastSunday() method
     * Tests the basic functionality of the method with inputs well within
     * expected values
     */
    @Test
    fun basicLastSundayTest(){
        val date = MyDate(
            minute = "00",
            hour = "12",
            isAfternoon = false,
            month = Month.SEPTEMBER,
            day = "5",
            year = "2023"
        )
        val sunday = MyDate(
            minute = "00",
            hour = "12",
            isAfternoon = false,
            month = Month.SEPTEMBER,
            day = "3",
            year = "2023"
        )
        assertEquals(date.lastSunday(),sunday)
    }
    /*
     * Tests the lastSunday() method
     * Tests whether the function correctly returns the same date if the date
     * is a Sunday
     */
    @Test
    fun isSundayLastSundayTest(){
        val sunday = MyDate(
            minute = "00",
            hour = "12",
            isAfternoon = false,
            month = Month.SEPTEMBER,
            day = "3",
            year = "2023"
        )
        assertEquals(sunday.lastSunday(),sunday)
    }

    /*
     * Tests the addDays() method
     * Tests the basic functionality of the method with inputs well within
     * expected values
     */
    @Test
    fun basicAddDaysTest(){
        val a = MyDate(
            day = "16",
            month = Month.AUGUST,
            year = "2023",
            hour = "6",
            minute = "26",
            isAfternoon = true
        )
        val b = MyDate(
            day = "17",
            month = Month.AUGUST,
            year = "2023",
            hour = "6",
            minute = "26",
            isAfternoon = true
        )
        assertEquals(a.addDays(1), b)
    }
    /*
     * Tests the addDays() method
     * Tests whether the output of the method is a valid date
     */
    @Test
    fun validityAddDaysTest(){
        val a = MyDate(
            day = "18",
            month = Month.AUGUST,
            year = "2023",
            hour = "5",
            minute = "26",
            isAfternoon = true
        )
        assertEquals(a.addDays(15).isValid(), true)
    }
    /*
     * Tests the addDays() method
     * Tests whether the function may receive negative values as well
     */
    @Test
    fun subtractAddDaysTest(){
        val a = MyDate(
            day = "18",
            month = Month.AUGUST,
            year = "2023",
            hour = "5",
            minute = "26",
            isAfternoon = true
        )
        val b = MyDate(
            day = "15",
            month = Month.AUGUST,
            year = "2023",
            hour = "5",
            minute = "26",
            isAfternoon = true
        )
        assertEquals(a.addDays(-3), b)
    }
    /*
     * Tests the addDays() method
     * Tests how the method handles having the month and year turn over while
     * retaining accuracy
     */
    @Test
    fun changeOverAddDaysTest(){
        val a = MyDate(
            day = "18",
            month = Month.AUGUST,
            year = "2022",
            hour = "5",
            minute = "26",
            isAfternoon = true
        )
        val b = MyDate(
            day = "18",
            month = Month.AUGUST,
            year = "2023",
            hour = "5",
            minute = "26",
            isAfternoon = true
        )
        assertEquals(a.addDays(365), b)
    }

    /*
     * Tests the minuteDifference() method
     * Tests the basic functionality of the method with inputs well within
     * expected values
     */
    @Test
    fun basicMinuteDiffTest(){
        val b = MyDate(
            day = "17",
            month = Month.AUGUST,
            year = "2023",
            hour = "6",
            minute = "26",
            isAfternoon = false
        )
        val a = MyDate(
            day = "17",
            month = Month.AUGUST,
            year = "2023",
            hour = "5",
            minute = "26",
            isAfternoon = false
        )
        assertEquals(a.minuteDifference(b), 60)
    }

    /*
     * Tests the isValid() method
     * Tests the basic functionality of the method with inputs well within
     * expected values
     */
    @Test
    fun basicIsValidTest(){
        val date = MyDate(
            day = "5",
            month = Month.SEPTEMBER,
            year = "2023",
            minute = "15",
            hour = "12",
            isAfternoon = true
        )
        assertEquals(date.isValid(),true)
        date.day = "31"
        assertEquals(date.isValid(), false)
        date.day = "30"
        date.hour = "14"
        assertEquals(date.isValid(), false)
        date.hour = "12"
        date.minute = "74"
        assertEquals(date.isValid(), false)
    }
    /*
     * Tests the isValid() method
     * Tests the basic functionality of the method with input using the default
     * constructor.
     *
     * NOTE: Technically, the result of this test may change depending on the
     * date and time of the system. This serves more as a test of the
     * constructor than of the method. See other test(s) as proof of
     * functionality
     */
    @Test
    fun defaultIsValidTest(){
        assertEquals(MyDate().isValid(),true)
    }
    /*
     * Tests the isValid() method
     * Tests whether February 29 is only valid on a leap year
     *
     * NOTE: This serves also as a basic test of leapYear, since it is a
     * private method called by isValid
     */
    @Test
    fun leapYearIsValidTest(){
        val date = MyDate(
            day = "29",
            month = Month.FEBRUARY,
            year = "2024"
        )
        assertEquals(date.isValid(), true)
        date.year = "2023"
        assertEquals(date.isValid(), false)
        date.year = "2000"
        assertEquals(date.isValid(), true)
        date.year = "3000"
        assertEquals(date.isValid(), false)
    }

    /*
     * Tests the isFuture() method
     * Tests the basic functionality of the method with inputs well within
     * expected values
     */
    @Test
    fun basicIsFutureTest(){
        var date = MyDate(
            day = (Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+1).toString(),
        )
        assertEquals(date.isFuture(), true)
        date = date.addDays(-2)
        assertEquals(date.isFuture(), false)
    }
    /*
     * Tests the isFuture() method
     * Tests the basic functionality of the method with input using the default
     * constructor.
     *
     * NOTE: Technically, the result of this test may change depending on the
     * date and time of the system. This serves more as a test of the
     * constructor than of the method. See other test(s) as proof of
     * functionality
     */
    @Test
    fun defaultIsFutureTest(){
        assertEquals(MyDate().isFuture(), false)
    }
}