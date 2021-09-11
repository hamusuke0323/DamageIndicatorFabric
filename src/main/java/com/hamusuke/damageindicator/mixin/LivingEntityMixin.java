package com.hamusuke.damageindicator.mixin;

import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract boolean canBeRiddenInWater();

    @Shadow
    public abstract boolean isDead();

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void heal(float amount, CallbackInfo ci) {
        if (!this.world.isClient && amount > 0.0F && this.getHealth() > 0.0F && this.getHealth() != this.getMaxHealth()) {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            packetByteBuf.writeDouble(this.getX());
            packetByteBuf.writeDouble(this.getBodyY(this.random.nextDouble() + 0.5D));
            packetByteBuf.writeDouble(this.getZ());
            packetByteBuf.writeText(new LiteralText("+" + DamageIndicator.ceil(amount)).formatted(Formatting.GREEN));

            ((ServerWorld) this.world).getPlayers().forEach(serverPlayerEntity -> {
                if (serverPlayerEntity.distanceTo(this) < 64) {
                    serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, packetByteBuf));
                }
            });
        }
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isInvulnerableTo(source) && !this.world.isClient && !this.isDead() && !(source.isFire() && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) && amount > 0.0F) {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            packetByteBuf.writeDouble(this.getX());
            packetByteBuf.writeDouble(this.getBodyY(this.random.nextDouble() + 0.5D));
            packetByteBuf.writeDouble(this.getZ());
            packetByteBuf.writeText(new LiteralText(Long.toString(DamageIndicator.ceil(amount))).setStyle(Style.EMPTY.withColor(DamageIndicator.getColorFromDamageSource(source))));

            ((ServerWorld) this.world).getPlayers().forEach(serverPlayerEntity -> {
                if (serverPlayerEntity.distanceTo(this) < 64) {
                    serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, packetByteBuf));
                }
            });
        }
    }
}
