package com.loqor.core.angels;

import com.loqor.LoqorsWeepingAngels;
import dev.amble.lib.register.datapack.SimpleDatapackRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class AngelRegistry extends SimpleDatapackRegistry<Angel> {
    private static final AngelRegistry instance = new AngelRegistry();
    public static final Identifier TEXTURE = LoqorsWeepingAngels.id("textures/entity/angel/stone_angel.png");
    public AngelRegistry() {
        super(Angel::fromInputStream, Angel.CODEC, "entities/angel/variants", "entities/angel/variants", true,
                LoqorsWeepingAngels.MOD_ID);
    }

    public static Angel STONE;

    @Override
    protected void defaults() {
        STONE = register(new Angel(LoqorsWeepingAngels.id("angel/stone"), TEXTURE));
    }

    @Override
    public void onCommonInit() {
        super.onCommonInit();
        this.defaults();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
    }

    @Override
    public Angel fallback() {
        return STONE;
    }

    public static AngelRegistry getInstance() {
        return instance;
    }
}
