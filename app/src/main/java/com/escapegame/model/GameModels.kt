package com.escapegame.model

/** Top-level state machine phases for the game. */
enum class GamePhase {
    INTRO,
    DIFFICULTY_SELECT,
    MODE_SELECT,
    LEADERBOARD,
    LEVEL_INTRO,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY
}

/** Visual theme for a level; each theme gets its own background rendering. */
enum class LevelTheme {
    LUNCHROOM,
    HALLWAY,
    CLASSROOM,
    BEIS_MEDRASH,
    GYM,
    LIBRARY,
    KITCHEN,
    DETENTION,
    ROOFTOP,
    PARKING_LOT,
    BUS_STOP,
    COAT_ROOM,
    MIKVEH,
    SUKKAH,
    SHUL,
    SIMCHA_HALL
}

/** Power-up varieties and their on-screen labels. */
enum class PowerUpType(val label: String) {
    COFFEE("KAVANA COFFEE! Speed boost!"),
    SELTZER("SELTZER! Triple jump!"),
    KUGEL("KUGEL SHIELD! One free catch!")
}

/** Per-level gameplay twists, announced on the level card. */
enum class Modifier(val announcement: String) {
    SLIPPERY("WET FLOOR! No traction!"),
    DARK("LIGHTS OUT! Stay close to your ner!"),
    WIND("WINDY! Hold onto your hat!"),
    ASSISTANT_MENAHEL("TWO MENAHELIM?! Oy!")
}

/**
 * Difficulty settings — pick your madreiga. Affects lives, enemy speeds,
 * when the chalk starts flying, and the score multiplier.
 */
enum class Difficulty(
    val label: String,
    val description: String,
    val lives: Int,
    val menahelSpeedFactor: Float,
    val mashgiachSpeedBonus: Float,
    val chalkFromLevel: Int,
    val scoreFactor: Float
) {
    KVETCH("KVETCH", "5 hats. Slower Menahel. Nobody judges. (Everybody judges.)", 5, 0.8f, 0f, 14, 0.75f),
    BAAL_HABOS("BAAL HABOS", "3 hats. The standard chinuch experience.", 3, 1f, 0f, 10, 1f),
    MASMID("MASMID", "2 hats. Faster Menahel. The chalk starts early.", 2, 1.2f, 0.6f, 6, 1.5f),
    GADOL_HADOR("GADOL HADOR", "1 hat. Everything is faster. Hatzlacha.", 1, 1.35f, 1.2f, 3, 2f)
}

/** Lifetime achievements — "semichos" — persisted across launches. */
enum class Achievement(val title: String) {
    SEMICHA("SEMICHA IN ESCAPOLOGY! Finished all 18 levels"),
    FELAFEL_SNIPER("FELAFEL SNIPER! 25 lifetime stuns"),
    ZRIZUS("ZRIZUS! Cleared a level in under 15 seconds"),
    KIBUD_RUGELACH("KIBUD RUGELACH! Every rugelach in one level"),
    SHOMER_NAFSHO("SHOMER NAFSHO! Escaped without losing a hat")
}

/** A solid platform in world (pixel) coordinates. */
data class Platform(val x: Float, val y: Float, val width: Float, val height: Float)

/**
 * A platform in screen-fraction coordinates so levels look right at any
 * resolution. [fx] and [fy] locate the platform's top-left corner as fractions
 * of screen width/height; [fw] is its width as a fraction of screen width.
 */
data class PlatformSpec(val fx: Float, val fy: Float, val fw: Float)

/** A rugelach collectible, positioned by screen fractions (center point). */
data class RugelachSpec(val fx: Float, val fy: Float)

/** A power-up, positioned by screen fractions (center point). */
data class PowerUpSpec(val fx: Float, val fy: Float, val type: PowerUpType)

/**
 * A patrolling mashgiach. Walks between [fxStart] and [fxEnd]; [fy] is the
 * fraction-height of the surface he stands on (platform top or floor top).
 */
data class PatrollerSpec(val fxStart: Float, val fxEnd: Float, val fy: Float)

/** Complete, resolution-independent description of one level. */
data class LevelDefinition(
    val number: Int,
    val name: String,
    val quip: String,
    val theme: LevelTheme,
    val menahelSpeed: Float,
    val platforms: List<PlatformSpec>,
    val rugelach: List<RugelachSpec>,
    val powerUps: List<PowerUpSpec> = emptyList(),
    val patrollers: List<PatrollerSpec> = emptyList(),
    val modifiers: Set<Modifier> = emptySet(),
    /** If set, the level must be finished before the van leaves. */
    val timeLimitSeconds: Int? = null
)
