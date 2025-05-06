package com.loqor;

import com.loqor.core.LWAEntities;
import com.loqor.core.LWAItems;
import com.loqor.core.entities.WeepingAngelEntity;
import dev.amble.lib.container.RegistryContainer;
import dev.amble.lib.register.AmbleRegistries;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoqorsWeepingAngels implements ModInitializer {
	public static final String MOD_ID = "loqors-weeping-angels";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Loqor's Weeping Angels mod is initializing...");
		RegistryContainer.register(LWAEntities.class, MOD_ID);
		RegistryContainer.register(LWAItems.class, MOD_ID);

		registerEntityAttributes();
	}

	public void registerEntityAttributes() {
		FabricDefaultAttributeRegistry.register(LWAEntities.WEEPING_ANGEL,
				WeepingAngelEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
						.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D));
	}
}