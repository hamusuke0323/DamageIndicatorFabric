package com.hamusuke.damageindicator.mixin.client;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        if (!DamageIndicatorClient.canReceiveDamagePacket.get()) {
            Entity entity = this.world.getEntityById(packet.id());
            if (entity instanceof LivingEntity livingEntity && packet.getTrackedValues() != null) {
                float h = livingEntity.getHealth();
                for (DataTracker.Entry<?> entry : packet.getTrackedValues()) {
                    if (entry.getData().getId() == 9) {
                        Object o = entry.get();
                        if (o instanceof Float f && f > 0.0F && h > 0.0F) {
                            if (f < h) {
                                damage(livingEntity, h - f);
                            } else if (f > h) {
                                heal(livingEntity, f - h);
                            }
                        }
                    }
                }
            }
        }
    }

    private void heal(LivingEntity target, float amount) {
        DamageIndicatorClient.addRenderer(target.getX(), target.getY(), target.getZ(), new LiteralText("+" + MathHelper.ceil(amount)).formatted(Formatting.GREEN), 1.0F);
    }

    private void damage(LivingEntity target, float amount) {
        DamageIndicatorClient.addRenderer(target.getX(), target.getY(), target.getZ(), new LiteralText("" + MathHelper.ceil(amount)), 1.0F);
    }
}
