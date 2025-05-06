package com.loqor.core;

import com.loqor.core.items.AngelSpawnerItem;
import dev.amble.lib.container.impl.ItemContainer;
import dev.amble.lib.datagen.util.NoEnglish;
import dev.amble.lib.item.AItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;

public class LWAItems extends ItemContainer {

    @NoEnglish
    public static Item ANGEL_SPAWNER_ITEM = new AngelSpawnerItem(LWAEntities.WEEPING_ANGEL, new AItemSettings().group(ItemGroups.SPAWN_EGGS));

}
