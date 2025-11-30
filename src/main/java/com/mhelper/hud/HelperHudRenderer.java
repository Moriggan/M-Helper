package com.mhelper.hud;

import com.mhelper.combat.MaceStateTracker;
import com.mhelper.combat.MaceStateTracker.AimAssistTracker;
import com.mhelper.combat.MaceStateTracker.TimingStatus;
import com.mhelper.config.MHelperConfig;
import com.mhelper.config.MHelperConfig.HudPalette;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferRenderer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

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

        float radius = (float) MathHelper.clamp(angularError * 1.5f * scale, 6.0 * scale, 32.0 * scale);
        int color = withOpacity(0xFFFFFF, 0.7);
        drawRing(context, centerX, centerY, radius, (float) Math.max(1.5f, 2.0f * scale), color);

        boolean aligned = Math.abs(tracker.getYawDifference()) <= 3 && Math.abs(tracker.getPitchDifference()) <= 3;
        if (aligned) {
            context.fill(centerX - 1, centerY - 8, centerX + 1, centerY + 8, withOpacity(0x00FF7F, 0.8));
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        String status = String.format("%.1f° / %.1f°", Math.abs(tracker.getYawDifference()), Math.abs(tracker.getPitchDifference()));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, centerY + (int) (radius + 8), 0xA0CFF5);
    }

    private int withOpacity(int rgb, double opacity) {
        int a = (int) MathHelper.clamp(opacity * 255.0, 0, 255);
        return (a << 24) | rgb;
    }

    private void drawRing(DrawContext context, int centerX, int centerY, float radius, float thickness, int color) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        int segments = 48;
        float innerRadius = Math.max(0, radius - thickness);
        float outerRadius = radius;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            buffer.vertex(matrix, centerX + cos * outerRadius, centerY + sin * outerRadius, 0).color(r, g, b, a);
            buffer.vertex(matrix, centerX + cos * innerRadius, centerY + sin * innerRadius, 0).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
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
