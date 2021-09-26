package com.hamusuke.damageindicator.command;

import com.hamusuke.damageindicator.network.NetworkManager;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class SummonIndicatorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("summonindicator").then(CommandManager.argument("location", Vec3ArgumentType.vec3()).then(CommandManager.argument("text", TextArgumentType.text()).executes(context -> {
            return summon(context.getSource(), TextArgumentType.getTextArgument(context, "text"), Vec3ArgumentType.getVec3(context, "location"));
        }))));
    }

    private static int summon(ServerCommandSource source, Text text, Vec3d vec3d) {
        PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packetByteBuf.writeDouble(vec3d.x);
        packetByteBuf.writeDouble(vec3d.y);
        packetByteBuf.writeDouble(vec3d.z);
        packetByteBuf.writeText(text);

        source.getWorld().getPlayers().forEach(serverPlayerEntity -> {
            serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, packetByteBuf));
        });

        return 1;
    }
}
