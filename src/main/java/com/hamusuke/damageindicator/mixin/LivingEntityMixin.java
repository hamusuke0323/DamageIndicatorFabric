package com.hamusuke.damageindicator.mixin;

import com.hamusuke.criticalib.invoker.LivingEntityInvoker;
import com.hamusuke.damageindicator.invoker.ILivingEntityInvoker;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
    protected int showImmuneCD;

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    protected float lastDamageTaken;

    LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (this.showImmuneCD > 0) {
            this.showImmuneCD--;
        }
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void heal(float amount, CallbackInfo ci) {
        amount = Math.min(this.getMaxHealth() - this.getHealth(), amount);
        if (!this.world.isClient && amount > 0.0F) {
            this.send(new LiteralText("+" + MathHelper.ceil(amount)), "heal", false);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerEntity) && !((Object) this instanceof ShulkerEntity) && !((Object) this instanceof WitherEntity)) {
            if (!this.world.isClient && !cir.getReturnValueZ() && this.canSendImmune(amount)) {
                this.sendImmune();
            }
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER))
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (!this.world.isClient) {
            this.send(new LiteralText("" + MathHelper.ceil(amount)), source.getName(), source.getAttacker() instanceof LivingEntityInvoker invoker && invoker.isCritical());
        }
    }

    @Override
    public void send(Text text, String source, boolean crit) {
        if (!this.world.isClient) {
            DamageIndicatorPacket damageIndicatorPacket = new DamageIndicatorPacket(this.getId(), text, source, crit);
            ((ServerWorld) this.world).getPlayers().forEach(serverPlayerEntity -> serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, damageIndicatorPacket.write(PacketByteBufs.create()))));
        }
    }

    @Override
    public void sendImmune() {
        this.showImmuneCD = 10;
        this.send(new TranslatableText("damageindicator.indicator.immune"), "immune", false);
    }

    @Override
    public boolean canSendImmune(float amount) {
        return this.getHealth() > 0.0F && this.showImmuneCD <= 0 && !((float) this.timeUntilRegen > 10.0F && amount <= this.lastDamageTaken);
    }
}
