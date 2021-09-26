package com.hamusuke.damageindicator.renderer;

import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;

@Environment(EnvType.CLIENT)
public class IndicatorRenderer {
    private static final Box EMPTY_BOUNDING_BOX = new Box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    protected double x;
    protected double y;
    protected double z;
    protected double velocityX;
    protected double velocityY;
    protected double velocityZ;
    private Box boundingBox;
    private boolean moveTooQuickly;
    protected boolean dead;
    protected float spacingXZ;
    protected float spacingY;
    protected int age;
    protected int maxAge;
    protected float gravityStrength;
    protected float g;
    protected final Text text;
    protected long timeDelta;
    protected final float distance;

    public IndicatorRenderer(double x, double y, double z, Text text, float distance) {
        this.boundingBox = EMPTY_BOUNDING_BOX;
        this.spacingXZ = 0.6F;
        this.spacingY = 1.8F;
        this.g = 0.98F;
        this.setBoundingBoxSpacing(0.2F, 0.2F);
        this.setPos(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.maxAge = 20;
        this.gravityStrength = -0.2F;
        this.text = text;
        this.distance = distance / 2.0F;
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age == 0) {
            this.timeDelta = Util.getMeasuringTimeMs();
        }

        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else if (this.age > this.maxAge / 2) {
            this.velocityY -= 0.04D * (double) this.gravityStrength;
            this.move(this.velocityX, this.velocityY, this.velocityZ);
            this.velocityY *= this.g;
        }
    }

    public void render(MatrixStack matrix, VertexConsumerProvider vertexConsumers, Camera camera, int light, float tickDelta) {
        float scale = MathHelper.lerp((Util.getMeasuringTimeMs() - this.timeDelta) / 300.0F, 0.05F * this.distance, 0.025F * this.distance);
        scale = MathHelper.clamp(scale, 0.025F * this.distance, this.age > this.maxAge / 2 ? 0.025F * this.distance : 0.05F * this.distance);
        MinecraftClient client = MinecraftClient.getInstance();
        double x = MathHelper.lerp(tickDelta, this.prevPosX, this.x);
        double y = MathHelper.lerp(tickDelta, this.prevPosY, this.y);
        double z = MathHelper.lerp(tickDelta, this.prevPosZ, this.z);
        Vec3d camPos = camera.getPos();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;

        matrix.push();
        matrix.translate(x - camX, y - camY, z - camZ);
        matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
        matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrix.scale(-scale, -scale, scale);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int l = 255;
        if (this.age > this.maxAge / 2 && this.age != 0) {
            l = (int) ((((float) (this.maxAge + 1) / (float) this.age) - 1.0F) * 255.0F);
        }
        l = MathHelper.clamp(l, 0, 255);

        TextColor color = this.text.getStyle().getColor();
        TextRenderer textRenderer = DamageIndicatorClient.getOrDefault(client.textRenderer);
        if (color != null && color.getRgb() == 0) {
            textRenderer.drawWithOutline(this.text.asOrderedText(), -textRenderer.getWidth(this.text) / 2.0F, -textRenderer.fontHeight / 2.0F, l << 24, 16777215 + (l << 24), matrix.peek().getModel(), vertexConsumers, light);
        } else {
            textRenderer.draw(this.text, -textRenderer.getWidth(this.text) / 2.0F, -textRenderer.fontHeight / 2.0F, 16777215 + (l << 24), false, matrix.peek().getModel(), vertexConsumers, true, 0, light);
        }

        RenderSystem.disableBlend();
        matrix.pop();
    }

    public void markDead() {
        this.dead = true;
    }

    protected void setBoundingBoxSpacing(float spacingXZ, float spacingY) {
        if (spacingXZ != this.spacingXZ || spacingY != this.spacingY) {
            this.spacingXZ = spacingXZ;
            this.spacingY = spacingY;
            Box box = this.getBoundingBox();
            double d = (box.minX + box.maxX - (double) spacingXZ) / 2.0D;
            double e = (box.minZ + box.maxZ - (double) spacingXZ) / 2.0D;
            this.setBoundingBox(new Box(d, box.minY, e, d + (double) this.spacingXZ, box.minY + (double) this.spacingY, e + (double) this.spacingXZ));
        }
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float f = this.spacingXZ / 2.0F;
        float g = this.spacingY;
        this.setBoundingBox(new Box(x - (double) f, y, z - (double) f, x + (double) f, y + (double) g, z + (double) f));
    }

    public void move(double dx, double dy, double dz) {
        if (!this.moveTooQuickly) {
            if (dx != 0.0D || dy != 0.0D || dz != 0.0D) {
                this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
                this.repositionFromBoundingBox();
            }

            if (Math.abs(dy) >= 9.999999747378752E-6D && Math.abs(dy) < 9.999999747378752E-6D) {
                this.moveTooQuickly = true;
            }

            if (dx != dx) {
                this.velocityX = 0.0D;
            }

            if (dz != dz) {
                this.velocityZ = 0.0D;
            }
        }
    }

    protected void repositionFromBoundingBox() {
        Box box = this.getBoundingBox();
        this.x = (box.minX + box.maxX) / 2.0D;
        this.y = box.minY;
        this.z = (box.minZ + box.maxZ) / 2.0D;
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public Box getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(Box boundingBox) {
        this.boundingBox = boundingBox;
    }
}
