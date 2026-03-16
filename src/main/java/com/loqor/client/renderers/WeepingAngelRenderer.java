package com.loqor.client.renderers;

import com.loqor.client.models.AngelModel;
import com.loqor.client.renderers.feature.AngeredEyesFeatureRenderer;
import com.loqor.client.renderers.feature.AngeredMouthFeatureRenderer;
import com.loqor.core.entities.WeepingAngelEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WeepingAngelRenderer extends MobEntityRenderer<WeepingAngelEntity, AngelModel<WeepingAngelEntity>> {
    private static final Identifier[] DESTROY_STAGE_TEXTURES = new Identifier[] {
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

    public WeepingAngelRenderer(EntityRendererFactory.Context context) {
        super(context, new AngelModel<>(AngelModel.getTexturedModelData().createModel()), 0.5f);
        this.addFeature(new AngeredEyesFeatureRenderer(this));
        this.addFeature(new AngeredMouthFeatureRenderer(this));
    }

    @Override
    public void render(WeepingAngelEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        boolean shouldRenderDamageOverlay = mobEntity.getHealth() < mobEntity.getMaxHealth();
        RenderLayer damageLayer = null;
        if (shouldRenderDamageOverlay) {
            int stage = 9 - (int) (mobEntity.getHealth() / mobEntity.getMaxHealth() * 9.0f);
            stage = Math.max(0, Math.min(9, stage));
            damageLayer = RenderLayer.getBlockBreaking(DESTROY_STAGE_TEXTURES[stage]);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        boolean isVisible = this.isVisible(mobEntity);
        boolean shouldRenderTranslucentLayer = !isVisible && !mobEntity.isInvisibleTo(client.player);
        boolean hasOutline = client.hasOutline(mobEntity);
        RenderLayer baseRenderLayer = this.getRenderLayer(mobEntity, isVisible, shouldRenderTranslucentLayer, hasOutline);
        RenderLayer finalDamageLayer = damageLayer;
        VertexConsumerProvider provider = requestedLayer -> {
            VertexConsumer baseConsumer = vertexConsumerProvider.getBuffer(requestedLayer);
            if (!shouldRenderDamageOverlay || finalDamageLayer == null || baseRenderLayer == null || requestedLayer != baseRenderLayer) {
                return baseConsumer;
            }

            // Only union on the base model layer; other layers (text/features) may use incompatible formats.
            MatrixStack.Entry entry = matrixStack.peek();
            VertexConsumer damageConsumer = new OverlayVertexConsumer(vertexConsumerProvider.getBuffer(finalDamageLayer),
                    entry.getPositionMatrix(), entry.getNormalMatrix(), 1.0f);
            return VertexConsumers.union(damageConsumer, baseConsumer);
        };
        super.render(mobEntity, f, g, matrixStack, provider, i);
    }

    @Override
    public Identifier getTexture(WeepingAngelEntity entity) {
        return entity.getAngel().texture();
    }
}
