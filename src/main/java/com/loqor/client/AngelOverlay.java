package com.loqor.client;

import com.loqor.LoqorsWeepingAngels;
import com.loqor.client.config.LWAClientConfig;
import com.loqor.core.entities.WeepingAngelEntity;
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
    // public static final Identifier DANGER = LoqorsWeepingAngels.id("textures/overlay/danger.png");
    private static final float EYE_EFFECT_DISTANCE = 24.0f;
    private static final float EYE_SHAKE_DISTANCE = 6.0f;
    private static final float EYE_ICON_SIZE = 18.0f;
    private static final float EYE_MARGIN = 8.0f;
    private static final int EYE_FRAME_SIZE = 18;
    private static final int EYE_FRAME_COUNT = 3;

    @Override
    public void onHudRender(DrawContext drawContext, float v) {
        if (!LWAClientConfig.INSTANCE.instance().doScreenEffects) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        float effectDistance = (float) Math.max(1.0D, LWAClientConfig.INSTANCE.instance().dangerEffectDistance);
        float queryDistance = Math.max(effectDistance, EYE_EFFECT_DISTANCE);
        Vec3d pos = player.getPos();
        Box range = new Box(
                pos.subtract(queryDistance, queryDistance, queryDistance),
                pos.add(queryDistance, queryDistance, queryDistance)
        );

        List<WeepingAngelEntity> angels = player.getWorld().getEntitiesByClass(WeepingAngelEntity.class, range, angel -> true);

        if (angels.isEmpty()) return;

        float closestDistance = (float) angels.stream()
                .mapToDouble(angel -> angel.getPos().distanceTo(player.getPos()))
                .min().orElse(effectDistance);

        float eyeTintProgress = MathHelper.clamp(
                (EYE_EFFECT_DISTANCE - closestDistance) / (EYE_EFFECT_DISTANCE - EYE_SHAKE_DISTANCE),
                0.0f,
                1.0f
        );
        float eyeShakeProgress = MathHelper.clamp((EYE_SHAKE_DISTANCE - closestDistance) / EYE_SHAKE_DISTANCE, 0.0f, 1.0f);
        // float dangerProximityFactor = MathHelper.clamp(1.0f - (closestDistance / effectDistance), 0.0f, 1.0f);
        boolean inEyeRange = closestDistance <= EYE_EFFECT_DISTANCE;
        boolean inDangerRange = closestDistance <= effectDistance;
        if (!inEyeRange && !inDangerRange) {
            return;
        }

        int i = drawContext.getScaledWindowWidth();
        int j = drawContext.getScaledWindowHeight();
        MatrixStack stack = drawContext.getMatrices();
        float delta = (float) player.age / 2;
        boolean dynamicEye = LWAClientConfig.INSTANCE.instance().dynamicEyeIntensity;
        float eyeScale = (float) Math.max(0.5D, LWAClientConfig.INSTANCE.instance().eyeScale);
        int scaledEyeSize = Math.max(1, MathHelper.ceil(EYE_ICON_SIZE * eyeScale));
        float shakeAmplitude = dynamicEye ? (eyeShakeProgress * 8.0f) : 0.0f;
        float iconSize = scaledEyeSize;
        float baseX;
        float baseY;
        switch (LWAClientConfig.INSTANCE.instance().eyeAnchor) {
            case TOP_LEFT -> {
                baseX = EYE_MARGIN;
                baseY = EYE_MARGIN;
            }
            case TOP_RIGHT -> {
                baseX = i - iconSize - EYE_MARGIN;
                baseY = EYE_MARGIN;
            }
            case BOTTOM_LEFT -> {
                baseX = EYE_MARGIN;
                baseY = j - iconSize - EYE_MARGIN;
            }
            case UNDER_CURSOR -> {
                baseX = (float) i / 2 - iconSize / 2;
                baseY = (float) j / 2 + EYE_MARGIN;
            }
            default -> {
                baseX = i - iconSize - EYE_MARGIN;
                baseY = j - iconSize - EYE_MARGIN;
            }
        }
        stack.push();

        float shakeY = (float) Math.sin(delta * 56.0f) * shakeAmplitude;
        int drawX = MathHelper.floor(baseX);
        int drawY = MathHelper.floor(baseY + shakeY);

        stack.translate(drawX, drawY, 0);
        stack.scale(eyeScale,eyeScale, 0);

        if (inEyeRange) {
            boolean fullyRed = eyeTintProgress >= 1.0f;
            int eyeFrame = fullyRed ? 0 : getBlinkFrame(player.age);
            int eyeFrameV = eyeFrame * EYE_FRAME_SIZE;

            // Base eye icon (default texture look).
            RenderSystem.setShaderColor(1, 1, 1, 1);
            drawContext.drawTexture(BLINK, 0, 0, 0, eyeFrameV, EYE_FRAME_SIZE, EYE_FRAME_SIZE, EYE_FRAME_SIZE, EYE_FRAME_SIZE * EYE_FRAME_COUNT);

            // Transition from default to red tint from 24 -> 6 blocks.
            if (dynamicEye && eyeTintProgress > 0.0f) {
                RenderSystem.setShaderColor(1, 0.25f, 0.25f, eyeTintProgress);
                drawContext.drawTexture(BLINK, 0, 0, 0, eyeFrameV, EYE_FRAME_SIZE, EYE_FRAME_SIZE, EYE_FRAME_SIZE, EYE_FRAME_SIZE * EYE_FRAME_COUNT);
            }

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        stack.pop();

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // FIXME: this doesn't work on servers, plus it's just kind of annoying. Maybe add a config option to disable the red screen effect and only show the eye icon later?
        /*if (inDangerRange) {
            drawContext.setShaderColor(1, 0.25f, 0.25f, MathHelper.clamp(dangerProximityFactor, 0f, 0.8f));
            drawContext.drawTexture(DANGER, 0, 0, -90, 0.0f, 0.0f, i, j, i, j);
        }*/
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1f);
        RenderSystem.disableBlend();
    }

    /**
     * Drives blink animation using a raised half-cosine so the eye spends
     * most of the time open and briefly dips through half-closed (frame 1)
     * to fully closed (frame 2) once per BLINK_PERIOD_TICKS.
     *
     * blinkValue = ((1 + cos(t)) / 2) ^ BLINK_POWER
     *   → 1.0 at t=0  (eye open, frame 0)
     *   → 0.0 at t=π  (eye closed, frame 2)
     *   Exponent 4 makes the curve spend ~85% of the period above 0.7 (open).
     */
    private static final double BLINK_PERIOD_TICKS = 50f;
    private static final double BLINK_POWER = 20.0;

    private static int getBlinkFrame(int ageTicks) {
        double t = (ageTicks / BLINK_PERIOD_TICKS) * 2.0 * Math.PI;
        double blinkValue = Math.pow((1.0 + Math.cos(t)) / 2.0, BLINK_POWER);
        if (blinkValue > 0.7) return 2; // fully closed
        if (blinkValue > 0.2) return 1; // half closed
        return 0;                        // open
    }
}
