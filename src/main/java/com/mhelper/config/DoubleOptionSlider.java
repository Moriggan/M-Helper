package com.mhelper.config;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.DoubleConsumer;

class DoubleOptionSlider extends SliderWidget {
    private final double min;
    private final double max;
    private final Text label;
    private final DoubleConsumer onValueChanged;

    DoubleOptionSlider(int x, int y, int width, int height, Text label, double value, double min, double max, DoubleConsumer onValueChanged) {
        super(x, y, width, height, Text.empty(), toProgress(value, min, max));
        this.min = min;
        this.max = max;
        this.label = label;
        this.onValueChanged = onValueChanged;
        updateMessage();
    }

    private static double toProgress(double value, double min, double max) {
        if (max <= min) {
            return 0.0;
        }
        return MathHelper.clamp((value - min) / (max - min), 0.0, 1.0);
    }

    private double getScaledValue() {
        return min + (max - min) * this.value;
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(getScaledValue());
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(label.copy().append(Text.literal(String.format(": %.2f", getScaledValue()))));
    }
}
