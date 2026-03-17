package com.loqor.client.config;

import com.loqor.LoqorsWeepingAngels;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;

public class LWAClientConfig {

    public enum EyeAnchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        UNDER_CURSOR
    }

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

    @AutoGen(category = CATEGORY)
    @DoubleField(min = 1.0D)
    @CustomDescription("Distance in blocks for danger effects and heartbeat sounds. Effects trigger when an angel is within this range.")
    @SerialEntry public double dangerEffectDistance = 3.0D;

    @AutoGen(category = CATEGORY)
    @Boolean(formatter = Boolean.Formatter.YES_NO, colored = true)
    @CustomDescription("If enabled, the eye icon fills and shakes more as angels get closer.")
    @SerialEntry public boolean dynamicEyeIntensity = true;

    @AutoGen(category = CATEGORY)
    @EnumCycler
    @CustomDescription("Where the eye icon renders on the screen.")
    @SerialEntry public EyeAnchor eyeAnchor = EyeAnchor.BOTTOM_RIGHT;

    @AutoGen(category = CATEGORY)
    @DoubleField(min = 0.5D, max = 5.0D)
    @CustomDescription("Scale multiplier for the eye icon.")
    @SerialEntry public double eyeScale = 2.0D;
}
