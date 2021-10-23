package com.hamusuke.damageindicator.mixin;

import com.hamusuke.damageindicator.client.invoker.PlayerEntityInvoker;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityInvoker {
    protected boolean isCritical;

    @ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 2)
    private boolean attack$bl3(boolean bl3) {
        this.isCritical = bl3;
        return bl3;
    }

    public boolean isCritical() {
        return this.isCritical;
    }

    public void setCritical(boolean critical) {
        this.isCritical = critical;
    }
}
