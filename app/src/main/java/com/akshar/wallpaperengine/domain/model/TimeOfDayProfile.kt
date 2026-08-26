package com.akshar.wallpaperengine.domain.model

import com.akshar.wallpaperengine.domain.solar.SolarTimes
import java.util.Calendar

enum class TimeOfDayProfile(
    val displayName: String,
    val description: String,
    val preferredStyles: List<String>,
    val preferredMoods: List<String>,
    val minBrightness: Float,
    val maxBrightness: Float,
    val darkOnly: Boolean
) {
    DAWN(
        displayName = "Dawn Awakening",
        description = "Soft ambient hues, peaceful morning light",
        preferredStyles = listOf("Nature", "Minimalist", "Abstract"),
        preferredMoods = listOf("Calm", "Serene", "Ethereal"),
        minBrightness = 0.30f,
        maxBrightness = 0.65f,
        darkOnly = false
    ),
    MORNING(
        displayName = "Morning Clarity",
        description = "Crisp and vibrant natural light",
        preferredStyles = listOf("Nature", "Landscape", "Anime"),
        preferredMoods = listOf("Energetic", "Serene"),
        minBrightness = 0.40f,
        maxBrightness = 1.00f,
        darkOnly = false
    ),
    AFTERNOON(
        displayName = "Midday Brilliance",
        description = "High dynamic contrast and rich detail",
        preferredStyles = listOf("Architecture", "Anime", "Abstract", "Nature"),
        preferredMoods = listOf("Energetic", "Action"),
        minBrightness = 0.35f,
        maxBrightness = 1.00f,
        darkOnly = false
    ),
    GOLDEN_HOUR(
        displayName = "Golden Hour Glow",
        description = "Warm radiant amber, sunset tones and neon hues",
        preferredStyles = listOf("Cyberpunk", "Nature", "Anime", "Abstract"),
        preferredMoods = listOf("Serene", "Energetic", "Ethereal"),
        minBrightness = 0.20f,
        maxBrightness = 0.70f,
        darkOnly = false
    ),
    EVENING(
        displayName = "Twilight Dusk",
        description = "Deepening shadows, cosmic purples, and calming tones",
        preferredStyles = listOf("Sci-Fi", "Cyberpunk", "Abstract"),
        preferredMoods = listOf("Mysterious", "Calm", "Ethereal"),
        minBrightness = 0.10f,
        maxBrightness = 0.45f,
        darkOnly = true
    ),
    DEEP_NIGHT(
        displayName = "Midnight OLED",
        description = "Obsidian void, electric neon glows, minimum eye strain",
        preferredStyles = listOf("Monochrome", "Cyberpunk", "Sci-Fi", "Abstract"),
        preferredMoods = listOf("Mysterious", "Calm"),
        minBrightness = 0.00f,
        maxBrightness = 0.30f,
        darkOnly = true
    );

    companion object {
        fun fromCurrentTime(
            calendar: Calendar = Calendar.getInstance(),
            solarTimes: SolarTimes? = null
        ): TimeOfDayProfile {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val currentMinutes = hour * 60 + minute

            if (solarTimes != null) {
                val dawnMinutes = solarTimes.dawnHour * 60 + solarTimes.dawnMinute
                val sunriseMinutes = solarTimes.sunriseHour * 60 + solarTimes.sunriseMinute
                val noonMinutes = solarTimes.solarNoonHour * 60 + solarTimes.solarNoonMinute
                val goldenMinutes = solarTimes.goldenHour * 60 + solarTimes.goldenMinute
                val sunsetMinutes = solarTimes.sunsetHour * 60 + solarTimes.sunsetMinute
                val duskMinutes = solarTimes.duskHour * 60 + solarTimes.duskMinute

                return when {
                    currentMinutes in dawnMinutes until sunriseMinutes -> DAWN
                    currentMinutes in sunriseMinutes until noonMinutes -> MORNING
                    currentMinutes in noonMinutes until goldenMinutes -> AFTERNOON
                    currentMinutes in goldenMinutes until sunsetMinutes -> GOLDEN_HOUR
                    currentMinutes in sunsetMinutes until (duskMinutes + 60) -> EVENING
                    else -> DEEP_NIGHT
                }
            }

            // Fallback hour schedule if solar times are not provided
            return when (hour) {
                in 5..6 -> DAWN
                in 7..11 -> MORNING
                in 12..16 -> AFTERNOON
                in 17..19 -> GOLDEN_HOUR
                in 20..22 -> EVENING
                else -> DEEP_NIGHT
            }
        }
    }
}
