package com.loqor.client.renderers.feature;

import com.loqor.client.models.AngelModel;
import com.loqor.core.entities.WeepingAngelEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class DamageOverlayFeatureRenderer extends FeatureRenderer<WeepingAngelEntity, AngelModel<WeepingAngelEntity>> {
    private static final Identifier[] DESTROY_STAGE_TEXTURES = new Identifier[]{
            new Identifier("textures/block/destroy_stage_0.png"),
            new Identifier("textures/block/destroy_stage_1.png"),
            new Identifier("textures/block/destroy_stage_2.png"),
            new Identifier("textures/block/destroy_stage_3.png"),
            new Identifier("textures/block/destroy_stage_4.png"),
            new Identifier("textures/block/destroy_stage_5.png"),
            new Identifier("textures/block/destroy_stage_6.png"),
            new Identifier("textures/block/destroy_stage_7.png"),
            new Identifier("textures/block/destroy_stage_8.png"),
            new Identifier("textures/block/destroy_stage_9.png")
    };

    /**
     * The angel model atlas is 128x128; vanilla destroy_stage textures are 16x16.
     * Scaling UVs by 8 makes one 16-pixel face on the atlas sample the full 16x16
     * crack texture, so the pattern tiles naturally across the whole model.
     */
    private static final float UV_SCALE = 128.0f / 16.0f;

    public DamageOverlayFeatureRenderer(FeatureRendererContext<WeepingAngelEntity, AngelModel<WeepingAngelEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, WeepingAngelEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity.isInvisible() || entity.getHealth() >= entity.getMaxHealth()) {
            return;
        }

        VertexConsumer base = vertexConsumerProvider.getBuffer(
                RenderLayer.getBlockBreaking(getDamageTexture(entity)));
        this.getContextModel().render(
                matrixStack, new ScaledUvVertexConsumer(base, UV_SCALE),
                light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static Identifier getDamageTexture(WeepingAngelEntity entity) {
        float max = entity.getMaxHealth();
        if (max <= 0.0f) return DESTROY_STAGE_TEXTURES[9];
        int stage = (int) ((1.0f - entity.getHealth() / max) * 10.0f);
        return DESTROY_STAGE_TEXTURES[Math.max(0, Math.min(9, stage))];
    }

    /**
     * Delegates every vertex call to the wrapped consumer, but multiplies the
     * texture U and V coordinates by {@code scale} so vanilla 16x16 crack textures
     * tile at the correct density over the angel model's 128x128 UV space.
     */
    private record ScaledUvVertexConsumer(VertexConsumer delegate, float scale) implements VertexConsumer {
        @Override public VertexConsumer vertex(double x, double y, double z)       { delegate.vertex(x, y, z);       return this; }
        @Override public VertexConsumer color(int r, int g, int b, int a)          { delegate.color(r, g, b, a);     return this; }
        @Override public VertexConsumer texture(float u, float v)                  { delegate.texture(u * scale, v * scale); return this; }
        @Override public VertexConsumer overlay(int u, int v)                      { delegate.overlay(u, v);         return this; }
        @Override public VertexConsumer light(int u, int v)                        { delegate.light(u, v);           return this; }
        @Override public VertexConsumer normal(float x, float y, float z)          { delegate.normal(x, y, z);       return this; }
        @Override public void next()                                                { delegate.next(); }
        @Override public void fixedColor(int r, int g, int b, int a)               { delegate.fixedColor(r, g, b, a); }
        @Override public void unfixColor()                                          { delegate.unfixColor(); }
    }
}
