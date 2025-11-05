package com.mhelper.movement;

import com.mhelper.config.MHelperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoWaterMlg {
    private boolean attemptedThisFall;
    private boolean waitingForPickup;
    private int previousSlot = -1;
    private long lastInteractMs;

    public void reset() {
        attemptedThisFall = false;
        waitingForPickup = false;
        previousSlot = -1;
    }

    public void tick(MinecraftClient client) {
        MHelperConfig config = MHelperConfig.get();
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        if (client.isPaused() || client.currentScreen != null) {
            return;
        }

        if (!config.autoWaterMlgEnabled) {
            restorePreviousSlot(client);
            attemptedThisFall = false;
            waitingForPickup = false;
            return;
        }

        if (config.requireSneakForAuto && !client.player.isSneaking()) {
            return;
        }
        if (!config.requireSneakForAuto && client.player.isSneaking()) {
            return;
        }

        if (client.player.getAbilities().flying || client.player.isFallFlying() || client.player.isSpectator()) {
            reset();
            return;
        }

        if (client.player.isOnGround() || client.player.isTouchingWater() || client.player.isClimbing()) {
            if (waitingForPickup && config.autoWaterMlgRefill) {
                attemptPickup(client);
            }
            attemptedThisFall = false;
            waitingForPickup = false;
            restorePreviousSlot(client);
            return;
        }

        double fallDistance = client.player.fallDistance;
        if (fallDistance < config.autoWaterMlgFallDistance) {
            return;
        }

        if (attemptedThisFall) {
            return;
        }

        int bucketSlot = findWaterBucket(client);
        if (bucketSlot == -1) {
            return;
        }

        selectSlot(client, bucketSlot);

        HitResult result = client.player.raycast(4.5, 0.0F, false);
        if (!(result instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now - lastInteractMs < 75L) {
            return;
        }

        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        attemptedThisFall = true;
        waitingForPickup = true;
        lastInteractMs = now;
    }

    private void attemptPickup(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        HitResult result = client.player.raycast(5.0, 0.0F, true);
        if (!(result instanceof BlockHitResult blockHitResult)) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now - lastInteractMs < 100L) {
            return;
        }

        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        lastInteractMs = now;
        waitingForPickup = false;
        restorePreviousSlot(client);
    }

    private int findWaterBucket(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(Items.WATER_BUCKET)) {
                return i;
            }
        }
        return -1;
    }

    private void selectSlot(MinecraftClient client, int slot) {
        if (client.player.getInventory().selectedSlot == slot) {
            return;
        }
        if (previousSlot == -1) {
            previousSlot = client.player.getInventory().selectedSlot;
        }
        client.player.getInventory().selectedSlot = slot;
    }

    private void restorePreviousSlot(MinecraftClient client) {
        if (previousSlot == -1) {
            return;
        }
        client.player.getInventory().selectedSlot = previousSlot;
        previousSlot = -1;
    }
}
