package com.mhelper.hud;

import com.mhelper.combat.MaceStateTracker;
import com.mhelper.combat.MaceStateTracker.AimAssistTracker;
import com.mhelper.combat.MaceStateTracker.TimingStatus;
import com.mhelper.config.MHelperConfig;
import com.mhelper.config.MHelperConfig.HudPalette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class HelperHudRenderer {
    private final MaceStateTracker maceStateTracker;

    public HelperHudRenderer(MaceStateTracker maceStateTracker) {
        this.maceStateTracker = maceStateTracker;
    }

    public void render(DrawContext context, float tickDelta, boolean enabled) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        renderSupportStatus(context, width, height, enabled);

        if (!enabled || !maceStateTracker.isPrimed()) {
            return;
        }

        renderTimingBar(context, width, height);
        renderAimAssist(context, width, height);
    }

    private void renderTimingBar(DrawContext context, int width, int height) {
        MHelperConfig config = MHelperConfig.get();
        double secondsToImpact = maceStateTracker.getSecondsToImpact();
        TimingStatus status = maceStateTracker.getTimingStatus();
        double opacity = config.timingBarOpacity;
        double scale = MathHelper.clamp(config.hudScale, 0.5, 1.5);

        int barWidth = (int) (160 * scale);
        int barHeight = Math.max(6, (int) (10 * scale));
        int x = width / 2 - barWidth / 2;
        int y = height / 2 + (int) (30 * scale);

        int backgroundColor = withOpacity(0x202020, opacity * 0.6);
        context.fill(x, y, x + barWidth, y + barHeight, backgroundColor);

        double clamped = MathHelper.clamp(1.0 - Math.min(secondsToImpact, 1.0), 0.0, 1.0);
        int filledWidth = (int) (barWidth * clamped);

        HudPalette palette = config.hudPalette;
        int fillColor = switch (status) {
            case PERFECT -> withOpacity(palette.perfect, opacity);
            case ALMOST -> withOpacity(palette.almost, opacity);
            case NOT_READY -> withOpacity(palette.notReady, opacity);
            default -> withOpacity(palette.notReady, opacity * 0.5);
        };

        context.fill(x, y, x + filledWidth, y + barHeight, fillColor);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawCenteredTextWithShadow(textRenderer, maceStateTracker.getCountdownText(), width / 2, y - (int) (12 * scale), 0xFFFFFF);
    }

    private void renderAimAssist(DrawContext context, int width, int height) {
        if (!MHelperConfig.get().showAimAssist) {
            return;
        }

        AimAssistTracker tracker = maceStateTracker.getAimAssistTracker();
        if (tracker.getTarget().isEmpty()) {
            return;
        }

        double angularError = tracker.getAngularDifference();
        double scale = MathHelper.clamp(MHelperConfig.get().hudScale, 0.5, 1.5);
        int centerX = width / 2;
        int centerY = height / 2;

        int crossLength = (int) MathHelper.clamp(angularError * 2.2 * scale, 8.0 * scale, 38.0 * scale);
        int color = withOpacity(0x66CCFF, 0.75);

        context.fill(centerX - 1, centerY - crossLength, centerX + 1, centerY + crossLength, color);
        context.fill(centerX - crossLength, centerY - 1, centerX + crossLength, centerY + 1, color);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        String status = String.format("%.1f° / %.1f°", Math.abs(tracker.getYawDifference()), Math.abs(tracker.getPitchDifference()));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, centerY + crossLength + 6, 0xA0CFF5);

        if (Math.abs(tracker.getYawDifference()) <= 3 && Math.abs(tracker.getPitchDifference()) <= 3) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Aligned!"), centerX, centerY - crossLength - 10, 0x80FF8F);
        }
    }

    private int withOpacity(int rgb, double opacity) {
        int a = (int) MathHelper.clamp(opacity * 255.0, 0, 255);
        return (a << 24) | rgb;
    }

    private void renderSupportStatus(DrawContext context, int width, int height, boolean enabled) {
        MHelperConfig config = MHelperConfig.get();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int x = 8;
        int y = height - 50;
        int color = withOpacity(0xFFFFFF, 0.8);
        if (!enabled) {
            context.drawTextWithShadow(textRenderer, Text.literal("Helper paused (press G to resume)"), x, y, color);
            return;
        }

        context.drawTextWithShadow(textRenderer, Text.literal("Auto Clicker: " + (config.autoClickerEnabled ? "ON" : "OFF")), x, y, color);
        y += 10;
        context.drawTextWithShadow(textRenderer, Text.literal("Critical Helper: " + (config.criticalHelperEnabled ? "ON" : "OFF")), x, y, color);
        y += 10;
        context.drawTextWithShadow(textRenderer, Text.literal("Water MLG: " + (config.autoWaterMlgEnabled ? "ON" : "OFF")), x, y, color);
    }
}
