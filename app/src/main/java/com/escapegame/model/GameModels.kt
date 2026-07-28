package com.escapegame.model

/** Top-level state machine phases for the game. */
enum class GamePhase {
    INTRO,
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
