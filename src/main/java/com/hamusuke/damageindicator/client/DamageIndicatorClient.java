package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.network.NetworkManager;
import com.hamusuke.damageindicator.renderer.IndicatorRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Queue;

@Environment(EnvType.CLIENT)
public class DamageIndicatorClient implements ClientModInitializer {
    public static final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();

    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.DAMAGE_PACKET_ID, (client, handler, buf, responseSender) -> {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            Text text = buf.readText();

            if (client.player == null) {
                return;
            }

            queue.add(new IndicatorRenderer(client.player.clientWorld, x, y, z, text));
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

    public static int getColorFromDamageSource(DamageSource source) {
        if (source.isFire()) {
            return 16750080;
        } else if (source.isFromFalling() || source.isFallingBlock()) {
            return 16769280;
        } else if (source.isOutOfWorld()) {
            return 0;
        }

        return 16777215;
    }
}
