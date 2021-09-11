package com.hamusuke.damageindicator.network;

import com.hamusuke.damageindicator.DamageIndicator;
import net.minecraft.util.Identifier;

public class NetworkManager {
    public static final Identifier DAMAGE_PACKET_ID = new Identifier(DamageIndicator.MOD_ID, "damage_packet");
}
