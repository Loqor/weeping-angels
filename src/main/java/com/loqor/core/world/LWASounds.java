package com.loqor.core.world;

import java.util.List;

import com.loqor.LoqorsWeepingAngels;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;


public class LWASounds {

    public static final SoundEvent HEART_BEAT = register("heart-beat");

    public static void init() {

    }
    private static SoundEvent register(String name) {
        return register(LoqorsWeepingAngels.id(name));
    }
    private static SoundEvent register(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    public static List<SoundEvent> getSounds(String modid) {
        return Registries.SOUND_EVENT.stream().filter(sound -> sound.getId().getNamespace().equals(modid)).toList();
    }
}

