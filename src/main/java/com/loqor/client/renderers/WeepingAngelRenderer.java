package com.loqor.client.renderers;

import com.loqor.client.models.AngelModel;
import com.loqor.core.entities.WeepingAngelEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WeepingAngelRenderer<T extends WeepingAngelEntity> extends MobEntityRenderer<T, AngelModel<T>> {

    public WeepingAngelRenderer(EntityRendererFactory.Context context) {
        super(context, new AngelModel<>(AngelModel.getTexturedModelData().createModel()), 0.5f);
    }

    @Override
    public void render(T mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(WeepingAngelEntity entity) {
        return entity.getAngel().texture();
    }
}
