package com.akshar.wallpaperengine.domain.solar

import java.util.Calendar
import kotlin.math.*

data class SolarTimes(
    val dawnHour: Int,
    val dawnMinute: Int,
    val sunriseHour: Int,
    val sunriseMinute: Int,
    val solarNoonHour: Int,
    val solarNoonMinute: Int,
    val goldenHour: Int,
    val goldenMinute: Int,
    val sunsetHour: Int,
    val sunsetMinute: Int,
    val duskHour: Int,
    val duskMinute: Int
)

/**
 * Astronomical Solar Position & Sunrise/Sunset Engine.
 * Implements standard NOAA solar calculation formulas to compute exact dawn, sunrise, solar noon,
 * golden hour, sunset, and dusk for given geographic coordinates or timezone offsets.
 */
object SolarCalculator {

    fun calculateSolarTimes(
        calendar: Calendar = Calendar.getInstance(),
        latitude: Double? = null,
        longitude: Double? = null
    ): SolarTimes {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val tzOffsetHours = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (1000.0 * 60.0 * 60.0)

        val effLon = longitude ?: (tzOffsetHours * 15.0)
        val effLat = latitude ?: 35.0

        // Fractional year in radians
        val gamma = 2.0 * Math.PI / 365.0 * (dayOfYear - 1)

        // Equation of time (in minutes)
        val eqtime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

        // Solar declination angle (in radians)
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
                0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)

        val latRad = Math.toRadians(effLat)

        // Standard zenith angles
        val zenithSunrise = Math.toRadians(90.833) // Sunrise/Sunset
        val zenithCivilDawn = Math.toRadians(96.0)  // Civil Dawn/Dusk
        val zenithGoldenHour = Math.toRadians(84.0) // Golden Hour

        fun calculateHourAngle(zenith: Double): Double? {
            val cosHa = (cos(zenith) / (cos(latRad) * cos(decl))) - (tan(latRad) * tan(decl))
            if (cosHa < -1.0 || cosHa > 1.0) return null // Polar day / night
            return acos(cosHa)
        }

        val haSunrise = calculateHourAngle(zenithSunrise) ?: Math.toRadians(90.0)
        val haDawn = calculateHourAngle(zenithCivilDawn) ?: Math.toRadians(96.0)
        val haGolden = calculateHourAngle(zenithGoldenHour) ?: Math.toRadians(84.0)

        // Solar noon in minutes from local midnight
        val solarNoonMinutes = 720 - (4 * effLon) - eqtime + (tzOffsetHours * 60)

        val sunriseMinutes = solarNoonMinutes - (Math.toDegrees(haSunrise) * 4)
        val sunsetMinutes = solarNoonMinutes + (Math.toDegrees(haSunrise) * 4)

        val dawnMinutes = solarNoonMinutes - (Math.toDegrees(haDawn) * 4)
        val duskMinutes = solarNoonMinutes + (Math.toDegrees(haDawn) * 4)

        val goldenHourMinutes = sunsetMinutes - (Math.toDegrees(haGolden) * 4 - Math.toDegrees(haSunrise) * 4).absoluteValue

        fun minutesToHourMinute(minutes: Double): Pair<Int, Int> {
            val normalized = ((minutes % 1440) + 1440) % 1440
            val h = (normalized / 60).toInt().coerceIn(0, 23)
            val m = (normalized % 60).toInt().coerceIn(0, 59)
            return Pair(h, m)
        }

        val (sH, sM) = minutesToHourMinute(sunriseMinutes)
        val (setH, setM) = minutesToHourMinute(sunsetMinutes)
        val (dH, dM) = minutesToHourMinute(dawnMinutes)
        val (duskH, duskM) = minutesToHourMinute(duskMinutes)
        val (noonH, noonM) = minutesToHourMinute(solarNoonMinutes)
        val (goldH, goldM) = minutesToHourMinute(goldenHourMinutes)

        return SolarTimes(
            dawnHour = dH,
            dawnMinute = dM,
            sunriseHour = sH,
            sunriseMinute = sM,
            solarNoonHour = noonH,
            solarNoonMinute = noonM,
            goldenHour = goldH,
            goldenMinute = goldM,
            sunsetHour = setH,
            sunsetMinute = setM,
            duskHour = duskH,
            duskMinute = duskM
        )
    }
}
