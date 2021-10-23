package com.hamusuke.damageindicator.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class DamageIndicatorPacket {
    private final double x;
    private final double y;
    private final double z;
    private final Text text;
    private final float scaleMultiplier;

    public DamageIndicatorPacket(double x, double y, double z, Text text, float scaleMultiplier) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.text = text;
        this.scaleMultiplier = scaleMultiplier;
    }

    public DamageIndicatorPacket(PacketByteBuf packetByteBuf) {
        this.x = packetByteBuf.readDouble();
        this.y = packetByteBuf.readDouble();
        this.z = packetByteBuf.readDouble();
        this.text = packetByteBuf.readText();
        this.scaleMultiplier = packetByteBuf.isReadable() ? packetByteBuf.readFloat() : 1.0F;
    }

    public void write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeDouble(this.x);
        packetByteBuf.writeDouble(this.y);
        packetByteBuf.writeDouble(this.z);
        packetByteBuf.writeText(this.text);
        packetByteBuf.writeFloat(this.scaleMultiplier);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public Text getText() {
        return this.text;
    }

    public float getScaleMultiplier() {
        return this.scaleMultiplier;
    }
}
