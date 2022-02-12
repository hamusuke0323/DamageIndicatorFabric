package com.hamusuke.damageindicator.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class DamageIndicatorPacket {
    private final int entityId;
    private final Text text;
    private final String source;
    private final boolean crit;

    public DamageIndicatorPacket(int entityId, Text text, String source, boolean crit) {
        this.entityId = entityId;
        this.text = text;
        this.source = source;
        this.crit = crit;
    }

    public DamageIndicatorPacket(PacketByteBuf packetByteBuf) {
        this.entityId = packetByteBuf.readVarInt();
        this.text = packetByteBuf.readText();
        this.source = packetByteBuf.readString();
        this.crit = packetByteBuf.readBoolean();
    }

    public PacketByteBuf write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeVarInt(this.entityId);
        packetByteBuf.writeText(this.text);
        packetByteBuf.writeString(this.source);
        packetByteBuf.writeBoolean(this.crit);
        return packetByteBuf;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Text getText() {
        return this.text;
    }

    public String getSource() {
        return this.source;
    }

    public boolean isCrit() {
        return this.crit;
    }
}
