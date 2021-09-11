package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.network.NetworkManager;
import com.hamusuke.damageindicator.renderer.IndicatorRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class DamageIndicatorClient implements ClientModInitializer {
    public static final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();
    public static final AtomicBoolean canReceiveDamagePacket = new AtomicBoolean();

    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.DAMAGE_PACKET_ID, (client, handler, buf, responseSender) -> {
            if (!canReceiveDamagePacket.get()) {
                canReceiveDamagePacket.set(true);
                return;
            }

            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            Text text = buf.readText();

            if (client.player == null) {
                return;
            }

            addRenderer(client.player.clientWorld, x, y, z, text);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            canReceiveDamagePacket.set(false);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.isPaused()) {
                List<IndicatorRenderer> list = Lists.newArrayList();
                queue.forEach(indicatorRenderer -> {
                    indicatorRenderer.tick();
                    if (!indicatorRenderer.isAlive()) {
                        list.add(indicatorRenderer);
                    }
                });

                queue.removeAll(list);
            }
        });
    }

    public static void addRenderer(ClientWorld clientWorld, double x, double y, double z, Text text) {
        queue.add(new IndicatorRenderer(clientWorld, x, y, z, text, (float) MinecraftClient.getInstance().gameRenderer.getCamera().getPos().distanceTo(new Vec3d(x, y, z))));
    }
}
