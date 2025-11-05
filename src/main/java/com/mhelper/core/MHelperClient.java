package com.mhelper.core;

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
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MHelperClient implements ClientModInitializer {
    private final MaceStateTracker maceStateTracker = new MaceStateTracker();
    private final ElytraAutomation elytraAutomation = new ElytraAutomation(maceStateTracker);
    private final HelperHudRenderer hudRenderer = new HelperHudRenderer(maceStateTracker);
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
            return;
        }

        if (panicLatched) {
            return;
        }

        maceStateTracker.tick(client);
        elytraAutomation.tick(client);
    }
}
