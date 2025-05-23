package com.loqor.client;

import com.loqor.LoqorsWeepingAngels;
import com.loqor.core.util.HeartbeatUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class AngelOverlay implements HudRenderCallback {

    public static final Identifier BLINK = LoqorsWeepingAngels.id("textures/overlay/blink.png");
    @Override
    public void onHudRender(DrawContext drawContext, float v) {
        if (MinecraftClient.getInstance().player == null) return;
        int i = drawContext.getScaledWindowWidth();
        int j = drawContext.getScaledWindowHeight();
        MatrixStack stack = drawContext.getMatrices();
        float delta = (float) MinecraftClient.getInstance().player.age / 2;
        int frame = 0;
        if (frame == 2) drawContext.fill(0, 0, i, j, ColorHelper.Argb.getArgb(255, 0, 0, 0));
        stack.push();

        //System.out.println(delta);
        stack.translate(i - 36, j - 36, 0);
        stack.scale(2, 2, 0);
        // (float) Math.sin(delta * 36);

        drawContext.drawTexture(BLINK, 0, 0, 0, frame * 18, 18, 18, 18, 54);
        stack.pop();

    }
}
