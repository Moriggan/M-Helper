# M-Helper Architecture Overview

This document outlines a suggested architecture for implementing the Minecraft 1.21.5 "Mace Hit Helper" mod. The goal is to provide in-game assistance for timing mace slam attacks, aligning crosshair aim, and automatically equipping an elytra when appropriate.

## Technology Stack

* **Minecraft Version:** 1.21.5 (Java Edition)
* **Mod Loader:** Fabric (preferred for rapid iteration and client-side features)
* **Language:** Java
* **Rendering:** Fabric Rendering API for HUD overlays plus `imgui-java` (GLFW/GL3 bindings) for the control center
* **Configuration:** Gson-backed JSON persisted to Fabric's config directory and edited via the ImGui overlay

## High-Level Modules

| Module | Responsibility |
| ------ | -------------- |
| `core` | Initializes the mod, registers keybinds/events, fans out ticks to subsystems |
| `combat` | Tracks mace charge state, predicts slam timing, runs auto clicker & critical helper |
| `movement` | Handles elytra automation and auto water MLG bucket swaps |
| `hud` | Renders timing bars, aim rings, and active module status strip |
| `ui` | Hosts the ImGui control center windows |
| `config` | Declares user-configurable options and persists them via Gson |

### Core Module

* Entry point via `MHelperMod` implementing `ModInitializer`.
* Registers client tick listeners (Fabric API) to update timers each frame.
* Uses `ClientPlayConnectionEvents.JOIN` / `DISCONNECT` to reset state per world.

### Combat Module

* Maintains a `MaceStateTracker` to determine slam priming, countdown timing, and aim suggestions.
* Provides an `AutoClicker` that fires `client.doAttack()` when cooldown and CPS gates are satisfied.
* Supplies a `CriticalHitHelper` that performs a controlled hop to secure crits once cooldown thresholds are met.
* Tracks panic state resets so automation stops immediately when the panic hotkey is pressed.

### Movement Module

* Elytra automation mirrors the original plan: equips mid-fall, starts gliding at threshold, restores armor after landing.
* `AutoWaterMlg` watches `player.fallDistance`, equips a water bucket from the hotbar, places water toward the predicted landing block, and re-collects it upon landing.
* Honors sneaking requirements and panic latch to avoid unwanted automation on restrictive servers.

### HUD Module

* Uses Fabric's `HudRenderCallback` to draw overlays.
* Visual elements:
  * **Timing Bar:** horizontal bar that fills as you approach the optimal slam window; color shifts from palette-defined colors when ready.
  * **Aim Reticle:** ring sized to angular error with alignment tick marks when the player matches the recommended vector.
  * **Status Strip:** bottom-left text summarizing whether auto clicker, critical helper, and water MLG are active or if the helper is paused.
* Respects config toggles for aim assist visibility and HUD scaling.

### UI Module

* `ImGuiOverlay` bootstraps imgui-java against Minecraft's GLFW window and GL3 context.
* Provides a control center window with toggles/sliders plus a quick status panel with recommended setups and sneak guardrail messaging.
* Automatically unlocks/re-locks the Minecraft cursor while open and pauses auto-combat helpers so you can tune settings safely.
* Saves config changes instantly when a checkbox or slider changes to keep the JSON on disk aligned with the overlay.

### Config Module

* `MHelperConfig` holds gson-serializable fields for mace timing, Elytra automation, combat helpers, HUD appearance, and water MLG settings.
* Reads/writes `config/mhelper.json` on demand; the ImGui overlay and keybind toggles mutate the singleton and call `save()`.
* Existing vanilla-style screen remains available as a fallback but the ImGui overlay is the primary UX.

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

