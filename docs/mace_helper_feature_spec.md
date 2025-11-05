# Mace Hit Helper Feature Specification

This spec describes the concrete behaviors, UI, and configuration for the Mace Hit Helper module.

## 1. Slam Timing Assistance

### 1.1 Detection
* Trigger when the player holds a mace in the main hand and begins a charge by pressing the attack button while falling.
* Determine the slam phase using player vertical velocity (`player.getVelocity().y`) and fall distance (`player.fallDistance`).
* Treat the slam as "primed" when fall distance ≥ `fallDistanceThreshold` (default 1.5 blocks).

### 1.2 Prediction Model
* Sample the player's downward velocity each tick while the slam is primed.
* Estimate remaining ticks to ground using:
  ```java
  double ticksToImpact = Math.max(0, -player.getY() - RaycastUtil.distanceToGround(player));
  ```
* Convert ticks to seconds for UI display (`seconds = ticks / 20.0`).
* Define the **perfect window** as impact times between 0.15s and 0.45s (configurable).
* Provide a gradient status:
  * `NOT_READY`: outside window.
  * `ALMOST`: within 0.05s of window.
  * `PERFECT`: inside perfect window.

### 1.3 Feedback
* Render a segmented bar at the center of the screen representing 1.0s before impact.
* Add color-coded states:
  * Blue: gathering speed.
  * Gold: within almost window.
  * Crimson: perfect swing moment.
* Display a countdown text `Swing in X.XXs` rounding to two decimals.
* Optionally play a short chime (client-side sound) when entering `PERFECT`.

## 2. Aim Assistance

### 2.1 Target Selection
* Prioritize the entity currently in crosshair using Fabric's `MinecraftClient.getInstance().crosshairTarget`.
* If no entity targeted, fallback to the closest living entity within 6 blocks below the player by ray casting a downward cone.

### 2.2 Aim Indicator
* Draw a translucent ring around the crosshair with radius proportional to angular error between player's look vector and the predicted target vector.
* When the player is within ±3° yaw/pitch of the predicted vector, shrink the ring and show a tick mark to indicate alignment.

## 3. Elytra Automation

### 3.1 Auto-Equip Logic
* Condition: player is falling (`velocity.y < -0.3`), slam primed, and elytra not equipped.
* Search inventory for an elytra (priority: hotbar, offhand, main inventory).
* Equip by sending `ClickSlotC2SPacket` to move the elytra into chest slot.
* Store previous chest item for later restoration.

### 3.2 Auto Glide Toggle
* Optionally deploy the elytra automatically when fall distance surpasses `autoGlideThreshold` (default 2.5 blocks).
* Respect player input: skip automation if the player is holding sneak (shift) to indicate manual control.

### 3.3 Re-Equip Armor
* After impact, if armor was stored, send packets to return it to chest slot once the player is on ground (`player.isOnGround()`).

## 4. Auto Clicker & Critical Hit Helper

### 4.1 Auto Clicker
* Optional module (default off) that simulates left-clicks for 1.12-style PvP.
* Only engages when the crosshair is over an attackable living entity and the player isn't using an item or mounted.
* Click cadence respects the attack cooldown meter: require `player.getAttackCooldownProgress(0.5f)` ≥ configurable threshold (default 0.92).
* CPS slider (`4.0`–`20.0`) determines nano-second spacing between `client.doAttack()` calls.
* If "hold to fire" is enabled (default on) the automation runs only while the native attack key is pressed; otherwise it free-fires on cooldown.

### 4.2 Critical Hit Helper
* Optional hop trigger (default on) that primes a short jump when the attack cooldown is nearly full and a target is within 3 blocks.
* Skips when the player is gliding, riding, in water, or otherwise airborne.
* Cooldown gate slider (0.6–1.0) sets the minimum cooldown progress before the helper jumps.
* Jump delay slider (0–180 ms) enforces a minimum interval between automatic hops to avoid bunny-hop spam.

## 5. Auto Water MLG

* Detects falls once `player.fallDistance` exceeds a configurable height (default 15 blocks).
* Searches the hotbar for a water bucket, swaps it into the selected slot, and fires `interactItem` toward the block below.
* Flags that the bucket should be scooped back up; upon landing it reuses the same slot to collect the water and optionally restores the prior item.
* Resets immediately if the player touches water, climbs, rides, or manually cancels by landing before the trigger height.

## 6. ImGui Control Center

* Keybind (`O`) toggles an ImGui overlay rendered via imgui-java and the client's GLFW context.
* Main window contains feature toggles, sliders, and numeric controls (timing window, automation guardrails, Elytra logic) that persist to `mhelper.json` upon change.
* Secondary quick status window highlights recommended load-outs and shows which modules are active.
* Overlay unlocks the in-game cursor while visible and respects keyboard navigation/mouse capture via ImGui's IO flags.

## 7. Configuration Options

| Key | Type | Default | Description |
| --- | ---- | ------- | ----------- |
| `autoEquipElytra` | boolean | `true` | Enable automatic chest slot swapping during a slam |
| `autoGlideThreshold` | double | `2.5` | Fall distance before attempting to open elytra |
| `perfectWindowStart` | double | `0.15` | Lower bound (seconds) of the perfect swing window |
| `perfectWindowEnd` | double | `0.45` | Upper bound (seconds) |
| `playPerfectChime` | boolean | `true` | Play a sound when entering perfect window |
| `showAimAssist` | boolean | `true` | Toggle aim indicator overlay |
| `timingBarOpacity` | double | `0.8` | Alpha multiplier for the timing HUD |
| `hudScale` | double | `1.0` | Scale factor for all HUD elements |
| `requireSneakForAuto` | boolean | `false` | If true, automation triggers only while sneaking |
| `autoClickerEnabled` | boolean | `false` | Enable the 1.12 auto clicker |
| `autoClickerHoldToFire` | boolean | `true` | Require attack key to be held for auto clicker |
| `autoClickerCps` | double | `12.0` | CPS cap for auto clicker |
| `autoClickerCooldownThreshold` | double | `0.92` | Minimum cooldown progress before a click |
| `criticalHelperEnabled` | boolean | `true` | Enable automatic hop priming |
| `criticalHelperCooldownGate` | double | `0.9` | Cooldown gate for hop trigger |
| `criticalHelperJumpDelayMs` | long | `65` | Delay between automatic hops in milliseconds |
| `autoWaterMlgEnabled` | boolean | `true` | Enable water-bucket landing automation |
| `autoWaterMlgFallDistance` | double | `15.0` | Fall distance threshold before attempting MLG |
| `autoWaterMlgRefill` | boolean | `true` | Attempt to pick water back up after landing |

## 8. Accessibility

* Provide colorblind-friendly palette (configurable) using tri-tone with distinct luminosity.
* Allow resizing HUD elements via slider (0.5x - 1.5x scale).
* Support toggling audio cues and customizing volume multiplier.

## 9. Safety Considerations

* Include disclaimers in config screen that automation may be disallowed on some servers.
* Provide a panic hotkey to disable all automation in real-time.
* Avoid sending repeated inventory packets; throttle to once every 5 ticks.

