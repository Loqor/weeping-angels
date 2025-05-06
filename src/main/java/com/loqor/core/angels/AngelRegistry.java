package com.loqor.core.angels;

import com.loqor.LoqorsWeepingAngels;
import dev.amble.lib.register.datapack.SimpleDatapackRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class AngelRegistry extends SimpleDatapackRegistry<Angel> {
    private static final AngelRegistry instance = new AngelRegistry();
    public AngelRegistry() {
        super(Angel::fromInputStream, Angel.CODEC, "entities/angel/variants", "entities/angel/variants", true,
                LoqorsWeepingAngels.MOD_ID);
    }

    public static Angel STONE;
    public static Angel BLACKSTONE;
    public static Angel ENDSTONE;

    @Override
    protected void defaults() {
        STONE = register(new Angel(LoqorsWeepingAngels.id("angel/stone"), LoqorsWeepingAngels.id("textures/entity/angel/stone_angel.png")));
        BLACKSTONE = register(new Angel(LoqorsWeepingAngels.id("angel/blackstone"), LoqorsWeepingAngels.id("textures/entity/angel/blackstone_angel.png")));
        ENDSTONE = register(new Angel(LoqorsWeepingAngels.id("angel/endstone"), LoqorsWeepingAngels.id("textures/entity/angel/endstone_angel.png")));
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
