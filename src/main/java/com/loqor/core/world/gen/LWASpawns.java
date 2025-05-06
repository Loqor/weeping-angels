package com.loqor.core.world.gen;

import com.loqor.core.LWAEntities;
import com.loqor.core.entities.WeepingAngelEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public class LWASpawns {
    public static void addSpawns() {
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(
                        BiomeKeys.NETHER_WASTES,
                        BiomeKeys.BASALT_DELTAS,
                        BiomeKeys.SOUL_SAND_VALLEY),
                SpawnGroup.MONSTER,
                LWAEntities.WEEPING_ANGEL,
                5,
                1,
                3);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(
                        BiomeKeys.PLAINS,
                        BiomeKeys.SWAMP,
                        BiomeKeys.MANGROVE_SWAMP,
                        BiomeKeys.FOREST,
                        BiomeKeys.FLOWER_FOREST,
                        BiomeKeys.BIRCH_FOREST,
                        BiomeKeys.DARK_FOREST,
                        BiomeKeys.OLD_GROWTH_BIRCH_FOREST,
                        BiomeKeys.OLD_GROWTH_PINE_TAIGA,
                        BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA,
                        BiomeKeys.TAIGA,
                        BiomeKeys.SNOWY_TAIGA,
                        BiomeKeys.WINDSWEPT_FOREST,
                        BiomeKeys.JUNGLE,
                        BiomeKeys.SPARSE_JUNGLE,
                        BiomeKeys.BAMBOO_JUNGLE,
                        BiomeKeys.CHERRY_GROVE,
                        BiomeKeys.SNOWY_BEACH,
                        BiomeKeys.STONY_SHORE,
                        BiomeKeys.DRIPSTONE_CAVES,
                        BiomeKeys.LUSH_CAVES,
                        BiomeKeys.DEEP_DARK),
                SpawnGroup.MONSTER,
                LWAEntities.WEEPING_ANGEL,
                10,
                2,
                4);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(
                        BiomeKeys.END_BARRENS,
                        BiomeKeys.END_HIGHLANDS,
                        BiomeKeys.END_MIDLANDS,
                        BiomeKeys.THE_END),
                SpawnGroup.MONSTER,
                LWAEntities.WEEPING_ANGEL,
                1,
                1,
                1);
    }
}
