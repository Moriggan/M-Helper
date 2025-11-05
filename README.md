# M-Helper

Minecraft Assist Features Mod focused on helping players execute perfect mace slams in version **1.21.5** while layering in duel-ready QoL like ImGui-powered controls, critical timing helpers, and emergency landing tech.

## Overview

The Mace Hit Helper now ships:

* Predictive mace slam timing with countdown cues and adaptive aim assist rings.
* Elytra auto-equip/gliding so you can chain aerial combos without manual inventory juggling.
* 1.12-style auto clicker with configurable CPS and cooldown gating plus a critical-hit priming hop.
* Auto water MLG that equips a bucket mid fall, places water, and re-collects it on landing.
* An ImGui control center for instant toggles, sliders, timing window tuning, and recommended load-outs.

All automation runs client-side and can be paused with `G`. Hit `B` for an instant panic disable and `O` to pop open the ImGui control center (the cursor unlocks automatically; press `O` again to re-lock). Dedicated hotkeys (`H`, `J`, `K`) flip the auto clicker, critical helper, and water MLG without touching the overlay.

## Documents

* [`docs/architecture.md`](docs/architecture.md) — high-level module breakdown and technology stack.
* [`docs/mace_helper_feature_spec.md`](docs/mace_helper_feature_spec.md) — detailed behavior and configuration for timing, aim, and elytra automation features.

## Getting Started (Implementation Notes)

1. Install JDK 21.
2. Run `gradle build` (or `./gradlew build` if you have the Gradle wrapper installed) to compile the mod.
3. Copy `build/libs/mhelper-<version>.jar` into your Minecraft `mods/` folder alongside Fabric API.
4. Launch the Fabric client and use the default hotkeys:
   * `G` — toggle overlays & automation
   * `B` — panic disable (instant off switch)
   * `O` — toggle the ImGui control center
   * `H` — toggle the auto clicker module
   * `J` — toggle the critical-hit hop helper
   * `K` — toggle auto water MLG

## Roadmap

- [x] Fabric project scaffolding
- [x] Implement mace slam timing tracker and prediction
- [x] HUD overlay for timing bar, aim ring, and module status strip
- [x] Elytra auto-equip and restore logic
- [x] ImGui control center with accessibility tuning
- [x] Auto clicker, critical helper, and auto water MLG modules

Contributions welcome—open an issue or PR with ideas for additional combat helpers.

