package com.example.hpbar;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class HealthBarRenderer {
    private static final int LIGHT = 0xF000F0; // полная яркость
    private static final float BAR_W = 40f;
    private static final float BAR_H = 5f;

    private HealthBarRenderer() {}

    public static void onRender(WorldRenderContext context) {
        HpBarConfig c = HpBarConfig.get();
        if (!c.enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider vcp = context.consumers();
        if (matrices == null || vcp == null) return;

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(false);
        double maxSq = (double) c.maxDistance * c.maxDistance;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity e)) continue;
            if (e.isDead() || e.getHealth() <= 0f) continue;
            if (e == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            if (!c.shouldShow(e, mc)) continue;

            Vec3d pos = e.getLerpedPos(tickDelta);
            if (pos.squaredDistanceTo(camPos) > maxSq) continue;

            drawBar(c, mc, e, pos, camPos, camera, matrices, vcp);
        }
    }

    private static void drawBar(HpBarConfig c, MinecraftClient mc, LivingEntity e, Vec3d pos, Vec3d camPos,
                                Camera camera, MatrixStack matrices, VertexConsumerProvider vcp) {
        float hp = e.getHealth();
        float max = Math.max(e.getMaxHealth(), 0.001f);
        float ratio = Math.min(1f, Math.max(0f, hp / max));

        matrices.push();
        // у игроков и мобов с видимым именем ванильный ник висит на height+0.3..0.5,
        // поэтому полоску с нашим ником поднимаем выше него
        boolean vanillaTag = e instanceof PlayerEntity || e.isCustomNameVisible();
        double yOff = vanillaTag ? 0.7 : 0.25;
        matrices.translate(pos.x - camPos.x, pos.y - camPos.y + e.getHeight() + yOff, pos.z - camPos.z);
        matrices.multiply(camera.getRotation());
        float s = 0.025f * c.scale;
        matrices.scale(s, -s, s);

        Matrix4f m = matrices.peek().getPositionMatrix();
        VertexConsumer vc = vcp.getBuffer(c.throughWalls
            ? RenderLayer.getTextBackgroundSeeThrough()
            : RenderLayer.getTextBackground());

        float x1 = -BAR_W / 2f;
        float x2 = BAR_W / 2f;

        // рамка, фон, заполнение (z растёт в сторону камеры)
        rect(vc, m, x1 - 1, -1, x2 + 1, BAR_H + 1, 0.0f, 0xCC000000);
        rect(vc, m, x1, 0, x2, BAR_H, 0.1f, 0xFF3A1010);
        rect(vc, m, x1, 0, x1 + BAR_W * ratio, BAR_H, 0.2f, healthColor(ratio));

        TextRenderer.TextLayerType layerType =
            c.throughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;

        boolean showName = (e instanceof PlayerEntity) ? c.showPlayerNames : c.showMobNames;
        if (showName) {
            Text name = e.getDisplayName();
            TextRenderer ntr = mc.textRenderer;
            float w = ntr.getWidth(name);
            ntr.draw(name, -w / 2f, -11f, 0xFFFFFFFF, false, m, vcp, layerType, 0x55000000, LIGHT);
        }

        if (c.showText) {
            String text = (int) Math.ceil(hp) + "/" + (int) Math.ceil(max);
            TextRenderer tr = mc.textRenderer;
            tr.draw(text, x2 + 3f, -1.5f, 0xFFFFFFFF, false, m, vcp, layerType, 0, LIGHT);
        }

        matrices.pop();
    }

    /** зелёный -> жёлтый -> красный */
    private static int healthColor(float ratio) {
        int r, g;
        if (ratio > 0.5f) {
            r = (int) (255 * (1f - ratio) * 2f);
            g = 255;
        } else {
            r = 255;
            g = (int) (255 * ratio * 2f);
        }
        return 0xFF000000 | (r << 16) | (g << 8);
    }

    private static void rect(VertexConsumer vc, Matrix4f m, float x1, float y1, float x2, float y2, float z, int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = argb & 255;
        vc.vertex(m, x1, y2, z).color(r, g, b, a).light(LIGHT);
        vc.vertex(m, x2, y2, z).color(r, g, b, a).light(LIGHT);
        vc.vertex(m, x2, y1, z).color(r, g, b, a).light(LIGHT);
        vc.vertex(m, x1, y1, z).color(r, g, b, a).light(LIGHT);
    }
}
