package com.mhelper.combat;

import com.mhelper.config.MHelperConfig;
import com.mhelper.core.RaycastUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class MaceStateTracker {
    public enum TimingStatus {
        INACTIVE,
        NOT_READY,
        ALMOST,
        PERFECT
    }

    private boolean primed;
    private double secondsToImpact;
    private TimingStatus timingStatus = TimingStatus.INACTIVE;
    private boolean perfectChimePlayed;
    private final AimAssistTracker aimAssistTracker = new AimAssistTracker();
    private Text countdownText = Text.empty();

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            reset();
            return;
        }

        updatePrimedState(client, player);

        if (!primed) {
            secondsToImpact = 0;
            timingStatus = TimingStatus.INACTIVE;
            perfectChimePlayed = false;
            aimAssistTracker.reset();
            countdownText = Text.empty();
            return;
        }

        double distanceToGround = RaycastUtil.distanceToGround(player);
        double velocityY = player.getVelocity().y;
        if (velocityY >= -0.01) {
            secondsToImpact = Double.POSITIVE_INFINITY;
        } else {
            double ticksToImpact = Math.max(0, distanceToGround / Math.max(-velocityY, 0.01));
            secondsToImpact = ticksToImpact / 20.0;
        }

        MHelperConfig config = MHelperConfig.get();
        double perfectStart = config.perfectWindowStart;
        double perfectEnd = config.perfectWindowEnd;

        if (secondsToImpact >= perfectStart && secondsToImpact <= perfectEnd) {
            timingStatus = TimingStatus.PERFECT;
        } else if (secondsToImpact >= perfectStart - 0.05 && secondsToImpact <= perfectEnd + 0.05) {
            timingStatus = TimingStatus.ALMOST;
        } else {
            timingStatus = TimingStatus.NOT_READY;
        }

        if (Double.isInfinite(secondsToImpact)) {
            countdownText = Text.literal("Stabilizing...");
        } else {
            countdownText = Text.literal(String.format("Swing in %.2fs", secondsToImpact));
        }

        if (timingStatus == TimingStatus.PERFECT && config.playPerfectChime && !perfectChimePlayed) {
            float volume = (float) MathHelper.clamp(config.chimeVolume, 0.0, 1.0);
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), volume, 1.2f);
            perfectChimePlayed = true;
        } else if (timingStatus != TimingStatus.PERFECT) {
            perfectChimePlayed = false;
        }

        aimAssistTracker.update(client, player);

        if (player.isOnGround() || player.getVelocity().y >= -0.05) {
            primed = false;
        }
    }

    private void updatePrimedState(MinecraftClient client, ClientPlayerEntity player) {
        MHelperConfig config = MHelperConfig.get();
        boolean holdingMace = isHoldingMace(player.getMainHandStack()) || isHoldingMace(player.getOffHandStack());
        boolean attackPressed = client.options.attackKey.isPressed();
        boolean falling = player.getVelocity().y < -0.08;
        boolean meetsFallDistance = player.fallDistance >= config.fallDistanceThreshold;

        if (!primed) {
            primed = holdingMace && attackPressed && falling && meetsFallDistance;
        } else if (!holdingMace || player.isTouchingWater() || player.isOnGround()) {
            primed = false;
        }
    }

    private boolean isHoldingMace(ItemStack stack) {
        return stack != null && stack.isOf(Items.MACE);
    }

    public void reset() {
        primed = false;
        secondsToImpact = 0;
        timingStatus = TimingStatus.INACTIVE;
        perfectChimePlayed = false;
        aimAssistTracker.reset();
        countdownText = Text.empty();
    }

    public boolean isPrimed() {
        return primed;
    }

    public double getSecondsToImpact() {
        return secondsToImpact;
    }

    public TimingStatus getTimingStatus() {
        return timingStatus;
    }

    public AimAssistTracker getAimAssistTracker() {
        return aimAssistTracker;
    }

    public Text getCountdownText() {
        return countdownText;
    }

    public static class AimAssistTracker {
        private Optional<LivingEntity> target = Optional.empty();
        private double angularDifference;
        private double yawDifference;
        private double pitchDifference;

        public void update(MinecraftClient client, ClientPlayerEntity player) {
            MHelperConfig config = MHelperConfig.get();
            if (!config.showAimAssist) {
                reset();
                return;
            }

            target = findTarget(client, player);
            if (target.isEmpty()) {
                angularDifference = 180;
                yawDifference = 180;
                pitchDifference = 180;
                return;
            }

            double tickDelta = client.getRenderTickCounter().getTickDelta(true);
            Vec3d playerPos = player.getCameraPosVec((float) tickDelta);
            Vec3d look = player.getRotationVec((float) tickDelta).normalize();

            LivingEntity entity = target.get();
            Vec3d entityPos = entity.getPos().add(0, entity.getStandingEyeHeight(), 0);
            Vec3d toTarget = entityPos.subtract(playerPos).normalize();

            angularDifference = Math.toDegrees(Math.acos(MathHelper.clamp(look.dotProduct(toTarget), -1.0, 1.0)));

            double targetYaw = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90.0);
            double playerYaw = MathHelper.wrapDegrees(player.getYaw());
            yawDifference = MathHelper.wrapDegrees(targetYaw - playerYaw);

            double targetPitch = Math.toDegrees(-Math.asin(toTarget.y));
            double playerPitch = player.getPitch();
            pitchDifference = targetPitch - playerPitch;
        }

        private Optional<LivingEntity> findTarget(MinecraftClient client, ClientPlayerEntity player) {
            HitResult hitResult = client.crosshairTarget;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((net.minecraft.util.hit.EntityHitResult) hitResult).getEntity();
                if (entity instanceof LivingEntity living) {
                    return Optional.of(living);
                }
            }

            if (client.world == null) {
                return Optional.empty();
            }

            double searchRadius = 6.0;
            Vec3d playerPos = player.getPos();
            Vec3d min = playerPos.subtract(searchRadius, searchRadius, searchRadius);
            Vec3d max = playerPos.add(searchRadius, searchRadius, searchRadius);
            net.minecraft.util.math.Box searchBox = new net.minecraft.util.math.Box(min, max);
            return client.world.getEntitiesByClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive() && !entity.isRemoved())
                    .stream()
                    .filter(entity -> entity.getY() < player.getY())
                    .filter(entity -> {
                        Vec3d toTarget = entity.getPos().add(0, entity.getStandingEyeHeight() * 0.6, 0).subtract(playerPos).normalize();
                        return toTarget.dotProduct(new Vec3d(0, -1, 0)) > 0.5;
                    })
                    .min((a, b) -> Double.compare(player.squaredDistanceTo(a), player.squaredDistanceTo(b)));
        }

        public void reset() {
            target = Optional.empty();
            angularDifference = 180;
            yawDifference = 180;
            pitchDifference = 180;
        }

        public Optional<LivingEntity> getTarget() {
            return target;
        }

        public double getAngularDifference() {
            return angularDifference;
        }

        public double getYawDifference() {
            return yawDifference;
        }

        public double getPitchDifference() {
            return pitchDifference;
        }
    }
}
