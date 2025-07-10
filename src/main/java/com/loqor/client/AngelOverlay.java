package com.loqor.client;

import com.loqor.LoqorsWeepingAngels;
import com.loqor.client.config.LWAClientConfig;
import com.loqor.core.entities.WeepingAngelEntity;
import com.loqor.core.util.HeartbeatUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class AngelOverlay implements HudRenderCallback {

    public static final Identifier BLINK = LoqorsWeepingAngels.id("textures/overlay/blink.png");
    public static final Identifier DANGER = LoqorsWeepingAngels.id("textures/overlay/danger.png");
    @Override
    public void onHudRender(DrawContext drawContext, float v) {
        if (!LWAClientConfig.INSTANCE.instance().doScreenEffects) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        Vec3d pos = player.getPos();
        Box range = new Box(
                pos.subtract(10, 10, 10),
                pos.add(10, 10, 10)
        );

        List<WeepingAngelEntity> angels = player.getWorld().getEntitiesByClass(WeepingAngelEntity.class, range, WeepingAngelEntity::isAngryEnough);

        if (angels.isEmpty()) return;

        float closestDistance = (float) angels.stream()
                .mapToDouble(angel -> angel.getPos().distanceTo(player.getPos()))
                .min().orElse(10f);

        int i = drawContext.getScaledWindowWidth();
        int j = drawContext.getScaledWindowHeight();
        MatrixStack stack = drawContext.getMatrices();
        float delta = (float) player.age / 2;
        int frame = 0;
        if (frame == 2) drawContext.fill(0, 0, i, j, ColorHelper.Argb.getArgb(255, 0, 0, 0));
        stack.push();

        //System.out.println(delta);
        stack.translate(i - 36, j - 36 + Math.sin(delta * 56), 0);
        stack.scale(2, 2, 0);
        // (float) Math.sin(delta * 36);

        if (closestDistance < 10) {
            RenderSystem.setShaderColor(1, 0.25f, 0.25f, 1);
            drawContext.drawTexture(BLINK, 0, 0, 0, 0, 18, 18, 18, 54);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            //drawContext.drawTexture(BLINK, 0, 0, 0, 0, 18, 18 /* change this value later!!! - Loqor */, 18, 54);
        }

        stack.pop();

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        drawContext.setShaderColor(1, 0.25f, 0.25f, MathHelper.clamp(1 - closestDistance / 10 * 2, 0f, 0.8f));
        drawContext.drawTexture(DANGER, 0, 0, -90, 0.0f, 0.0f, i, j, i, j);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1f);
        RenderSystem.disableBlend();
    }
}
