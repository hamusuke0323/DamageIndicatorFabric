package com.hamusuke.damageindicator.invoker.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;

import java.util.Map;

@Environment(EnvType.CLIENT)
public interface FontManagerInvoker {
    Map<Identifier, FontStorage> getFontStorages();
}
