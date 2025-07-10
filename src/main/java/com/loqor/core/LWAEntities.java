package com.loqor.core;

import com.loqor.core.entities.WeepingAngelEntity;
import dev.amble.lib.container.impl.EntityContainer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.*;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.world.Heightmap;

public class LWAEntities implements EntityContainer {
    public static final EntityType<WeepingAngelEntity> WEEPING_ANGEL = FabricEntityTypeBuilder.Mob.createMob()
            .entityFactory(WeepingAngelEntity::new).spawnGroup(SpawnGroup.MONSTER)
            .dimensions(EntityDimensions.fixed(0.6F, 1.99F)).spawnRestriction(SpawnRestriction.Location.ON_GROUND,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WeepingAngelEntity::canSpawnInDark).fireImmune().trackRangeChunks(16).build();
}
