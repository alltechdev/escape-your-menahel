# Escape Your Menahel 🎩

The ultimate yeshiva breakout platformer, designed for **kosher flip and bar
phones**: keypad + d-pad controls, portrait orientation, and a fixed virtual
resolution that renders identically on the tiniest screens.

You left the beis medrash early. He saw. He ALWAYS sees. Twelve levels stand
between you and the 4:15 bus to freedom.

## Features

- **18 handcrafted levels** — from the lunchroom, through the beis medrash,
  Detention Row, the roof (assur!), the coat room, the mikveh, the
  fire-escape sukkah, the shul kiddush, and the simcha hall, all the way to
  the Mesivta Van
- **Level modifiers** — wet floors (no traction), lights-out levels (follow
  your little circle of ner), rooftop wind, the Assistant Menahel (two
  menahelim?!), and a final level where the van leaves in 75 seconds
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
- **Bein Hazmanim mode** — endless procedurally generated days with rising
  danger, random modifiers, and a persistent best-day record. There is no
  bein hazmanim from the Menahel
- **Choose your madreiga** — four difficulties: KVETCH (5 hats, slower
  Menahel), BAAL HABOS (the standard chinuch experience), MASMID (2 hats,
  faster, early chalk), and GADOL HADOR (1 hat, everything faster,
  hatzlacha) with score multipliers from 0.75x to 2x; your choice is
  remembered
- **Chalk fire** — from level 10 the Menahel throws chalk with decades-honed
  accuracy; getting hit delivers mussar (half speed while you absorb it)
- **Semichos (achievements)** — five lifetime achievements (Felafel Sniper,
  Zrizus, Kibud Rugelach, Shomer Nafsho, and full Semicha in Escapology)
  plus lifetime escape stats, persisted across launches
- **PA announcements** — the yeshiva intercom interrupts at random
  ("Reminder — the elevator is still fleishig.")
- **Funky klezmer chiptune soundtrack + sound effects** — a D-freygish loop
  and all SFX (jump blips, felafel pfft, pickup dings, stun warbles, caught
  womps, level fanfares) synthesized at runtime, zero audio assets. Mute
  with `#`/`0`/`M` on keypads or the ♪ button on the shell; the choice is
  remembered
- **ESCAPE BOY COLOR™ mode** — on touchscreen-only devices the game becomes
  a full retro handheld in classic yellow: a wide landscape LCD up top,
  d-pad + A/B + SELECT/START on the body below. The level re-flows into a
  landscape world for the LCD. Never shown when a physical keypad/d-pad is
  present — keypad phones keep the fullscreen portrait game

## Controls (keypad / d-pad)

| Key | Action |
|---|---|
| `4` / `6` or LEFT / RIGHT | Move |
| `2` or UP | Jump (press twice for double jump) |
| `5` or OK / CENTER | Start · run · shoot felafel · confirm |
| `*`, `P`, or MENU | Mincha break (pause) |
| `#`, `0`, or `M` | Klezmer on/off |
| BACK | Exit the game |

On touchscreens with no physical keys: d-pad moves, **B** jumps, **A** runs +
shoots felafel, **START** pauses, and any tap confirms menus.

Hybrid devices with both a keypad/d-pad **and** a touchscreen are treated as
keypad devices: fullscreen game, no on-screen controls, touches ignored.

## Global leaderboard (no server, just GitHub — and fully optional)

**No internet is needed to play.** The leaderboard is a strictly optional
extra: fetching and submitting only happen on demand, and every failure
degrades silently back to normal offline play.

The game shows the global top 10 (press `7`, or SELECT on the title screen in
touch mode), fetched anonymously from `leaderboard.json` in this repo.

**Live submissions**: when a run ends, the game automatically submits your
score under an auto-generated handle (e.g. `Shmerel-382`) by opening a
GitHub issue via a build-time token; a GitHub Action validates the checksum,
keeps each player's best score per mode, commits the updated
`leaderboard.json`, and closes the issue. No server anywhere.

**Maintainer setup for live submissions**: create a fine-grained PAT with
*Issues: write* scoped to only this repository (a dedicated bot account is
best), and add it as an Actions secret named `LEADERBOARD_TOKEN`. CI injects
it into the APK at build time. Know the trade-off: anyone can extract a token
from a public APK; the blast radius is limited to opening issues here, and
you can revoke/rotate at any time. Without the secret, builds fall back to
manual submission.

**Manual submissions** always work: note the leaderboard code on the
game-over/victory screen and open an issue titled:

```
SCORE: <your code> <story|endless> <KVETCH|BAAL_HABOS|MASMID|GADOL_HADOR>
```

Anti-cheat is checksum-grade — this is an honor system among bnei Torah.

## Building

```bash
./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/release/`. CI builds it automatically
on every push (see `.github/workflows/android-ci.yml`) and uploads it as the
`escape-your-menahel-release` artifact (signed with the debug key so it
installs directly).

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
├── audio         Runtime-synthesized klezmer chiptune
├── persistence   High score + settings storage
├── GameView      Surface + render thread + key mapping
└── MainActivity
```

Compatible with Android 5.0+ (minSdk 21). No touchscreen required.

*Est. 5747 · Accredited by absolutely nobody.*
