package com.escapegame.core

/**
 * Central tuning knobs for the whole game.
 *
 * The game simulates and draws inside a fixed virtual world of
 * [WORLD_WIDTH] x [WORLD_HEIGHT] (portrait) which is scaled to the physical
 * surface. That keeps layout, physics, and text identical on every screen
 * size, from tiny phones to tablets, with d-pad-only controls throughout.
 *
 * All distances below are in world units (virtual pixels), all durations in
 * frames (~60 per second) unless noted otherwise.
 */
object GameConfig {
    // Loop
    const val FRAME_MILLIS = 16L // ~60 FPS

    // Virtual world. Keypad phones play fullscreen in the portrait world;
    // touch mode plays in the landscape world shown inside the handheld
    // shell's LCD. The portrait dimensions double as the shell's coordinate
    // space. Levels are defined as fractions, so they re-flow to either.
    const val WORLD_WIDTH = 1080f
    const val WORLD_HEIGHT = 1920f
    const val LANDSCAPE_WORLD_WIDTH = 1440f
    const val LANDSCAPE_WORLD_HEIGHT = 1080f

    // World
    const val FLOOR_TOP_FRACTION = 0.906f
    const val GRAVITY = 1.6f

    // Player
    const val PLAYER_WIDTH = 60f
    const val PLAYER_HEIGHT = 75f
    const val PLAYER_WALK_SPEED = 11f
    const val PLAYER_RUN_SPEED = 19f
    const val PLAYER_JUMP_POWER = -33f
    const val PLAYER_FRICTION = 0.85f
    const val PLAYER_BASE_JUMPS = 2
    const val PLAYER_SELTZER_JUMPS = 3
    const val COFFEE_SPEED_MULTIPLIER = 1.5f

    // Effect durations
    const val COFFEE_DURATION = 480
    const val SELTZER_DURATION = 600
    const val RESPAWN_INVINCIBILITY = 120
    const val SHIELD_HIT_INVINCIBILITY = 60

    // Enemies
    const val MENAHEL_WIDTH = 62f
    const val MENAHEL_HEIGHT = 85f
    const val MENAHEL_STUN_FRAMES = 120
    const val MASHGIACH_WIDTH = 55f
    const val MASHGIACH_HEIGHT = 75f
    const val MASHGIACH_SPEED = 4f
    const val MASHGIACH_STUN_FRAMES = 150
    const val MENAHEL_CATCH_DISTANCE = 75f
    const val MASHGIACH_CATCH_DISTANCE = 65f

    // Menahel speech bubbles
    const val QUOTE_MIN_COOLDOWN = 240
    const val QUOTE_MAX_COOLDOWN = 480
    const val QUOTE_DURATION = 130

    // Projectiles
    const val FELAFEL_COOLDOWN_MILLIS = 300L
    const val FELAFEL_SPEED = 20f
    const val FELAFEL_RADIUS = 12f

    // Scoring
    const val SCORE_RUGELACH = 100
    const val SCORE_POWER_UP = 50
    const val SCORE_LEVEL_CLEAR = 500
    const val TIME_BONUS_MAX = 3000
    const val TIME_BONUS_DECAY_PER_SECOND = 25

    // Game structure
    const val STARTING_LIVES = 3
    const val LEVEL_INTRO_FRAMES = 200

    // Exit door
    const val DOOR_WIDTH = 100f
    const val DOOR_HEIGHT = 170f
}
