# M-Helper

Minecraft Assist Features Mod focused on helping players execute perfect mace slams in version **1.21.5**.

## Overview

The Mace Hit Helper aims to:

* Predict the optimal swing window for mace slam attacks when falling.
* Guide crosshair aim toward the best target during the descent.
* Automate elytra equipping/gliding so you can chain aerial combos without manual inventory juggling.

This repository now ships a Fabric-based client mod that provides those quality-of-life helpers. All logic runs client-side and
can be toggled with the `G` key. Hit `B` for an instant panic disable and `O` to open the in-game settings panel.

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
   * `O` — open the helper configuration screen

## Roadmap

- [x] Fabric project scaffolding
- [x] Implement mace slam timing tracker and prediction
- [x] HUD overlay for timing bar and aim ring
- [x] Elytra auto-equip and restore logic
- [x] Config screen UI with accessibility tuning

Contributions welcome—open an issue or PR with ideas for additional combat helpers.

