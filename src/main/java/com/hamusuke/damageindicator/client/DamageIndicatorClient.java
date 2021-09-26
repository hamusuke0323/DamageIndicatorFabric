package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.client.event.ResourceReloadCompleteEvent;
import com.hamusuke.damageindicator.client.invoker.FontManagerInvoker;
import com.hamusuke.damageindicator.client.invoker.MinecraftClientInvoker;
import com.hamusuke.damageindicator.network.NetworkManager;
import com.hamusuke.damageindicator.renderer.IndicatorRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class DamageIndicatorClient implements ClientModInitializer {
    public static final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();
    public static final AtomicBoolean canReceiveDamagePacket = new AtomicBoolean();
    @Nullable
    private static TextRenderer customFont;

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

            addRenderer(x, y, z, text);
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

        ResourceReloadCompleteEvent.EVENT.register(client -> {
            FontManagerInvoker invoker = (FontManagerInvoker) ((MinecraftClientInvoker) client).getFontManager();
            Map<Identifier, FontStorage> map = invoker.getFontStorages();
            for (Map.Entry<Identifier, FontStorage> entry : map.entrySet()) {
                Identifier identifier = entry.getKey();
                if (identifier.getNamespace().equalsIgnoreCase(DamageIndicator.MOD_ID) || identifier.getPath().equalsIgnoreCase("default")) {
                    customFont = new TextRenderer((id) -> entry.getValue());
                }
            }
        });
    }

    public static void addRenderer(double x, double y, double z, Text text) {
        queue.add(new IndicatorRenderer(x, y, z, text, (float) MinecraftClient.getInstance().gameRenderer.getCamera().getPos().distanceTo(new Vec3d(x, y, z))));
    }

    public static TextRenderer getOrDefault(TextRenderer def) {
        return customFont == null ? def : customFont;
    }
}
