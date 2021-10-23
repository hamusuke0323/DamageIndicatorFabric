package com.hamusuke.damageindicator.mixin;

import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.client.invoker.PlayerEntityInvoker;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    private static final float CRITICAL = 1.5F;

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract boolean canBeRiddenInWater();

    LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void heal(float amount, CallbackInfo ci) {
        amount = Math.min(this.getMaxHealth() - this.getHealth(), amount);
        if (!this.world.isClient && amount > 0.0F) {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            new DamageIndicatorPacket(this.getX(), this.getBodyY(this.random.nextDouble() + 0.5D), this.getZ(), new LiteralText("+" + MathHelper.ceil(amount)).formatted(Formatting.GREEN), 1.0F).write(packetByteBuf);

            ((ServerWorld) this.world).getPlayers().forEach(serverPlayerEntity -> {
                if (serverPlayerEntity.distanceTo(this) < 64) {
                    serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, packetByteBuf));
                }
            });
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER))
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        float scaleMul = 1.0F;
        if (source.getAttacker() instanceof PlayerEntity playerEntity) {
            scaleMul = ((PlayerEntityInvoker) playerEntity).isCritical() ? CRITICAL : 1.0F;
        }

        if (source.getSource() instanceof PersistentProjectileEntity projectile) {
            scaleMul = projectile.isCritical() ? CRITICAL : 1.0F;
        }

        PacketByteBuf packetByteBuf = PacketByteBufs.create();
        new DamageIndicatorPacket(this.getX(), this.getBodyY(this.random.nextDouble() + 0.5D), this.getZ(), new LiteralText("" + MathHelper.ceil(amount)).styled(style -> style.withColor(DamageIndicator.getColorFromDamageSource(source))), scaleMul).write(packetByteBuf);

        ((ServerWorld) this.world).getPlayers().forEach(serverPlayerEntity -> {
            if (serverPlayerEntity.distanceTo(this) < 64) {
                serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, packetByteBuf));
            }
        });
    }
}
