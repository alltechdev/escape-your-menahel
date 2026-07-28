package com.escapegame.levels

import com.escapegame.model.LevelDefinition
import com.escapegame.model.LevelTheme
import com.escapegame.model.PatrollerSpec
import com.escapegame.model.PlatformSpec
import com.escapegame.model.PowerUpSpec
import com.escapegame.model.PowerUpType
import com.escapegame.model.RugelachSpec

/**
 * The complete level catalog plus all flavor text.
 *
 * Coordinates are screen fractions of the virtual world (portrait). Platform
 * tiers used throughout (fraction of world height for the platform top):
 * ground top is at ~0.906; tiers step up by ~0.10, each reachable with a
 * single jump, so layouts stay fair on every screen.
 */
object Levels {

    // Platform top tiers (fractions of world height)
    private const val T1 = 0.82f
    private const val T2 = 0.72f
    private const val T3 = 0.62f
    private const val T4 = 0.52f
    private const val T5 = 0.42f

    /** Fraction-height of the floor top, for patrollers standing on the ground. */
    const val FLOOR_TOP = 0.906f

    val all: List<LevelDefinition> = listOf(
        LevelDefinition(
            number = 1,
            name = "The Lunchroom",
            quip = "Fleishig day. The Menahel spotted your untucked shirt at bentching.",
            theme = LevelTheme.LUNCHROOM,
            menahelSpeed = 2.6f,
            platforms = listOf(
                PlatformSpec(0.12f, T1, 0.24f),
                PlatformSpec(0.45f, T2, 0.24f),
                PlatformSpec(0.70f, T1, 0.22f)
            ),
            rugelach = listOf(
                RugelachSpec(0.24f, T1 - 0.04f),
                RugelachSpec(0.57f, T2 - 0.04f),
                RugelachSpec(0.81f, T1 - 0.04f)
            )
        ),
        LevelDefinition(
            number = 2,
            name = "Hallway of the Hanhala",
            quip = "The sign says NO RUNNING IN THE HALLWAY. Nu, so run quietly.",
            theme = LevelTheme.HALLWAY,
            menahelSpeed = 2.85f,
            platforms = listOf(
                PlatformSpec(0.08f, T1, 0.20f),
                PlatformSpec(0.38f, T2, 0.22f),
                PlatformSpec(0.68f, T3, 0.22f),
                PlatformSpec(0.35f, T4, 0.20f)
            ),
            rugelach = listOf(
                RugelachSpec(0.18f, T1 - 0.04f),
                RugelachSpec(0.49f, T2 - 0.04f),
                RugelachSpec(0.79f, T3 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.45f, T4 - 0.045f, PowerUpType.COFFEE)
            )
        ),
        LevelDefinition(
            number = 3,
            name = "The Great Cholent Spill",
            quip = "Erev Shabbos. The cholent reached yad soledes bo. So did the floor.",
            theme = LevelTheme.KITCHEN,
            menahelSpeed = 3.1f,
            platforms = listOf(
                PlatformSpec(0.15f, T1, 0.22f),
                PlatformSpec(0.50f, T1, 0.22f),
                PlatformSpec(0.32f, T2, 0.22f),
                PlatformSpec(0.62f, T3, 0.24f)
            ),
            rugelach = listOf(
                RugelachSpec(0.26f, T1 - 0.04f),
                RugelachSpec(0.61f, T1 - 0.04f),
                RugelachSpec(0.43f, T2 - 0.04f),
                RugelachSpec(0.74f, T3 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.10f, FLOOR_TOP - 0.045f, PowerUpType.KUGEL)
            )
        ),
        LevelDefinition(
            number = 4,
            name = "Shiur Room 3B",
            quip = "The Rebbi stepped out for a minute. The Menahel stepped in. And the Mashgiach.",
            theme = LevelTheme.CLASSROOM,
            menahelSpeed = 3.3f,
            platforms = listOf(
                PlatformSpec(0.10f, T1, 0.22f),
                PlatformSpec(0.42f, T2, 0.24f),
                PlatformSpec(0.72f, T1, 0.20f),
                PlatformSpec(0.20f, T3, 0.22f)
            ),
            rugelach = listOf(
                RugelachSpec(0.21f, T1 - 0.04f),
                RugelachSpec(0.54f, T2 - 0.04f),
                RugelachSpec(0.31f, T3 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.82f, T1 - 0.045f, PowerUpType.SELTZER)
            ),
            patrollers = listOf(
                PatrollerSpec(0.42f, 0.66f, T2)
            )
        ),
        LevelDefinition(
            number = 5,
            name = "The Beis Medrash",
            quip = "Quiet please. Escaping b'iyun in progress.",
            theme = LevelTheme.BEIS_MEDRASH,
            menahelSpeed = 3.5f,
            platforms = listOf(
                PlatformSpec(0.08f, T1, 0.20f),
                PlatformSpec(0.36f, T2, 0.20f),
                PlatformSpec(0.64f, T2, 0.20f),
                PlatformSpec(0.50f, T3, 0.20f),
                PlatformSpec(0.18f, T4, 0.20f)
            ),
            rugelach = listOf(
                RugelachSpec(0.18f, T1 - 0.04f),
                RugelachSpec(0.46f, T2 - 0.04f),
                RugelachSpec(0.74f, T2 - 0.04f),
                RugelachSpec(0.60f, T3 - 0.04f),
                RugelachSpec(0.28f, T4 - 0.04f)
            ),
            patrollers = listOf(
                PatrollerSpec(0.10f, 0.45f, FLOOR_TOP)
            )
        ),
        LevelDefinition(
            number = 6,
            name = "The Gym (Social Hall)",
            quip = "Folding chairs from last night's vort everywhere. The Menahel ran track in '87. Allegedly.",
            theme = LevelTheme.GYM,
            menahelSpeed = 3.75f,
            platforms = listOf(
                PlatformSpec(0.12f, T1, 0.18f),
                PlatformSpec(0.44f, T1, 0.18f),
                PlatformSpec(0.74f, T1, 0.18f),
                PlatformSpec(0.28f, T3, 0.20f),
                PlatformSpec(0.58f, T3, 0.20f)
            ),
            rugelach = listOf(
                RugelachSpec(0.21f, T1 - 0.04f),
                RugelachSpec(0.53f, T1 - 0.04f),
                RugelachSpec(0.83f, T1 - 0.04f),
                RugelachSpec(0.38f, T3 - 0.04f),
                RugelachSpec(0.68f, T3 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.05f, FLOOR_TOP - 0.045f, PowerUpType.COFFEE),
                PowerUpSpec(0.90f, T1 - 0.045f, PowerUpType.KUGEL)
            ),
            patrollers = listOf(
                PatrollerSpec(0.28f, 0.78f, T3)
            )
        ),
        LevelDefinition(
            number = 7,
            name = "The Otzar HaSeforim",
            quip = "SHHHH. (Also: run.)",
            theme = LevelTheme.LIBRARY,
            menahelSpeed = 4.0f,
            platforms = listOf(
                PlatformSpec(0.10f, T1, 0.18f),
                PlatformSpec(0.38f, T2, 0.18f),
                PlatformSpec(0.66f, T3, 0.18f),
                PlatformSpec(0.38f, T4, 0.18f),
                PlatformSpec(0.10f, T5, 0.18f)
            ),
            rugelach = listOf(
                RugelachSpec(0.19f, T1 - 0.04f),
                RugelachSpec(0.47f, T2 - 0.04f),
                RugelachSpec(0.75f, T3 - 0.04f),
                RugelachSpec(0.47f, T4 - 0.04f),
                RugelachSpec(0.19f, T5 - 0.04f)
            ),
            patrollers = listOf(
                PatrollerSpec(0.50f, 0.90f, FLOOR_TOP)
            )
        ),
        LevelDefinition(
            number = 8,
            name = "The Kitchen",
            quip = "Fresh felafel. Unlimited ammo. B'dieved, also lunch.",
            theme = LevelTheme.KITCHEN,
            menahelSpeed = 4.25f,
            platforms = listOf(
                PlatformSpec(0.14f, T1, 0.20f),
                PlatformSpec(0.48f, T2, 0.20f),
                PlatformSpec(0.76f, T1, 0.18f),
                PlatformSpec(0.16f, T3, 0.18f),
                PlatformSpec(0.55f, T4, 0.20f)
            ),
            rugelach = listOf(
                RugelachSpec(0.24f, T1 - 0.04f),
                RugelachSpec(0.58f, T2 - 0.04f),
                RugelachSpec(0.85f, T1 - 0.04f),
                RugelachSpec(0.25f, T3 - 0.04f),
                RugelachSpec(0.65f, T4 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.65f, T4 - 0.09f, PowerUpType.SELTZER)
            ),
            patrollers = listOf(
                PatrollerSpec(0.14f, 0.60f, FLOOR_TOP)
            )
        ),
        LevelDefinition(
            number = 9,
            name = "Detention Row",
            quip = "No one has ever escaped detention. Be the first. B'ezras Hashem.",
            theme = LevelTheme.DETENTION,
            menahelSpeed = 4.5f,
            platforms = listOf(
                PlatformSpec(0.06f, T1, 0.16f),
                PlatformSpec(0.32f, T1, 0.16f),
                PlatformSpec(0.58f, T1, 0.16f),
                PlatformSpec(0.20f, T3, 0.16f),
                PlatformSpec(0.46f, T3, 0.16f),
                PlatformSpec(0.72f, T3, 0.16f)
            ),
            rugelach = listOf(
                RugelachSpec(0.14f, T1 - 0.04f),
                RugelachSpec(0.40f, T1 - 0.04f),
                RugelachSpec(0.66f, T1 - 0.04f),
                RugelachSpec(0.28f, T3 - 0.04f),
                RugelachSpec(0.54f, T3 - 0.04f),
                RugelachSpec(0.80f, T3 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.90f, FLOOR_TOP - 0.045f, PowerUpType.KUGEL)
            ),
            patrollers = listOf(
                PatrollerSpec(0.06f, 0.48f, T1),
                PatrollerSpec(0.46f, 0.88f, T3)
            )
        ),
        LevelDefinition(
            number = 10,
            name = "The Roof (Assur!)",
            quip = "You are absolutely not allowed up here. That's the whole point.",
            theme = LevelTheme.ROOFTOP,
            menahelSpeed = 4.75f,
            platforms = listOf(
                PlatformSpec(0.10f, T1, 0.18f),
                PlatformSpec(0.40f, T2, 0.18f),
                PlatformSpec(0.70f, T2, 0.18f),
                PlatformSpec(0.25f, T4, 0.18f),
                PlatformSpec(0.55f, T4, 0.18f)
            ),
            rugelach = listOf(
                RugelachSpec(0.19f, T1 - 0.04f),
                RugelachSpec(0.49f, T2 - 0.04f),
                RugelachSpec(0.79f, T2 - 0.04f),
                RugelachSpec(0.34f, T4 - 0.04f),
                RugelachSpec(0.64f, T4 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.05f, FLOOR_TOP - 0.045f, PowerUpType.COFFEE)
            ),
            patrollers = listOf(
                PatrollerSpec(0.40f, 0.88f, T2)
            )
        ),
        LevelDefinition(
            number = 11,
            name = "The Parking Lot",
            quip = "The Menahel's minivan has 340,000 miles and the koach of a lion.",
            theme = LevelTheme.PARKING_LOT,
            menahelSpeed = 5.0f,
            platforms = listOf(
                PlatformSpec(0.08f, T1, 0.16f),
                PlatformSpec(0.34f, T2, 0.16f),
                PlatformSpec(0.60f, T1, 0.16f),
                PlatformSpec(0.82f, T3, 0.16f),
                PlatformSpec(0.30f, T4, 0.16f),
                PlatformSpec(0.56f, T5, 0.16f)
            ),
            rugelach = listOf(
                RugelachSpec(0.16f, T1 - 0.04f),
                RugelachSpec(0.42f, T2 - 0.04f),
                RugelachSpec(0.68f, T1 - 0.04f),
                RugelachSpec(0.90f, T3 - 0.04f),
                RugelachSpec(0.38f, T4 - 0.04f),
                RugelachSpec(0.64f, T5 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.64f, T5 - 0.09f, PowerUpType.SELTZER),
                PowerUpSpec(0.05f, FLOOR_TOP - 0.045f, PowerUpType.KUGEL)
            ),
            patrollers = listOf(
                PatrollerSpec(0.20f, 0.70f, FLOOR_TOP)
            )
        ),
        LevelDefinition(
            number = 12,
            name = "The 4:15 Bus",
            quip = "The bus to freedom is at the corner. The Menahel is faster than he looks. NU, RUN!",
            theme = LevelTheme.BUS_STOP,
            menahelSpeed = 5.4f,
            platforms = listOf(
                PlatformSpec(0.06f, T1, 0.15f),
                PlatformSpec(0.30f, T2, 0.15f),
                PlatformSpec(0.54f, T3, 0.15f),
                PlatformSpec(0.78f, T2, 0.15f),
                PlatformSpec(0.30f, T4, 0.15f),
                PlatformSpec(0.06f, T5, 0.15f),
                PlatformSpec(0.55f, T5, 0.15f)
            ),
            rugelach = listOf(
                RugelachSpec(0.13f, T1 - 0.04f),
                RugelachSpec(0.37f, T2 - 0.04f),
                RugelachSpec(0.61f, T3 - 0.04f),
                RugelachSpec(0.85f, T2 - 0.04f),
                RugelachSpec(0.37f, T4 - 0.04f),
                RugelachSpec(0.13f, T5 - 0.04f),
                RugelachSpec(0.62f, T5 - 0.04f)
            ),
            powerUps = listOf(
                PowerUpSpec(0.90f, FLOOR_TOP - 0.045f, PowerUpType.COFFEE)
            ),
            patrollers = listOf(
                PatrollerSpec(0.30f, 0.75f, FLOOR_TOP),
                PatrollerSpec(0.30f, 0.66f, T2)
            )
        )
    )

    /** Things the Menahel yells mid-chase, shown in his speech bubble. */
    val menahelQuotes: List<String> = listOf(
        "NU?! STOP!",
        "Where's your HAT?!",
        "My office. NOW.",
        "Is that a FELAFEL?!",
        "You missed Shacharis!",
        "Is that how a ben Torah runs?!",
        "Tuck in your shirt!",
        "A shanda!",
        "GEVALT!",
        "Takeh?! TAKEH?!",
        "Your chavrusa is waiting!",
        "This goes on your permanent record!",
        "I ran track in '87!",
        "No recess until Lag BaOmer!",
        "Mamash unbelievable!",
        "Wait till I call your father!",
        "The Rosh Yeshiva will hear of this!",
        "You call that a gartel?!"
    )

    /** Random headline for the game-over screen. */
    val gameOverLines: List<String> = listOf(
        "Detention until Rosh Chodesh.",
        "The Menahel always wins. Gam zu l'tova?",
        "Your permanent record is now a full masechta.",
        "Nu nu. Back to seder.",
        "Oy vey. So close.",
        "He gave you the LOOK. It's over.",
        "Straight to mussar shmuess with you."
    )

    /** Lines for the victory screen, in display order. */
    val victoryLines: List<String> = listOf(
        "You escaped the yeshiva!",
        "Enjoy the freedom. Breathe the air.",
        "(See you at Shacharis. 7:30. Sharp.)"
    )
}
