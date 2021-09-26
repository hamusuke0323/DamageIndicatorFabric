package com.hamusuke.damageindicator;

import com.hamusuke.damageindicator.command.SummonIndicatorCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.damage.DamageSource;

public class DamageIndicator implements ModInitializer {
    public static final String MOD_ID = "damageindicator";

    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            SummonIndicatorCommand.register(dispatcher);
        });
    }

    public static long ceil(float value) {
        long l = (long) value;
        return value > (float) l ? l + 1 : l;
    }

    public static int getColorFromDamageSource(DamageSource source) {
        if (source.isFire()) {
            return 16750080;
        } else if (source.isFromFalling() || source.isFallingBlock() || source == DamageSource.IN_WALL) {
            return 16769280;
        } else if (source.isOutOfWorld()) {
            return 0;
        }

        return 16777215;
    }
}
