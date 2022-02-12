package com.hamusuke.damageindicator.config.values;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;

public class RGBValue extends AbstractConfig<RGBValue.RGB> {
    public RGBValue(String name, int red, int green, int blue) {
        super(name);
        this.value = new RGB(red, green, blue);
    }

    @Override
    public void write(JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(this.name).beginObject();
        this.value.write(jsonWriter);
        jsonWriter.endObject();
    }

    @Override
    public void read(JsonObject jsonObject) {
        if (jsonObject.has(this.name) && jsonObject.get(this.name).isJsonObject()) {
            JsonObject rgb = jsonObject.getAsJsonObject(this.name);
            this.value.read(rgb);
        }
    }

    public static class RGB {
        private final IntValue red;
        private final IntValue green;
        private final IntValue blue;

        public RGB(int red, int green, int blue) {
            this.red = new IntValue("red", red, 0, 255);
            this.green = new IntValue("green", green, 0, 255);
            this.blue = new IntValue("blue", blue, 0, 255);
        }

        public void write(JsonWriter writer) throws IOException {
            writer.name(this.red.name).value(this.getRed()).name(this.green.name).value(this.getGreen()).name(this.blue.name).value(this.getBlue());
        }

        public void read(JsonObject jsonObject) {
            if (jsonObject.has(this.red.name)) {
                this.setRed(jsonObject.get(this.red.name).getAsInt());
            }

            if (jsonObject.has(this.green.name)) {
                this.setGreen(jsonObject.get(this.green.name).getAsInt());
            }

            if (jsonObject.has(this.blue.name)) {
                this.setBlue(jsonObject.get(this.blue.name).getAsInt());
            }
        }

        public int getRed() {
            return this.red.get();
        }

        public void setRed(int red) {
            this.red.set(red);
        }

        public int getGreen() {
            return this.green.get();
        }

        public void setGreen(int green) {
            this.green.set(green);
        }

        public int getBlue() {
            return this.blue.get();
        }

        public void setBlue(int blue) {
            this.blue.set(blue);
        }

        public int toRGB() {
            return MathHelper.packRgb(this.getRed(), this.getGreen(), this.getBlue());
        }
    }
}
