package com.hamusuke.damageindicator;

import net.minecraft.entity.damage.DamageSource;

public class DamageIndicator {
    public static final String MOD_ID = "damageindicator";
    public static final float NORMAL = 1.0F;
    public static final float CRITICAL = 2.0F;

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
