package com.hamusuke.damageindicator.client.invoker;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface FontManagerInvoker {
    ImmutableMap<Identifier, FontStorage> getFontStorages();
}
