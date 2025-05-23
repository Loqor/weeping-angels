package com.loqor.client.models;

import com.loqor.core.entities.WeepingAngelEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class AngelModel<T extends WeepingAngelEntity> extends SinglePartEntityModel<T> {

	private final ModelPart Angel;
	private final ModelPart Head;
	private final ModelPart Body;
	private final ModelPart Wings;
	private final ModelPart Wing1;
	private final ModelPart Wing2;
	private final ModelPart RightArm;
	private final ModelPart LeftArm;
	private final ModelPart dress;

	public AngelModel(ModelPart root) {
		this.Angel = root.getChild("Angel");
		this.Head = this.Angel.getChild("Head");
		this.Body = this.Angel.getChild("Body");
		this.Wings = this.Body.getChild("Wings");
		this.Wing1 = this.Wings.getChild("Wing1");
		this.Wing2 = this.Wings.getChild("Wing2");
		this.RightArm = this.Angel.getChild("RightArm");
		this.LeftArm = this.Angel.getChild("LeftArm");
		this.dress = this.Angel.getChild("dress");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData Angel = modelPartData.addChild("Angel", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData Head = Angel.addChild("Head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
		.uv(62, 55).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.5F)), ModelTransform.pivot(0.0F, -24.0F, 0.0F));

		ModelPartData Body = Angel.addChild("Body", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.0F))
		.uv(16, 32).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.25F))
		.uv(32, 62).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 16.0F, 4.0F, new Dilation(0.55F)), ModelTransform.pivot(0.0F, -24.0F, 0.0F));

		ModelPartData Wings = Body.addChild("Wings", ModelPartBuilder.create(), ModelTransform.pivot(-4.0F, 0.0F, 0.0F));

		ModelPartData Wing1 = Wings.addChild("Wing1", ModelPartBuilder.create().uv(61, 16).cuboid(0.5979F, -4.0F, 6.1769F, 1.0F, 28.0F, 10.0F, new Dilation(0.0F))
		.uv(32, 48).cuboid(0.0979F, -4.0F, 6.1769F, 2.0F, 4.0F, 10.0F, new Dilation(0.001F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 1.0036F, 0.0F));

		ModelPartData Wing2 = Wings.addChild("Wing2", ModelPartBuilder.create().uv(61, 16).mirrored().cuboid(2.5329F, -4.0F, -0.3071F, 1.0F, 28.0F, 10.0F, new Dilation(0.0F)).mirrored(false)
		.uv(32, 48).mirrored().cuboid(2.0329F, -4.0F, -0.3071F, 2.0F, 4.0F, 10.0F, new Dilation(0.001F)).mirrored(false), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.0036F, 0.0F));

		ModelPartData RightArm = Angel.addChild("RightArm", ModelPartBuilder.create().uv(40, 16).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.0F, -22.0F, 0.0F));

		ModelPartData LeftArm = Angel.addChild("LeftArm", ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(5.0F, -22.0F, 0.0F));

		ModelPartData dress = Angel.addChild("dress", ModelPartBuilder.create().uv(53, 0).cuboid(-2.1F, 0.5F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.45F))
		.uv(0, 50).cuboid(-2.1F, -5.5F, -2.0F, 8.0F, 18.0F, 4.0F, new Dilation(0.75F)), ModelTransform.pivot(-1.9F, -12.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		Angel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, alpha);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.rotationsForEachPose(entity.getAngelPose());
	}

	@Override
	public ModelPart getPart() {
		return this.Angel;
	}

	public void rotationsForEachPose(WeepingAngelEntity.AngelPose pose) {
		switch (pose) {
			case RETREATING -> {
				this.Angel.yaw = (float) (180f * (Math.PI / 180f));
			}
			case ATTACKING, MOVING -> {
				this.Angel.yaw = (float) (0 * (Math.PI / 180f));
				this.Head.pitch = (float) (0 * (Math.PI / 180f));
				this.RightArm.pitch = (float) (-90.4541127733f * (Math.PI / 180f));
				this.RightArm.yaw = (float) (-2.993175786f * (Math.PI / 180f));
				this.RightArm.roll = (float) (-24.6656943499f * (Math.PI / 180f));
				this.LeftArm.pitch = (float) (-80.9205325268f * (Math.PI / 180f));
				this.LeftArm.yaw = (float) (4.2085425191f * (Math.PI / 180f));
				this.LeftArm.roll = (float) (-24.6656943499f * (Math.PI / 180f));
			}
			case AFRAID -> {
				this.Angel.yaw = (float) (180f * (Math.PI / 180f));
				this.Head.pitch = (float) (30f * (Math.PI / 180f));
				this.RightArm.pitch = (float) (-93.6472701325 * (Math.PI / 180f));
				this.RightArm.yaw = (float) (-38.6358981994f * (Math.PI / 180f));
				this.RightArm.roll = (float) (-35.8762349975f * (Math.PI / 180f));
				this.LeftArm.pitch = (float) (-93.6472701325f * (Math.PI / 180f));
				this.LeftArm.yaw = (float) (38.6358981994f * (Math.PI / 180f));
				this.LeftArm.roll = (float) (35.8762349975f * (Math.PI / 180f));
			}
			case ANGRY -> {
				this.Angel.yaw = (float) (0 * (Math.PI / 180f));
				this.Head.pitch = (float) (-45f * (Math.PI / 180f));
				this.RightArm.pitch = (float) (-132.5082921552 * (Math.PI / 180f));
				this.RightArm.yaw = (float) (18.9664339657f * (Math.PI / 180f));
				this.RightArm.roll = (float) (-29.1170963085f * (Math.PI / 180f));
				this.LeftArm.pitch = (float) (-132.5082921552f * (Math.PI / 180f));
				this.LeftArm.yaw = (float) (18.9664339657f * (Math.PI / 180f));
				this.LeftArm.roll = (float) (29.1170963085f * (Math.PI / 180f));
			}
			case HIDING -> {
				this.Angel.yaw = (float) (0 * (Math.PI / 180f));
				this.Head.pitch = (float) (30f * (Math.PI / 180f));
				this.RightArm.pitch = (float) (-93.6472701325 * (Math.PI / 180f));
				this.RightArm.yaw = (float) (-38.6358981994f * (Math.PI / 180f));
				this.RightArm.roll = (float) (-35.8762349975f * (Math.PI / 180f));
				this.LeftArm.pitch = (float) (-93.6472701325f * (Math.PI / 180f));
				this.LeftArm.yaw = (float) (38.6358981994f * (Math.PI / 180f));
				this.LeftArm.roll = (float) (35.8762349975f * (Math.PI / 180f));
			}
			default -> {}
		}
	}
}