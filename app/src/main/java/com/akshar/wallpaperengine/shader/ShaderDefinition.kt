package com.akshar.wallpaperengine.shader

enum class ShaderStyle(val id: String, val displayName: String, val description: String) {
    THEME_DEFAULT("THEME_DEFAULT", "Theme Default", "Automatic shader matching current theme"),
    NEBULA("NEBULA", "Purple Nebula", "Slow-moving cosmic violet energy clouds"),
    AURORA("AURORA", "Aurora", "Soft flowing violet and indigo energy waves"),
    CYBER_GRID("CYBER_GRID", "Cyber Grid", "Subtle futuristic perspective grid movement"),
    ENERGY_FLOW("ENERGY_FLOW", "Energy Flow", "Glowing particle streams and energy vectors"),
    VOID("VOID", "Void Distortion", "Dark spatial distortion with subtle purple glow"),
    PARTICLES("PARTICLES", "Sakura Petals", "Floating anime particles and sakura dust");

    companion object {
        fun fromId(id: String): ShaderStyle {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: THEME_DEFAULT
        }
    }
}

enum class ShaderIntensity(val id: String, val multiplier: Float) {
    LOW("LOW", 0.4f),
    MEDIUM("MEDIUM", 0.75f),
    HIGH("HIGH", 1.2f);

    companion object {
        fun fromId(id: String): ShaderIntensity {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: MEDIUM
        }
    }
}
