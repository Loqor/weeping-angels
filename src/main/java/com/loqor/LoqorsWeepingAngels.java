package com.loqor;

import com.loqor.core.LWAEntities;
import com.loqor.core.entities.WeepingAngelEntity;
import dev.amble.lib.container.RegistryContainer;
import dev.amble.lib.register.AmbleRegistries;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoqorsWeepingAngels implements ModInitializer {
	public static final String MOD_ID = "loqors-weeping-angels";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RegistryContainer.register(LWAEntities.class, MOD_ID);

		registerEntityAttributes();
	}

	public void registerEntityAttributes() {
		FabricDefaultAttributeRegistry.register(LWAEntities.WEEPING_ANGEL,
				WeepingAngelEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
						.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D));
	}
}