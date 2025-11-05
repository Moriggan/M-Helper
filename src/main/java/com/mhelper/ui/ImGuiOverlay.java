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
        long handle = client.getWindow().getHandle();
        glfw.init(handle, true);
        gl3.init("#version 150");
        initialized = true;
    }

    public void toggle() {
        visible = !visible;
    }

    public void render(float tickDelta) {
        if (!visible || !initialized) {
            return;
        }

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

        ImGui.end();
    }

    private String status(boolean enabled, String label) {
        return (enabled ? "ON  " : "OFF ") + label;
    }
}
