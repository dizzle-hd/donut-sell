# Donut Sell

A Fabric mod for **Minecraft 26.3**. It sells a chosen item one hotbar slot at a time through `/ah sell <price>` and is configured in **OneConfig / OneClient** (press Right Shift → **Donut Sell**).

> ⚠️ **Only use it while you are at your PC.** Several protections stop the mod as soon as you are AFK. Check the server's current rules yourself before using it (for DonutSMP: in-game or on the official Discord). You use it at your own risk.

## What it does

1. **Fill the hotbar:** it opens your inventory and moves the item into empty hotbar slots one stack at a time. Each move is the same thing a player does by hovering over the stack and pressing the number key. There is a random pause between clicks. Then it closes the inventory again.
2. **Sell:** it switches to each filled hotbar slot in turn (1 → 9, or in random order) and sends `/ah sell <price>`.
3. **Confirmation menu:** if the server opens a confirmation menu, it clicks the confirm button (see the settings).
4. **Repeat:** it refills the hotbar and keeps selling until no more of the item is left, the server reports the listing limit, or you stop it.

Slots that hold other items (sword, pickaxe, …) are **never touched**. You can also exclude slots, for example `3-9`.

## Settings (Right Shift → Donut Sell)

| Section | Setting | Default |
|---|---|---|
| Sell | **Item** (item picker) | Diamond |
| Sell | **Price**: `1500`, `2.5k`, `1m`, `1,2b` … | `1k` |
| Sell | Price applies to: whole stack / per item | whole stack |
| Sell | Sell command (`{price}` gets replaced) | `ah sell {price}` |
| Sell | Max. listings per run (0 = unlimited) | 0 |
| Sell | Auto-confirm the confirmation menu + confirm words | on |
| Sell | Stop words in chat (e.g. limit reached) | `maximum, limit, …` |
| Sell | Throttle words in chat (`too fast`, `cooldown`, …): pause 2.5 s, retry and slow down instead of stopping | on |
| Hotbar | Allowed slots | `1-9` |
| Hotbar | Random slot order | off |
| Timing | **Speed**: Fast (≈0.5 s per listing) / Normal / Careful / Custom | Fast |
| Timing | Custom pauses (min/max, randomized), only used with "Custom" | 180–380 / 250–500 / 900–1600 ms |
| Safety | AFK protection (no mouse movement → stop) | 45 s |
| Safety | Stop when the window loses focus | on |
| Safety | Stop on movement (WASD, jump, attack, use, drop) | on |

**Start/stop:** use the **K** key (rebindable under Options → Controls → Donut Sell), or the Start button in the OneConfig menu. A run started from the menu begins once you close the menu.

The mod also stops automatically if you open another menu (chat, inventory, …), if you disconnect, or if an item is rejected twice in a row.

## Installation

Put these in the `mods` folder (OneClient usually brings all of them except this mod):

- Fabric Loader ≥ 0.19.5 for Minecraft 26.3
- Fabric API
- OneConfig ≥ 1.4.4 (plus its dependencies: Fabric Language Kotlin, Compose Multiplatform)
- `donut-sell-<version>.jar`

## Download

Every push to the default branch is built automatically by GitHub Actions (`.github/workflows/build.yml`) and published as a **GitHub release** with the finished jar (see Releases in the repo). Every other branch and every pull request gets the jar as a workflow artifact.

## Building

You need JDK 25.

```bash
./gradlew build
```

The jar ends up in `build/libs/donut-sell-<version>.jar`.

### About OneConfig

OneConfig is **not** bundled into the jar. OneClient provides it at runtime. To compile, the project uses `oneconfig-stubs/` by default: a minimal copy of the OneConfig v1 API classes this mod needs, with signatures taken 1:1 from [Polyfrost/OneConfig](https://github.com/Polyfrost/OneConfig) v1.7.0. That way the build works without the Polyfrost Maven repository. If you'd rather compile against the real artifact, set `oneconfig_use_maven=true` in `gradle.properties`.

The config is registered through OneConfig's `oneconfig:config` entrypoint (`ConfigEntrypoint`). That is the official way since OneConfig 1.4.4 for a mod to get its own card in the menu.
