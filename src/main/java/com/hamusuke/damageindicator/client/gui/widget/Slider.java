package com.hamusuke.damageindicator.client.gui.widget;

import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class Slider extends SliderWidget {
    protected final Text prefix;
    protected final double maxValue;
    protected final double minValue;
    protected final Consumer<Slider> applier;

    public Slider(int x, int y, int width, int height, Text prefix, double minValue, double maxValue, double currentValue, Consumer<Slider> applier) {
        super(x, y, width, height, LiteralText.EMPTY, (currentValue - minValue) / (maxValue - minValue));
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.applier = applier;
        this.prefix = prefix;
        this.setMessage(ScreenTexts.composeGenericOptionText(this.prefix, Text.of("" + this.getIntValue())));
    }

    public int getIntValue() {
        return (int) Math.round(this.getValue());
    }

    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(new LiteralText("").append(this.prefix).append("" + this.getIntValue()));
    }

    @Override
    protected void applyValue() {
        this.applier.accept(this);
    }
}
