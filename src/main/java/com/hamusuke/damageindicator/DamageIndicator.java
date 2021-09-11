package com.hamusuke.damageindicator;

import net.minecraft.entity.damage.DamageSource;

public class DamageIndicator {
    public static final String MOD_ID = "damageindicator";

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
