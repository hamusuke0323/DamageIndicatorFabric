package com.hamusuke.damageindicator.invoker;

import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public interface ILivingEntityInvoker {
    default void send(Text text, float scaleMul) {
    }

    default void sendImmune() {
        this.send(new TranslatableText("critical.indicator.immune").styled(style -> style.withColor(Formatting.GRAY)), DamageIndicator.NORMAL);
    }
}
