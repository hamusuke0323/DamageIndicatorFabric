package com.hamusuke.damageindicator.invoker;

import net.minecraft.text.Text;

public interface ILivingEntityInvoker {
    default boolean canSendImmune(float amount) {
        return false;
    }

    default void send(Text text, String source, boolean crit) {
    }

    default void sendImmune() {
    }
}
