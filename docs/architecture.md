# M-Helper Architecture Overview

This document outlines a suggested architecture for implementing the Minecraft 1.21.5 "Mace Hit Helper" mod. The goal is to provide in-game assistance for timing mace slam attacks, aligning crosshair aim, and automatically equipping an elytra when appropriate.

## Technology Stack

* **Minecraft Version:** 1.21.5 (Java Edition)
* **Mod Loader:** Fabric (preferred for rapid iteration and client-side features)
* **Language:** Java with Kotlin optional for utility code
* **Mixins:** SpongePowered Mixin via Fabric for client event hooks
* **Rendering:** Fabric Rendering API for HUD overlays
* **Configuration:** Cloth Config API + AutoConfig for user-friendly settings menus

## High-Level Modules

| Module | Responsibility |
| ------ | -------------- |
| `core` | Initializes the mod, registers event listeners, loads config |
| `combat` | Tracks mace charge state, predicts slam timing, suggests attack windows |
| `movement` | Monitors fall speed, toggles elytra auto-equip, and cancels glide when unsafe |
| `hud` | Renders crosshair indicators, timing bars, and textual hints |
| `config` | Declares user-configurable options and persists them |

### Core Module

* Entry point via `MHelperMod` implementing `ModInitializer`.
* Registers client tick listeners (Fabric API) to update timers each frame.
* Uses `ClientPlayConnectionEvents.JOIN` / `DISCONNECT` to reset state per world.

### Combat Module

* Hooks into `AttackBlockCallback` and `AttackEntityCallback` to detect when the player starts charging a mace attack.
* Maintains an internal `MaceChargeTracker` that records:
  * Timestamp when the player begins charging.
  * Player vertical velocity and fall distance at the moment of charge.
  * Whether the player is currently gliding with an elytra.
* Predicts the **optimal slam window**:
  * Minimum fall distance: 1.5 blocks (configurable).
  * Impact damage multiplier threshold: 1.5x when fall distance ≥ 5 blocks.
  * For precise timing, compute `timeToGround` by ray tracing the player's downward velocity and collision boxes.
* Exposes `MaceHitPrediction` containing:
  * `estimatedImpactTick`
  * `criticalWindowStartTick`
  * `criticalWindowEndTick`
  * `recommendedAimVector`

### Movement Module

* Listens for player movement updates via `ClientTickEvents.END_CLIENT_TICK`.
* If player is falling with a mace equipped in main hand and chest slot not occupied by elytra:
  * Check config `autoEquipElytra`.
  * If an elytra is found in inventory and the player is in mid-air for > configurable tick threshold, automatically equip it by sending a `ClickSlotC2SPacket`.
* Automatically re-equips the previously worn chest armor when touching ground if config `reEquipArmor` is enabled.
* Optional feature: `emergencyFireworkBoost` that auto-fires a rocket when gliding speed < threshold.

### HUD Module

* Uses Fabric's `HudRenderCallback` to draw overlays.
* Visual elements:
  * **Timing Bar:** horizontal bar that fills as you approach the optimal slam window; color shifts from blue → yellow → red when ready.
  * **Aim Reticle:** circle that narrows to indicate the recommended crosshair alignment vector. Uses ray cast intersection with the predicted enemy position.
  * **Text Prompts:** "Swing in 0.4s" or "Impact Ready!" with color coding.
* Supports toggling via keybinding (`GLFW.GLFW_KEY_G`) registered with `KeyBindingHelper`.

### Config Module

* Define `MHelperConfig` with fields such as:
  * `boolean autoEquipElytra`
  * `boolean reEquipArmor`
  * `float fallDistanceThreshold`
  * `float perfectWindowDuration`
  * `HudStyle hudStyle`
* Provide a config screen accessible from Mod Menu via `ClothConfig` integration.

## Data Flow

1. Player begins a mace slam (detected by right click + mace + falling).
2. `MaceChargeTracker` records the start tick and continuously estimates the impact time.
3. `HudOverlayRenderer` reads the prediction and updates visuals each frame.
4. `AutoElytraManager` ensures the player can glide during descent by equipping an elytra if configured.
5. Upon impact or cancellation, state resets and the HUD fades out.

## Networking Considerations

* Features are client-side only; no custom packets required.
* Auto-equipping elytra uses vanilla inventory packets, safe on most servers but should include config toggles to avoid accidental policy violations.

## Testing Strategy

* **Unit Tests:** Validate `MaceChargeTracker` calculations with mocked time and velocity.
* **Manual QA:** Launch single-player world, spawn a training dummy (armor stand + modded HP display) to validate predictions.
* **Performance Checks:** Ensure HUD rendering uses matrix stack efficiently and avoids allocations.

## Future Enhancements

* Add training arena overlay to highlight landing zones.
* Integrate with ReplayMod to analyze real fights.
* Provide web dashboard that records metrics per slam attempt.

