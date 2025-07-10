package com.loqor.config;

import com.loqor.LoqorsWeepingAngels;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;

public class LWAServerConfig {

    public static final String CATEGORY = "server";

    public static final ConfigClassHandler<LWAServerConfig> INSTANCE = ConfigClassHandler.createBuilder(LWAServerConfig.class)
            .id(YACLPlatform.rl(LoqorsWeepingAngels.MOD_ID, "server"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("loqors-weeping-angels-server.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @AutoGen(category = CATEGORY)
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @CustomDescription("Enable or disable the tracking of heartbeats. If disabled, heartbeats will not be played to any players (I couldn't make it a client side option :( ).")
    @SerialEntry public boolean shouldDoHeartbeatTracking = true;

    @AutoGen(category = CATEGORY)
    @IntField(min = 1)
    @CustomDescription("The spawn rate of angels in the world. Higher values mean they *might* spawn less/more. Minecraft code is a bit weird.")
    @SerialEntry public int angelSpawnRate = 5;
}
