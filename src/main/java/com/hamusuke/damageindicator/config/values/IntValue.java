package com.hamusuke.damageindicator.config.values;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;

public class IntValue extends AbstractConfig<Integer> {
    protected final int min;
    protected final int max;

    public IntValue(String name, int defaultValue, int min, int max) {
        super(name);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.clamp();
    }

    @Override
    public void write(JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(this.name).value(this.clamp());
    }

    @Override
    public void read(JsonObject jsonObject) {
        if (jsonObject.has(this.name)) {
            this.set(jsonObject.get(this.name).getAsInt());
        }
    }

    protected int clamp() {
        return this.value = MathHelper.clamp(this.value, this.min, this.max);
    }

    @Override
    public Integer get() {
        return this.clamp();
    }

    @Override
    public void set(Integer value) {
        this.value = value;
        this.clamp();
    }

    public int getMax() {
        return this.max;
    }

    public int getMin() {
        return this.min;
    }
}
