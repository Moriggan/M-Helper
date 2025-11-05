package com.mhelper.ui;

import com.mhelper.config.MHelperConfig;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import net.minecraft.client.MinecraftClient;

public class ImGuiOverlay {
    private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gl3 = new ImGuiImplGl3();
    private boolean initialized;
    private boolean visible;

    public void ensureInitialized(MinecraftClient client) {
        if (initialized || client == null || client.getWindow() == null) {
            return;
        }

        ImGui.createContext();
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        ImGui.styleColorsDark();
        ImGui.getIO().setMouseDrawCursor(false);
        long handle = client.getWindow().getHandle();
        glfw.init(handle, true);
        gl3.init("#version 150");
        initialized = true;
    }

    public void toggle(MinecraftClient client) {
        visible = !visible;

        if (client != null && client.mouse != null) {
            if (visible) {
                client.mouse.unlockCursor();
            } else if (client.currentScreen == null) {
                client.mouse.lockCursor();
            }
        }

        if (!initialized) {
            return;
        }

        ImGui.getIO().setMouseDrawCursor(visible);
    }

    public boolean isVisible() {
        return visible;
    }

    public void render(MinecraftClient client, float tickDelta) {
        if (!visible || !initialized || client == null || client.getWindow() == null) {
            return;
        }

        ImGui.getIO().setMouseDrawCursor(true);

        glfw.newFrame();
        gl3.newFrame();
        ImGui.newFrame();

        drawMainWindow();
        drawStatusWindow();

        ImGui.render();
        gl3.renderDrawData(ImGui.getDrawData());
    }

    private void drawMainWindow() {
        MHelperConfig config = MHelperConfig.get();
        ImGui.setNextWindowSize(420, 360, ImGuiCond.Once);
        if (!ImGui.begin("M-Helper Control Center", ImGuiWindowFlags.NoCollapse)) {
            ImGui.end();
            return;
        }

        ImGui.text("Combat");
        ImGui.separator();

        ImBoolean autoClickerEnabled = new ImBoolean(config.autoClickerEnabled);
        if (ImGui.checkbox("Auto Clicker", autoClickerEnabled)) {
            config.autoClickerEnabled = autoClickerEnabled.get();
            config.save();
        }
        ImGui.sameLine();
        ImGui.textDisabled("(H)");

        ImBoolean holdToFire = new ImBoolean(config.autoClickerHoldToFire);
        if (ImGui.checkbox("Hold attack to trigger", holdToFire)) {
            config.autoClickerHoldToFire = holdToFire.get();
            config.save();
        }
        float[] cps = {(float) config.autoClickerCps};
        if (ImGui.sliderFloat("Clicks per second", cps, 4.0f, 20.0f, "%.1f CPS")) {
            config.autoClickerCps = cps[0];
            config.save();
        }
        float[] cooldown = {(float) config.autoClickerCooldownThreshold};
        if (ImGui.sliderFloat("Cooldown gate", cooldown, 0.6f, 1.0f, "%.2f ready")) {
            config.autoClickerCooldownThreshold = cooldown[0];
            config.save();
        }

        ImGui.separator();
        ImBoolean criticalEnabled = new ImBoolean(config.criticalHelperEnabled);
        if (ImGui.checkbox("Critical hit helper", criticalEnabled)) {
            config.criticalHelperEnabled = criticalEnabled.get();
            config.save();
        }
        ImGui.sameLine();
        ImGui.textDisabled("(J)");

        float[] critCooldown = {(float) config.criticalHelperCooldownGate};
        if (ImGui.sliderFloat("Prime at cooldown", critCooldown, 0.6f, 1.0f, "%.2f")) {
            config.criticalHelperCooldownGate = critCooldown[0];
            config.save();
        }
        float[] critDelay = {config.criticalHelperJumpDelayMs};
        if (ImGui.sliderFloat("Jump delay", critDelay, 0f, 180f, "%.0f ms")) {
            config.criticalHelperJumpDelayMs = Math.round(critDelay[0]);
            config.save();
        }

        ImGui.separator();
        ImGui.text("Mace & Elytra");
        ImGui.separator();

        ImBoolean autoEquip = new ImBoolean(config.autoEquipElytra);
        if (ImGui.checkbox("Auto-equip elytra", autoEquip)) {
            config.autoEquipElytra = autoEquip.get();
            config.save();
        }
        float[] glide = {(float) config.autoGlideThreshold};
        if (ImGui.sliderFloat("Auto-glide threshold", glide, 1.0f, 6.0f, "%.1f blocks")) {
            config.autoGlideThreshold = glide[0];
            config.save();
        }
        float[] fallPrime = {(float) config.fallDistanceThreshold};
        if (ImGui.sliderFloat("Prime slam at fall distance", fallPrime, 0.5f, 6.0f, "%.1f blocks")) {
            config.fallDistanceThreshold = fallPrime[0];
            config.save();
        }
        ImBoolean playChime = new ImBoolean(config.playPerfectChime);
        if (ImGui.checkbox("Play perfect-window chime", playChime)) {
            config.playPerfectChime = playChime.get();
            config.save();
        }
        float[] chimeVolume = {(float) config.chimeVolume};
        if (ImGui.sliderFloat("Chime volume", chimeVolume, 0.0f, 1.0f, "%.2f")) {
            config.chimeVolume = chimeVolume[0];
            config.save();
        }
        float[] perfectStart = {(float) config.perfectWindowStart};
        float[] perfectEnd = {(float) config.perfectWindowEnd};
        if (ImGui.dragFloatRange2("Perfect window (s)", perfectStart, perfectEnd, 0.0025f, 0.05f, 0.9f, "Start %.2f", "End %.2f")) {
            float min = Math.min(perfectStart[0], perfectEnd[0] - 0.01f);
            float max = Math.max(perfectEnd[0], min + 0.01f);
            config.perfectWindowStart = Math.max(0.05f, min);
            config.perfectWindowEnd = Math.min(0.9f, max);
            config.save();
        }

        ImGui.separator();
        ImGui.text("Utility");
        ImGui.separator();

        ImBoolean autoMlg = new ImBoolean(config.autoWaterMlgEnabled);
        if (ImGui.checkbox("Auto water MLG", autoMlg)) {
            config.autoWaterMlgEnabled = autoMlg.get();
            config.save();
        }
        ImGui.sameLine();
        ImGui.textDisabled("(K)");

        float[] mlgHeight = {(float) config.autoWaterMlgFallDistance};
        if (ImGui.sliderFloat("Trigger height", mlgHeight, 8.0f, 40.0f, "%.1f blocks")) {
            config.autoWaterMlgFallDistance = mlgHeight[0];
            config.save();
        }
        ImBoolean refill = new ImBoolean(config.autoWaterMlgRefill);
        if (ImGui.checkbox("Refill after landing", refill)) {
            config.autoWaterMlgRefill = refill.get();
            config.save();
        }

        ImGui.separator();
        ImGui.text("Automation guardrails");
        ImGui.separator();

        ImBoolean requireSneak = new ImBoolean(config.requireSneakForAuto);
        if (ImGui.checkbox("Require sneaking for automation", requireSneak)) {
            config.requireSneakForAuto = requireSneak.get();
            config.save();
        }
        ImGui.textWrapped(requireSneak.get()
                ? "Helpers only fire while you hold sneak."
                : "Sneaking temporarily pauses all helpers.");

        ImGui.separator();
        ImGui.text("Visuals");
        ImGui.separator();

        float[] hudScale = {(float) config.hudScale};
        if (ImGui.sliderFloat("HUD scale", hudScale, 0.5f, 1.5f, "%.2f")) {
            config.hudScale = hudScale[0];
            config.save();
        }
        float[] opacity = {(float) config.timingBarOpacity};
        if (ImGui.sliderFloat("Timing bar opacity", opacity, 0.2f, 1.0f, "%.2f")) {
            config.timingBarOpacity = opacity[0];
            config.save();
        }
        ImBoolean aimAssist = new ImBoolean(config.showAimAssist);
        if (ImGui.checkbox("Show aim assist ring", aimAssist)) {
            config.showAimAssist = aimAssist.get();
            config.save();
        }

        ImGui.end();
    }

    private void drawStatusWindow() {
        MHelperConfig config = MHelperConfig.get();
        ImGui.setNextWindowPos(30, 30, ImGuiCond.Once);
        ImGui.setNextWindowBgAlpha(0.55f);
        if (!ImGui.begin("Quick Toggles", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.end();
            return;
        }

        ImGui.textColored(0.6f, 0.8f, 1f, 1f, "Recommended loadout");
        ImGui.bulletText("Panic key (B) to instantly disable all automation");
        ImGui.bulletText("Use Elytra auto-glide with water MLG for long falls");
        ImGui.bulletText("Combine critical helper with auto clicker for 1.12 duels");

        ImGui.separator();
        ImGui.textColored(0.8f, 0.8f, 0.8f, 1f, "Active modules:");
        ImGui.bulletText(status(config.autoClickerEnabled, "Auto clicker"));
        ImGui.bulletText(status(config.criticalHelperEnabled, "Critical helper"));
        ImGui.bulletText(status(config.autoWaterMlgEnabled, "Auto water MLG"));
        ImGui.bulletText(status(config.autoEquipElytra, "Auto elytra"));
        ImGui.separator();
        ImGui.textColored(0.7f, 0.9f, 0.7f, 1f,
                config.requireSneakForAuto ? "Sneak to enable helpers" : "Sneak to pause helpers");

        ImGui.end();
    }

    private String status(boolean enabled, String label) {
        return (enabled ? "ON  " : "OFF ") + label;
    }
}
