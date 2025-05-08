package com.loqor.core;

import com.loqor.LoqorsWeepingAngels;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class LWADamageTypes {
    public static final RegistryKey<DamageType> ANGEL_NECK_SNAP = RegistryKey.of(RegistryKeys.DAMAGE_TYPE,
            LoqorsWeepingAngels.id("angel_neck_snap_damage_type"));

    public static DamageSource of(World world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }
}
