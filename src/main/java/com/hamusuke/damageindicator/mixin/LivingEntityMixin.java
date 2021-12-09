package com.hamusuke.damageindicator.mixin;

import com.hamusuke.criticalib.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.invoker.ILivingEntityInvoker;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntityInvoker {
    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract boolean isDead();

    @Shadow
    protected float lastDamageTaken;

    LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void heal(float amount, CallbackInfo ci) {
        amount = Math.min(this.getMaxHealth() - this.getHealth(), amount);
        if (!this.world.isClient && amount > 0.0F) {
            this.send(new LiteralText("+" + MathHelper.ceil(amount)).formatted(Formatting.GREEN), DamageIndicator.NORMAL);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.world.isClient && !cir.getReturnValue() && !this.isDead() && (float) this.timeUntilRegen <= 10.0F && amount > this.lastDamageTaken) {
            this.sendImmune();
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER))
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        float scaleMul = DamageIndicator.NORMAL;

        if (source.getAttacker() instanceof LivingEntityInvoker invoker) {
            if (invoker.isCritical()) {
                scaleMul = DamageIndicator.CRITICAL;
                invoker.setCritical(false);
            }
        }

        this.send(new LiteralText("" + MathHelper.ceil(amount)).styled(style -> style.withColor(DamageIndicator.getColorFromDamageSource(source))), scaleMul);
    }

    @Override
    public void send(Text text, float scaleMul) {
        if (!this.world.isClient) {
            DamageIndicatorPacket damageIndicatorPacket = new DamageIndicatorPacket(this.getParticleX(0.5D), this.getBodyY(MathHelper.nextDouble(this.random, 0.5D, 1.2D)), this.getParticleZ(0.5D), text, scaleMul);
            ((ServerWorld) this.world).getPlayers().forEach(serverPlayerEntity -> {
                if (serverPlayerEntity.distanceTo(this) < 64) {
                    serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, damageIndicatorPacket.write(PacketByteBufs.create())));
                }
            });
        }
    }
}
