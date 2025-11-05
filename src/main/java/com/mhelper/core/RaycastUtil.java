package com.mhelper.core;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class RaycastUtil {
    private RaycastUtil() {}

    public static double distanceToGround(ClientPlayerEntity player) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d end = start.add(0, -64, 0);
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player);
        BlockHitResult hitResult = player.getWorld().raycast(context);
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return 64;
        }
        return Math.max(0, start.y - hitResult.getPos().y);
    }
}
