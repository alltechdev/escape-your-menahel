# Escape Your Menahel 🎩

The ultimate yeshiva breakout platformer, designed for **kosher flip and bar
phones**: keypad + d-pad controls, portrait orientation, and a fixed virtual
resolution that renders identically on the tiniest screens.

You left the beis medrash early. He saw. He ALWAYS sees. Twelve levels stand
between you and the 4:15 bus to freedom.

## Features

- **12 handcrafted levels** — from the lunchroom, through the beis medrash and
  Detention Row, to the roof (assur!), the parking lot, and the 4:15 bus stop
- **The Menahel** — chases you relentlessly, gets faster every level, and
  yells authentic mussar in speech bubbles ("Where's your HAT?!", "I ran track
  in '87!")
- **The Mashgiach** — patrols with a clipboard from level 4 on; stun him and
  he has to recount the attendance sheet
- **Felafel launcher** — the classic. 300ms cooldown, aims where you face
- **Power-ups** — Kavana Coffee (speed), seltzer (triple jump), and Bubby's
  kugel (absorbs one catch)
- **Rugelach collectibles**, score with time bonuses, 3 lives (displayed as
  black hats, naturally), and a persistent high score
- **Pause = Mincha Break**

## Controls (keypad / d-pad)

| Key | Action |
|---|---|
| `4` / `6` or LEFT / RIGHT | Move |
| `2` or UP | Jump (press twice for double jump) |
| `5` or OK / CENTER | Start · run · shoot felafel · confirm |
| `*`, `P`, or MENU | Mincha break (pause) |
| BACK | Exit the game |

## Building

```bash
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`. CI builds it automatically on
every push (see `.github/workflows/android-ci.yml`) and uploads it as the
`escape-your-menahel-debug` artifact.

## Architecture

The game simulates in a fixed 1080×1920 virtual world scaled to the physical
surface, so layout, physics, and text are identical on every screen size.

```
com.escapegame
├── core          GameConfig — every tuning constant in one place
├── model         Phases, themes, level/platform/pickup specs
├── levels        The 12-level catalog + all flavor text
├── entities      Talmid, Menahel, Mashgiach, FelafelBall, pickups
├── engine        GameEngine — state machine, rules, scoring
├── render        Theme backgrounds, HUD, overlay screens
├── persistence   High-score storage
├── GameView      Surface + render thread + key mapping
└── MainActivity
```

Compatible with Android 5.0+ (minSdk 21). No touchscreen required.

*Est. 5747 · Accredited by absolutely nobody.*
