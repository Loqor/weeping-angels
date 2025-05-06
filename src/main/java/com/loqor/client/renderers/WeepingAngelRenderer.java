package com.loqor.client.renderers;

import com.loqor.core.entities.WeepingAngelEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class WeepingAngelRenderer extends EntityRenderer<WeepingAngelEntity> {
    SkeletonEntityModel model;
    public static final Identifier TEXTURE = new Identifier("textures/entity/skeleton/stray.png");
    public WeepingAngelRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new SkeletonEntityModel(SkeletonEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public void render(WeepingAngelEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(1.5f, -1.5f, -1.5f);
        matrices.translate(0, -1.5f, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(TEXTURE)), 0xf000f0,
                OverlayTexture.DEFAULT_UV, 1.0F, 1.0f, 1.0F, 1.0F);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(WeepingAngelEntity entity) {
        return TEXTURE;
    }
}
