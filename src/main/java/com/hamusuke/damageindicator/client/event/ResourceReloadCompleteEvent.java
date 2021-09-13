package com.hamusuke.damageindicator.client.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public interface ResourceReloadCompleteEvent {
    Event<ResourceReloadCompleteEvent> EVENT = EventFactory.createArrayBacked(ResourceReloadCompleteEvent.class, listener -> client -> {
        for (ResourceReloadCompleteEvent event : listener) {
            event.onReloadComplete(client);
        }
    });

    void onReloadComplete(MinecraftClient client);
}
