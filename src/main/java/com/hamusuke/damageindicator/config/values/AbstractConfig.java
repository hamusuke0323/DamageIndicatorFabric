package com.hamusuke.damageindicator.config.values;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public abstract class AbstractConfig<V> {
    protected final String name;
    protected V value;

    public AbstractConfig(String name) {
        this.name = name;
    }

    public abstract void write(JsonWriter jsonWriter) throws IOException;

    public abstract void read(JsonObject jsonObject) throws Exception;

    public String getName() {
        return this.name;
    }

    public V get() {
        return this.value;
    }

    public void set(V value) {
        this.value = value;
    }
}
