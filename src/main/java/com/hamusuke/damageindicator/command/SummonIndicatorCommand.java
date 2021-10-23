package com.hamusuke.damageindicator.command;

import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
            return summon(context.getSource(), TextArgumentType.getTextArgument(context, "text"), Vec3ArgumentType.getVec3(context, "location"), 1.0F);
        }).then(CommandManager.argument("scaleMultiplier", FloatArgumentType.floatArg(1.0F, 2.0F)).executes(context -> {
            return summon(context.getSource(), TextArgumentType.getTextArgument(context, "text"), Vec3ArgumentType.getVec3(context, "location"), FloatArgumentType.getFloat(context, "scaleMultiplier"));
        })))));
    }

    private static int summon(ServerCommandSource source, Text text, Vec3d vec3d, float scaleMultiplier) {
        PacketByteBuf packetByteBuf = PacketByteBufs.create();
        new DamageIndicatorPacket(vec3d.x, vec3d.y, vec3d.z, text, scaleMultiplier).write(packetByteBuf);

        source.getWorld().getPlayers().forEach(serverPlayerEntity -> {
            serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(NetworkManager.DAMAGE_PACKET_ID, packetByteBuf));
        });

        return 1;
    }
}
