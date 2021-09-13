package com.hamusuke.damageindicator.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.hamusuke.damageindicator.client.invoker.FontManagerInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(FontManager.class)
public abstract class FontManagerMixin implements FontManagerInvoker {
    @Shadow
    @Final
    Map<Identifier, FontStorage> fontStorages;

    public ImmutableMap<Identifier, FontStorage> getFontStorages() {
        return ImmutableMap.copyOf(this.fontStorages);
    }
}
