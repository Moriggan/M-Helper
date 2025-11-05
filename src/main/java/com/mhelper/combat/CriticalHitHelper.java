package com.mhelper.combat;

import com.mhelper.config.MHelperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class CriticalHitHelper {
    private long lastJumpMs;

    public void reset() {
        lastJumpMs = 0L;
    }

    public void tick(MinecraftClient client) {
        MHelperConfig config = MHelperConfig.get();
        if (!config.criticalHelperEnabled) {
            return;
        }

        if (client.player == null || client.interactionManager == null) {
            return;
        }

        if (client.isPaused() || client.currentScreen != null) {
            return;
        }

        if (config.requireSneakForAuto && !client.player.isSneaking()) {
            return;
        }
        if (!config.requireSneakForAuto && client.player.isSneaking()) {
            return;
        }

        if (client.player.isFallFlying() || client.player.isTouchingWater() || client.player.isClimbing() || client.player.hasVehicle()) {
            return;
        }

        if (!client.player.isOnGround()) {
            return;
        }

        if (!client.options.attackKey.isPressed() && !(config.autoClickerEnabled && !config.autoClickerHoldToFire)) {
            return;
        }

        HitResult crosshair = client.crosshairTarget;
        if (!(crosshair instanceof EntityHitResult entityHitResult)) {
            return;
        }

        if (!(entityHitResult.getEntity() instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }

        float cooldown = client.player.getAttackCooldownProgress(0.5f);
        if (cooldown < (float) config.criticalHelperCooldownGate) {
            return;
        }

        double distance = client.player.squaredDistanceTo(living);
        if (distance > 9.0) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now - lastJumpMs < Math.max(0L, config.criticalHelperJumpDelayMs)) {
            return;
        }

        client.player.jump();
        lastJumpMs = now;
    }
}
