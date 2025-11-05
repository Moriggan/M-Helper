package com.mhelper.core;

import com.mhelper.combat.AutoClicker;
import com.mhelper.combat.CriticalHitHelper;
import com.mhelper.combat.MaceStateTracker;
import com.mhelper.config.MHelperConfig;
import com.mhelper.hud.HelperHudRenderer;
import com.mhelper.movement.ElytraAutomation;
import com.mhelper.movement.AutoWaterMlg;
import com.mhelper.ui.ImGuiOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import com.mhelper.combat.MaceStateTracker;
import com.mhelper.config.MHelperConfig;
import com.mhelper.hud.HelperHudRenderer;
import com.mhelper.config.MHelperConfigScreen;
import com.mhelper.movement.ElytraAutomation;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MHelperClient implements ClientModInitializer {
    private final MaceStateTracker maceStateTracker = new MaceStateTracker();
    private final ElytraAutomation elytraAutomation = new ElytraAutomation(maceStateTracker);
    private final HelperHudRenderer hudRenderer = new HelperHudRenderer(maceStateTracker);
    private final AutoClicker autoClicker = new AutoClicker();
    private final CriticalHitHelper criticalHitHelper = new CriticalHitHelper();
    private final AutoWaterMlg autoWaterMlg = new AutoWaterMlg();
    private final ImGuiOverlay imguiOverlay = new ImGuiOverlay();
    private KeyBinding toggleKey;
    private KeyBinding panicKey;
    private KeyBinding configKey;
    private KeyBinding autoClickerToggle;
    private KeyBinding criticalToggle;
    private KeyBinding autoMlgToggle;
    private KeyBinding toggleKey;
    private KeyBinding panicKey;
    private KeyBinding configKey;

    private boolean enabled = true;
    private boolean panicLatched;

    @Override
    public void onInitializeClient() {
        MHelperConfig.get(); // ensure config loaded
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mhelper.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "mod.mhelper.name"
        ));
        panicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mhelper.panic",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "mod.mhelper.name"
        ));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mhelper.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "mod.mhelper.name"
        ));
        autoClickerToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mhelper.auto_clicker",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "mod.mhelper.name"
        ));
        criticalToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mhelper.critical_helper",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "mod.mhelper.name"
        ));
        autoMlgToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mhelper.auto_mlg",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "mod.mhelper.name"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(false);
            hudRenderer.render(drawContext, tickDelta, enabled);
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            hudRenderer.render(drawContext, tickDelta, enabled);
        HudRenderCallback.EVENT.register((DrawContext drawContext, RenderTickCounter tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(true);
            hudRenderer.render(drawContext, tickDelta, enabled);
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            hudRenderer.render(matrixStack, tickDelta, enabled);
            imguiOverlay.render(MinecraftClient.getInstance(), tickDelta);
        });
    }

    private void onClientTick(MinecraftClient client) {
        imguiOverlay.ensureInitialized(client);


        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> hudRenderer.render(matrixStack, tickDelta, enabled));
    }

    private void onClientTick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            if (enabled) {
                panicLatched = false;
            }
        }

        while (panicKey.wasPressed()) {
            enabled = false;
            panicLatched = true;
            maceStateTracker.reset();
            autoClicker.reset();
            criticalHitHelper.reset();
            autoWaterMlg.reset();
        }

        while (configKey.wasPressed()) {
            imguiOverlay.toggle(client);
        }

        MHelperConfig config = MHelperConfig.get();
        while (autoClickerToggle.wasPressed()) {
            config.autoClickerEnabled = !config.autoClickerEnabled;
            config.save();
        }

        while (criticalToggle.wasPressed()) {
            config.criticalHelperEnabled = !config.criticalHelperEnabled;
            config.save();
        }

        while (autoMlgToggle.wasPressed()) {
            config.autoWaterMlgEnabled = !config.autoWaterMlgEnabled;
            config.save();
        }

        while (configKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new MHelperConfigScreen(null));
            } else {
                client.setScreen(new MHelperConfigScreen(client.currentScreen));
            }
        }

        if (!enabled) {
            maceStateTracker.reset();
            autoClicker.reset();
            criticalHitHelper.reset();
            autoWaterMlg.reset();
            return;
        }

        if (panicLatched) {
            return;
        }

        maceStateTracker.tick(client);
        elytraAutomation.tick(client);

        if (imguiOverlay.isVisible()) {
            autoClicker.reset();
            criticalHitHelper.reset();
            autoWaterMlg.reset();
            return;
        }

        autoClicker.tick(client);
        criticalHitHelper.tick(client);
        autoWaterMlg.tick(client);
    }
}
