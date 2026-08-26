package com.akshar.wallpaperengine.domain.solar

import com.akshar.wallpaperengine.domain.model.TimeOfDayProfile
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SolarCalculatorTest {

    @Test
    fun testSolarTimesCalculationForEquinox() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles")).apply {
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 20)
        }

        val solarTimes = SolarCalculator.calculateSolarTimes(calendar, 37.7749, -122.4194)

        assertTrue("Dawn should be early morning: ${solarTimes.dawnHour}", solarTimes.dawnHour in 4..7)
        assertTrue("Sunrise should be morning: ${solarTimes.sunriseHour}", solarTimes.sunriseHour in 5..8)
        assertTrue("Solar noon should be around midday: ${solarTimes.solarNoonHour}", solarTimes.solarNoonHour in 11..14)
        assertTrue("Golden hour should be late afternoon: ${solarTimes.goldenHour}", solarTimes.goldenHour in 16..20)
        assertTrue("Sunset should follow golden hour: ${solarTimes.sunsetHour}", solarTimes.sunsetHour >= solarTimes.goldenHour)
    }

    @Test
    fun testTimeOfDayProfileFromCurrentTime() {
        // Morning (9 AM)
        val morningCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }
        val morningProfile = TimeOfDayProfile.fromCurrentTime(morningCal)
        assertEquals(TimeOfDayProfile.MORNING, morningProfile)
        assertFalse(morningProfile.darkOnly)

        // Midnight (1 AM)
        val nightCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
        }
        val nightProfile = TimeOfDayProfile.fromCurrentTime(nightCal)
        assertEquals(TimeOfDayProfile.DEEP_NIGHT, nightProfile)
        assertTrue(nightProfile.darkOnly)

        // Golden Hour (18:00 / 6 PM)
        val goldenCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
        }
        val goldenProfile = TimeOfDayProfile.fromCurrentTime(goldenCal)
        assertEquals(TimeOfDayProfile.GOLDEN_HOUR, goldenProfile)
    }
}
