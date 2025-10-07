package com.loqor.client.renderers;

import com.loqor.client.models.AngelModel;
import com.loqor.client.renderers.feature.AngeredEyesFeatureRenderer;
import com.loqor.client.renderers.feature.AngeredMouthFeatureRenderer;
import com.loqor.core.entities.WeepingAngelEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class WeepingAngelRenderer extends MobEntityRenderer<WeepingAngelEntity, AngelModel<WeepingAngelEntity>> {

    public WeepingAngelRenderer(EntityRendererFactory.Context context) {
        super(context, new AngelModel<>(AngelModel.getTexturedModelData().createModel()), 0.5f);
        this.addFeature(new AngeredEyesFeatureRenderer(this));
        this.addFeature(new AngeredMouthFeatureRenderer(this));
    }

    @Override
    public void render(WeepingAngelEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        RenderLayer layer = RenderLayer.getBlockBreaking(new Identifier("textures/block/destroy_stage_" + (9 -
                ((int) ((mobEntity.getHealth() / mobEntity.getMaxHealth() * 9)))) + ".png"));
        VertexConsumer vertexConsumer = new OverlayVertexConsumer(vertexConsumerProvider.getBuffer(layer),
                matrixStack.peek().getPositionMatrix(), matrixStack.peek().getNormalMatrix(), 1.0f);
        RenderLayer originalLayer = model.getLayer(this.getTexture(mobEntity));
        VertexConsumer vC = vertexConsumerProvider.getBuffer(originalLayer);
        VertexConsumerProvider provider = layer1 -> VertexConsumers.union(vertexConsumer, vC);
        super.render(mobEntity, f, g, matrixStack, provider, i);
    }

    @Override
    public Identifier getTexture(WeepingAngelEntity entity) {
        return entity.getAngel().texture();
    }
}
