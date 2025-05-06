package com.loqor.client.models;

import com.loqor.core.entities.WeepingAngelEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class AngelModel<T extends Entity> extends SinglePartEntityModel<T> {
	public final ModelPart Angel;
	public final ModelPart Head;
	public final ModelPart Body;
	public final ModelPart Wings;
	public final ModelPart Wing1;
	public final ModelPart Wing2;
	public final ModelPart RightArm;
	public final ModelPart LeftArm;
	public final ModelPart RightLeg;
	public final ModelPart LeftLeg;
	public AngelModel(ModelPart root) {
		this.Angel = root.getChild("Angel");
		this.Head = this.Angel.getChild("Head");
		this.Body = this.Angel.getChild("Body");
		this.Wings = this.Body.getChild("Wings");
		this.Wing1 = this.Wings.getChild("Wing1");
		this.Wing2 = this.Wings.getChild("Wing2");
		this.RightArm = this.Angel.getChild("RightArm");
		this.LeftArm = this.Angel.getChild("LeftArm");
		this.RightLeg = this.Angel.getChild("RightLeg");
		this.LeftLeg = this.Angel.getChild("LeftLeg");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData Angel = modelPartData.addChild("Angel", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData Head = Angel.addChild("Head", ModelPartBuilder.create().uv(20, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
		.uv(20, 16).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.25F)), ModelTransform.pivot(0.0F, -24.0F, 0.0F));

		ModelPartData Body = Angel.addChild("Body", ModelPartBuilder.create().uv(20, 32).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.0F))
		.uv(44, 32).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(0.0F, -24.0F, 0.0F));

		ModelPartData Wings = Body.addChild("Wings", ModelPartBuilder.create(), ModelTransform.pivot(-4.0F, 0.0F, 0.0F));

		ModelPartData Wing1 = Wings.addChild("Wing1", ModelPartBuilder.create().uv(56, 48).cuboid(0.5979F, 0.0F, 4.1769F, 1.0F, 9.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(1.0979F, -4.0F, 6.1769F, 0.0F, 29.0F, 10.0F, new Dilation(0.01F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 1.0036F, 0.0F));

		ModelPartData cube_r1 = Wing1.addChild("cube_r1", ModelPartBuilder.create().uv(0, 39).cuboid(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.5979F, -1.1695F, 5.7938F, -0.0873F, 0.0F, 0.0F));

		ModelPartData cube_r2 = Wing1.addChild("cube_r2", ModelPartBuilder.create().uv(6, 39).cuboid(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.001F)), ModelTransform.of(0.5979F, 0.3827F, 5.1008F, -0.3927F, 0.0F, 0.0F));

		ModelPartData Wing2 = Wings.addChild("Wing2", ModelPartBuilder.create().uv(56, 48).cuboid(2.5329F, 0.0F, -2.3071F, 1.0F, 9.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(3.0329F, -4.0F, -0.3071F, 0.0F, 29.0F, 10.0F, new Dilation(0.01F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.0036F, 0.0F));

		ModelPartData cube_r3 = Wing2.addChild("cube_r3", ModelPartBuilder.create().uv(0, 39).cuboid(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(2.5329F, -1.1695F, -0.6902F, -0.0873F, 0.0F, 0.0F));

		ModelPartData cube_r4 = Wing2.addChild("cube_r4", ModelPartBuilder.create().uv(6, 39).cuboid(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.001F)), ModelTransform.of(2.5329F, 0.3827F, -1.3833F, -0.3927F, 0.0F, 0.0F));

		ModelPartData RightArm = Angel.addChild("RightArm", ModelPartBuilder.create().uv(52, 0).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.0F, -22.0F, 0.0F));

		ModelPartData LeftArm = Angel.addChild("LeftArm", ModelPartBuilder.create().uv(52, 16).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(5.0F, -22.0F, 0.0F));

		ModelPartData RightLeg = Angel.addChild("RightLeg", ModelPartBuilder.create().uv(24, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F))
		.uv(0, 48).cuboid(-2.1F, 0.5F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.25F)), ModelTransform.pivot(-1.9F, -12.0F, 0.0F));

		ModelPartData LeftLeg = Angel.addChild("LeftLeg", ModelPartBuilder.create().uv(40, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(1.9F, -12.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.Angel.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart getPart() {
		return this.Angel;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}
}