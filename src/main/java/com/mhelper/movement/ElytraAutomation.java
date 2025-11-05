package com.mhelper.movement;

import com.mhelper.combat.MaceStateTracker;
import com.mhelper.config.MHelperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class ElytraAutomation {
    private final MaceStateTracker maceStateTracker;
    private boolean elytraEquippedByMod;
    private int storedArmorSlot = -1;
    private int packetCooldown;

    public ElytraAutomation(MaceStateTracker maceStateTracker) {
        this.maceStateTracker = maceStateTracker;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }

        if (packetCooldown > 0) {
            packetCooldown--;
        }

        MHelperConfig config = MHelperConfig.get();
        if (!config.autoEquipElytra) {
            return;
        }

        if (!maceStateTracker.isPrimed()) {
            if (elytraEquippedByMod && storedArmorSlot >= 0 && player.isOnGround() && hasStack(player, storedArmorSlot)) {
                moveStack(client, storedArmorSlot, getChestSlotIndex());
                elytraEquippedByMod = false;
                storedArmorSlot = -1;
            }
            return;
        }

        if (packetCooldown > 0) {
            return;
        }

        boolean requireSneak = config.requireSneakForAuto;
        if (requireSneak && !player.isSneaking()) {
            return;
        }
        if (!requireSneak && player.isSneaking()) {
            return;
        }

        double velocityY = player.getVelocity().y;
        boolean fallingFast = velocityY < -0.3;
        boolean elytraEquipped = player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);

        if (!fallingFast || elytraEquipped) {
            return;
        }

        int elytraSlot = findElytraSlot(player);
        if (elytraSlot == -1) {
            return;
        }

        storedArmorSlot = elytraSlot;
        moveStack(client, elytraSlot, getChestSlotIndex());
        elytraEquippedByMod = true;
        packetCooldown = 5;

        if (config.autoGlideThreshold > 0 && player.fallDistance > config.autoGlideThreshold && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    private void moveStack(MinecraftClient client, int fromSlot, int toSlot) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            return;
        }

        int syncId = player.playerScreenHandler.syncId;
        client.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, toSlot, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, player);
    }

    private boolean hasStack(ClientPlayerEntity player, int slot) {
        if (slot < 0 || slot >= player.playerScreenHandler.slots.size()) {
            return false;
        }
        return !player.playerScreenHandler.getSlot(slot).getStack().isEmpty();
    }

    private int getChestSlotIndex() {
        return 38;
    }

    private int findElytraSlot(ClientPlayerEntity player) {
        // Hotbar 0-8
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                return i;
            }
        }
        // Offhand slot 40
        if (!player.getInventory().offHand.isEmpty() && player.getInventory().offHand.get(0).isOf(Items.ELYTRA)) {
            return 45; // PlayerScreenHandler slot index for offhand
        }
        // Main inventory 9-35
        for (int i = 9; i < 36; i++) {
            if (player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
                return i;
            }
        }
        return -1;
    }
}
