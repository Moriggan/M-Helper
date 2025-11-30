package com.mhelper.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class MHelperConfigScreen extends Screen {
    private final Screen parent;
    private final MHelperConfig config;

    public MHelperConfigScreen(Screen parent) {
        super(Text.translatable("screen.mhelper.config"));
        this.parent = parent;
        this.config = MHelperConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 6;
        int buttonWidth = 220;
        int buttonHeight = 20;
        int spacing = 24;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("options.general.off"), Text.translatable("options.general.on"))
                .initially(config.autoEquipElytra)
                .build(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.auto_equip"), (button, value) -> config.autoEquipElytra = value));
        y += spacing;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("options.general.off"), Text.translatable("options.general.on"))
                .initially(config.requireSneakForAuto)
                .build(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.require_sneak"), (button, value) -> config.requireSneakForAuto = value));
        y += spacing;

        addDrawableChild(new DoubleOptionSlider(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.auto_glide"), config.autoGlideThreshold, 0.0, 6.0, value -> config.autoGlideThreshold = value));
        y += spacing;

        addDrawableChild(new DoubleOptionSlider(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.hud_scale"), config.hudScale, 0.5, 1.5, value -> config.hudScale = value));
        y += spacing;

        addDrawableChild(new DoubleOptionSlider(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.opacity"), config.timingBarOpacity, 0.2, 1.0, value -> config.timingBarOpacity = value));
        y += spacing;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("options.general.off"), Text.translatable("options.general.on"))
                .initially(config.showAimAssist)
                .build(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.show_aim"), (button, value) -> config.showAimAssist = value));
        y += spacing;

        addDrawableChild(new DoubleOptionSlider(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.chime_volume"), config.chimeVolume, 0.0, 1.0, value -> config.chimeVolume = value));
        y += spacing;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("options.general.off"), Text.translatable("options.general.on"))
                .initially(config.playPerfectChime)
                .build(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.play_chime"), (button, value) -> config.playPerfectChime = value));
        y += spacing;

        addDrawableChild(CyclingButtonWidget.<MHelperConfig.HudPalette>builder(value -> Text.literal(formatPalette(value)))
                .values(MHelperConfig.HudPalette.values())
                .initially(config.hudPalette)
                .build(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.palette"), (button, value) -> config.hudPalette = value));
        y += spacing;

        addDrawableChild(new DoubleOptionSlider(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.fall_threshold"), config.fallDistanceThreshold, 0.5, 5.0, value -> config.fallDistanceThreshold = value));
        y += spacing;

        addDrawableChild(new DoubleOptionSlider(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.perfect_start"), config.perfectWindowStart, 0.05, 0.6, value -> config.perfectWindowStart = value));
        y += spacing;

        addDrawableChild(new DoubleOptionSlider(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight, Text.translatable("config.mhelper.perfect_end"), config.perfectWindowEnd, 0.2, 0.8, value -> config.perfectWindowEnd = value));
        y += spacing;

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(centerX - buttonWidth / 2, this.height - 40, buttonWidth, buttonHeight)
                .build());
    }

    private String formatPalette(MHelperConfig.HudPalette palette) {
        return palette.name().toLowerCase().replace('_', ' ');
    }

    @Override
    public void close() {
        config.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        super.removed();
        config.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        int disclaimerY = this.height - 70;
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("config.mhelper.disclaimer.line1"), this.width / 2, disclaimerY, 0xF2C94C);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("config.mhelper.disclaimer.line2"), this.width / 2, disclaimerY + 12, 0xF2994A);
    }

}
