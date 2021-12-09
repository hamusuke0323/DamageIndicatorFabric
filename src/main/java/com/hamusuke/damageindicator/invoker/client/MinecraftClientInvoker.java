package com.hamusuke.damageindicator.invoker.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontManager;

@Environment(EnvType.CLIENT)
public interface MinecraftClientInvoker {
    FontManager getFontManager();
}
