package com.loqor.client;

import com.loqor.client.renderers.WeepingAngelRenderer;
import com.loqor.core.LWAEntities;
import com.loqor.core.angels.AngelRegistry;
import dev.amble.lib.register.AmbleRegistries;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class LoqorsWeepingAngelsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AmbleRegistries.getInstance().registerAll(
				AngelRegistry.getInstance()
		);
		registerEntityRenderers();
	}

	public void registerEntityRenderers() {
		EntityRendererRegistry.register(LWAEntities.WEEPING_ANGEL, WeepingAngelRenderer::new);
	}
}