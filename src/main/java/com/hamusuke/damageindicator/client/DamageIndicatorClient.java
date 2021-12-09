package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import com.hamusuke.damageindicator.invoker.client.FontManagerInvoker;
import com.hamusuke.damageindicator.invoker.client.MinecraftClientInvoker;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class DamageIndicatorClient implements ClientModInitializer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();
    private static final AtomicReference<TextRenderer> customFont = new AtomicReference<>();

    public static void addRenderer(double x, double y, double z, Text text, float scaleMultiplier) {
        queue.add(new IndicatorRenderer(x, y, z, text, (float) client.gameRenderer.getCamera().getPos().distanceTo(new Vec3d(x, y, z)), scaleMultiplier));
    }

    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.DAMAGE_PACKET_ID, (client, handler, buf, responseSender) -> {
            DamageIndicatorPacket packet = new DamageIndicatorPacket(buf);
            addRenderer(packet.getX(), packet.getY(), packet.getZ(), packet.getText(), packet.getScaleMultiplier());
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

    public static void createCustomFont() {
        for (Map.Entry<Identifier, FontStorage> entry : ((FontManagerInvoker) ((MinecraftClientInvoker) client).getFontManager()).getFontStorages().entrySet()) {
            Identifier identifier = entry.getKey();
            if (identifier.getNamespace().equalsIgnoreCase(DamageIndicator.MOD_ID) || identifier.getPath().equalsIgnoreCase("default")) {
                customFont.set(new TextRenderer((id) -> entry.getValue()));
            }
        }
    }

    public static TextRenderer getOrDefault(TextRenderer def) {
        return customFont.get() == null ? def : customFont.get();
    }
}
