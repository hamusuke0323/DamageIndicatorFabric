package com.hamusuke.damageindicator.mixin.client;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        if (!this.client.options.hudHidden && !DamageIndicatorClient.queue.isEmpty()) {
            this.client.getProfiler().push("damage indicator rendering");
            VertexConsumerProvider.Immediate impl = this.client.getBufferBuilders().getEntityVertexConsumers();
            matrices.push();
            DamageIndicatorClient.queue.forEach(indicatorRenderer -> indicatorRenderer.render(matrices, impl, this.client.getEntityRenderDispatcher().camera, tickDelta));
            matrices.pop();
            impl.draw();
            this.client.getProfiler().pop();
        }
    }
}
