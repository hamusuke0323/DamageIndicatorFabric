package com.hamusuke.damageindicator.client.invoker;

public interface PlayerEntityInvoker {
    boolean isCritical();

    void setCritical(boolean critical);
}
