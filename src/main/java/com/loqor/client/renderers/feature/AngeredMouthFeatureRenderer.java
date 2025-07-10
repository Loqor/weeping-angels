package com.loqor.client.renderers.feature;

import com.loqor.LoqorsWeepingAngels;
import com.loqor.client.models.AngelModel;
import com.loqor.core.angels.Angel;
import com.loqor.core.entities.WeepingAngelEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AngeredMouthFeatureRenderer
        extends FeatureRenderer<WeepingAngelEntity, AngelModel<WeepingAngelEntity>> {

    public AngeredMouthFeatureRenderer(FeatureRendererContext<WeepingAngelEntity, AngelModel<WeepingAngelEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, WeepingAngelEntity weepingAngelEntity, float f, float g, float h, float j, float k, float l) {
        if (weepingAngelEntity.isInvisible()) {
            return;
        }

        if (!weepingAngelEntity.isAngryEnough()) {
            return;
        }

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEyes(Angel.getAngeredTexture(weepingAngelEntity.getAngel().texture())));

        this.getContextModel().render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
