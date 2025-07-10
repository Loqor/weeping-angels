package com.loqor.client.config;

import com.loqor.LoqorsWeepingAngels;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;

public class LWAClientConfig {

    public static final String CATEGORY = "client";

    public static final ConfigClassHandler<LWAClientConfig> INSTANCE = ConfigClassHandler.createBuilder(LWAClientConfig.class)
            .id(YACLPlatform.rl(LoqorsWeepingAngels.MOD_ID, "client"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("loqors-weeping-angels-client.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @AutoGen(category = CATEGORY)
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @CustomDescription("Enable or disable the screen effects (blinking and red vignette) for Loqor's Weeping Angels.")
    @SerialEntry public boolean doScreenEffects = true;
}
