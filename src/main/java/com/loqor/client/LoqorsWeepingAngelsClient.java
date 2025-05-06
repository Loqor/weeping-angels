package com.loqor.client;

import com.loqor.client.renderers.WeepingAngelRenderer;
import com.loqor.core.LWAEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class LoqorsWeepingAngelsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerEntityRenderers();
	}

	public void registerEntityRenderers() {
		EntityRendererRegistry.register(LWAEntities.WEEPING_ANGEL, WeepingAngelRenderer::new);
	}
}