package com.mhelper.combat;

import com.mhelper.config.MHelperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoClicker {
    private long lastClickTimeNs;

    public void reset() {
        lastClickTimeNs = 0L;
    }

    public void tick(MinecraftClient client) {
        MHelperConfig config = MHelperConfig.get();
        if (!config.autoClickerEnabled) {
            return;
        }

        if (client.player == null || client.interactionManager == null) {
            return;
        }

        if (config.requireSneakForAuto && !client.player.isSneaking()) {
            return;
        }
        if (!config.requireSneakForAuto && client.player.isSneaking()) {
            return;
        }

        if (config.autoClickerHoldToFire && !client.options.attackKey.isPressed()) {
            return;
        }

        if (client.player.isSpectator() || client.player.isRiding() || client.player.isUsingItem()) {
            return;
        }

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof EntityHitResult entityHitResult)) {
            return;
        }

        Entity target = entityHitResult.getEntity();
        if (target == null || !target.isAttackable() || !target.isAlive()) {
            return;
        }

        float cooldownProgress = client.player.getAttackCooldownProgress(0.5f);
        if (cooldownProgress < (float) config.autoClickerCooldownThreshold) {
            return;
        }

        double cps = Math.max(1.0, config.autoClickerCps);
        long now = Util.getMeasuringTimeNano();
        long intervalNs = (long) (1_000_000_000L / cps);
        if (now - lastClickTimeNs < intervalNs) {
            return;
        }

        client.doAttack();
        lastClickTimeNs = now;
    }
}
