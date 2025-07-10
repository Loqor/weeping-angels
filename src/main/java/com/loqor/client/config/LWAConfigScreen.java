package com.loqor.client.config;

import com.loqor.config.LWAServerConfig;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LWAConfigScreen {

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.create(
                LWAServerConfig.INSTANCE, (defaults, config, builder) -> builder
                        .title(Text.translatable("text.loqors-weeping-angels.config.title"))
                        .category(ConfigCategory.createBuilder()
                                .name(Text.translatable("text.loqors-weeping-angels.config.categories"))
                                .option(ButtonOption.createBuilder()
                                        .name(Text.translatable("category.loqors-weeping-angels.config.client"))
                                        .action((yaclScreen, buttonOption) -> MinecraftClient.getInstance().setScreen(
                                                LWAClientConfig.INSTANCE.generateGui().generateScreen(yaclScreen))
                                        ).build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.translatable("category.loqors-weeping-angels.config.server"))
                                        .action((yaclScreen, buttonOption) -> MinecraftClient.getInstance().setScreen(
                                                LWAServerConfig.INSTANCE.generateGui().generateScreen(yaclScreen))
                                        ).build())
                                .build())
        ).generateScreen(parent);
    }
}
