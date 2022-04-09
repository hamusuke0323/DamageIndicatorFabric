package com.hamusuke.damageindicator.config.values;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class BooleanValue extends AbstractConfig<Boolean> {
    public BooleanValue(String name) {
        this(name, false);
    }

    public BooleanValue(String name, boolean defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    @Override
    public void write(JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(this.name).value(this.value);
    }

    @Override
    public void read(JsonObject jsonObject) {
        if (jsonObject.has(this.name)) {
            this.value = jsonObject.get(this.name).getAsBoolean();
        }
    }

    public void toggle() {
        this.set(!this.get());
    }
}
