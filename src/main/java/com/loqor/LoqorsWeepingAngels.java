package com.loqor;

import com.loqor.core.LWAEntities;
import com.loqor.core.LWAItems;
import com.loqor.core.angels.AngelRegistry;
import com.loqor.core.entities.WeepingAngelEntity;
import com.loqor.core.util.HeartbeatUtil;
import com.loqor.core.world.LWASounds;
import com.loqor.core.world.gen.LWASpawns;
import dev.amble.lib.container.RegistryContainer;
import dev.amble.lib.register.AmbleRegistries;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoqorsWeepingAngels implements ModInitializer {
	public static final String MOD_ID = "loqors-weeping-angels";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
	public static final HeartbeatUtil INSTANCE = new HeartbeatUtil();


	@Override
	public void onInitialize() {
		LOGGER.info("Loqor's Weeping Angels mod is initializing...");

		// This registers the entity types and item classes
		RegistryContainer.register(LWAEntities.class, MOD_ID);
		RegistryContainer.register(LWAItems.class, MOD_ID);
		LWASounds.init();

		// This is for spawning the Weeping Angels in different biomes
		LWASpawns.addSpawns();

		registerEntityAttributes();

		// This is for the player heartbeat buildup
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				HeartbeatUtil.checkHeartRate(player);
			}
		});
	}


	// This is for registering the entity attributes
	public void registerEntityAttributes() {
		FabricDefaultAttributeRegistry.register(LWAEntities.WEEPING_ANGEL,
				WeepingAngelEntity.getAngelAttributes());
	}
}