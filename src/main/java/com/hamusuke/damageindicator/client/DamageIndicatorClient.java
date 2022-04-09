package com.hamusuke.damageindicator.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.hamusuke.damageindicator.client.gui.screen.ConfigScreen;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import com.hamusuke.damageindicator.config.ClientConfig;
import com.hamusuke.damageindicator.network.DamageIndicatorPacket;
import com.hamusuke.damageindicator.network.NetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Queue;

@Environment(EnvType.CLIENT)
public class DamageIndicatorClient implements ClientModInitializer {
    public static final Queue<IndicatorRenderer> queue = Queues.newLinkedBlockingDeque();
    public static final ClientConfig clientConfig = new ClientConfig(FabricLoader.getInstance().getConfigDir().resolve("damageindicator/config.json").toFile());
    private static final KeyBinding OPEN_CONFIG = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.damageindicator.openConfig", GLFW.GLFW_KEY_V, "key.damageindicator.category.indicator"));
    private static final KeyBinding HIDE_INDICATOR = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.damageindicator.hideIndicator.desc", GLFW.GLFW_KEY_B, "key.damageindicator.category.indicator"));

    public void onInitializeClient() {
        clientConfig.load();

        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.DAMAGE_PACKET_ID, (client, handler, buf, responseSender) -> {
            if (client.world != null && client.player != null) {
                DamageIndicatorPacket packet = new DamageIndicatorPacket(buf);
                Entity entity = client.world.getEntityById(packet.getEntityId());

                if (entity instanceof LivingEntity livingEntity) {
                    double x = livingEntity.getParticleX(0.5D);
                    double y = livingEntity.getBodyY(MathHelper.nextDouble(livingEntity.getRandom(), 0.5D, 1.2D));
                    double z = livingEntity.getParticleZ(0.5D);
                    Vec3d vec3d = new Vec3d(x, y, z);
                    float distance = (float) client.gameRenderer.getCamera().getPos().distanceTo(vec3d);
                    BlockHitResult result = client.world.raycast(new RaycastContext(client.player.getPos(), vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));
                    if ((clientConfig.forciblyRenderIndicator.get() || result.getType() == HitResult.Type.MISS) && distance <= (float) clientConfig.renderDistance.get()) {
                        queue.add(new IndicatorRenderer(x, y, z, packet.getText(), packet.getSource(), packet.isCrit(), distance));
                    }
                }
            }
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

            while (OPEN_CONFIG.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen));
            }

            while (HIDE_INDICATOR.wasPressed()) {
                clientConfig.hideIndicator.toggle();
                clientConfig.save();
            }
        });
    }
}
