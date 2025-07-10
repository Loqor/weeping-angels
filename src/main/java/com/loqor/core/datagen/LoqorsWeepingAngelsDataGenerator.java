package com.loqor.core.datagen;

import com.loqor.core.LWADamageTypes;
import com.loqor.core.LWAEntities;
import com.loqor.core.LWAItems;
import dev.amble.lib.datagen.lang.AmbleLanguageProvider;
import dev.amble.lib.datagen.lang.LanguageType;
import dev.amble.lib.datagen.sound.AmbleSoundProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class LoqorsWeepingAngelsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		generateENUSLanguage(pack);
		generateSoundData(pack);
	}

	public void generateENUSLanguage(FabricDataGenerator.Pack pack) {
		pack.addProvider(
				((output, registriesFuture) -> addEnglishTranslations(output, LanguageType.EN_US)));
	}

	public void generateSoundData(FabricDataGenerator.Pack pack) {
		pack.addProvider((((output, registriesFuture) -> new AmbleSoundProvider(output))));
	}

	public AmbleLanguageProvider addEnglishTranslations(FabricDataOutput output,
														LanguageType languageType) {
		AmbleLanguageProvider provider = new AmbleLanguageProvider(output, languageType);

		provider.addTranslation(LWAItems.ANGEL_SPAWNER_ITEM, "Angel Spawner");
		provider.addTranslation(LWAEntities.WEEPING_ANGEL.getTranslationKey(), "Weeping Angel");
		provider.addTranslation("death.attack.angel_neck_snap_damage_type.player", "%1$s had their neck snapped by a Weeping Angel!");

		// YACL Config Translations
		provider.addTranslation("text.loqors-weeping-angels.config.title", "Loqor's Weeping Angels Options");
		provider.addTranslation("text.loqors-weeping-angels.config.categories", "Categories");
		provider.addTranslation("category.loqors-weeping-angels.config.client", "Client Options");
		provider.addTranslation("category.loqors-weeping-angels.config.server", "Server Options");
		provider.addTranslation("yacl3.config.loqors-weeping-angels:client.category.client", "Client Options");
		provider.addTranslation("yacl3.config.loqors-weeping-angels:server.category.server", "Server Options");
		provider.addTranslation("yacl3.config.loqors-weeping-angels:client.doScreenEffects", "Do Screen Effects");
		provider.addTranslation("yacl3.config.loqors-weeping-angels:server.shouldDoHeartbeatTracking", "Do Heartbeat Tracking");
		provider.addTranslation("yacl3.config.loqors-weeping-angels:server.angelSpawnRate", "Angel Spawn Rate");
		return provider;
	}


}
