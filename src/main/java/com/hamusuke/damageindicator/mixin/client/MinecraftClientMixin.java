package com.hamusuke.damageindicator.mixin.client;

import com.hamusuke.damageindicator.invoker.client.MinecraftClientInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements MinecraftClientInvoker {
    @Shadow
    @Final
    private FontManager fontManager;

    @Override
    public FontManager getFontManager() {
        return this.fontManager;
    }
}
