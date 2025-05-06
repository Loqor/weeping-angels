package com.loqor.client;

import com.loqor.core.LWAEntities;
import com.loqor.core.LWAItems;
import dev.amble.lib.datagen.lang.AmbleLanguageProvider;
import dev.amble.lib.datagen.lang.LanguageType;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class LoqorsWeepingAngelsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		generateENUSLanguage(pack);
	}

	public void generateENUSLanguage(FabricDataGenerator.Pack pack) {
		pack.addProvider(
				((output, registriesFuture) -> addEnglishTranslations(output, LanguageType.EN_US)));
	}

	public AmbleLanguageProvider addEnglishTranslations(FabricDataOutput output,
														LanguageType languageType) {
		AmbleLanguageProvider provider = new AmbleLanguageProvider(output, languageType);

		provider.addTranslation(LWAItems.ANGEL_SPAWNER_ITEM, "Angel Spawner");
		provider.addTranslation(LWAEntities.WEEPING_ANGEL.getTranslationKey(), "Weeping Angel");

		return provider;
	}


}
