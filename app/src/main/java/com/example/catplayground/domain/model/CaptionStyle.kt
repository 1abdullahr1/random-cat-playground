package com.example.catplayground.domain.model

enum class CaptionPosition(val displayName: String) {
    TOP("Top"),
    CENTER("Center"),
    BOTTOM("Bottom")
}

enum class CaptionColor(val displayName: String, val hexColor: Long) {
    WHITE("White", 0xFFFFFFFF),
    BUTTER("Butter", 0xFFFFE6B6),
    MINT("Mint", 0xFFD7F2E5),
    LAVENDER("Lavender", 0xFFE7DEFF),
    YELLOW("Yellow", 0xFFFFEB3B),
    RED("Red", 0xFFFF5252),
    BLACK("Black", 0xFF000000)
}

object CaptionPresets {
    val presets = listOf(
        "I'm not lazy, I'm conserving energy.",
        "If I fits, I sits.",
        "Feed me right meow!",
        "Bow down, mortal human.",
        "Cat-titude is everything.",
        "Send treats immediately.",
        "You called? Where is my tuna?",
        "Zero regrets, maximum purrs.",
        "Plotting world domination.",
        "Nap time is all the time.",
        "Judge me when you can catch mice.",
        "I saw a bird outside. Panic mode on."
    )

    fun getRandomPreset(): String {
        return presets.random()
    }
}
