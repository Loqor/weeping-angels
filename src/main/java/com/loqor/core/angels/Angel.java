package com.loqor.core.angels;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loqor.LoqorsWeepingAngels;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.amble.lib.api.Identifiable;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public record Angel(Identifier id, Identifier texture) implements Identifiable {
    public static final Codec<Angel> CODEC = Codecs.exceptionCatching(RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(Angel::id),
            Identifier.CODEC.fieldOf("texture").forGetter(Angel::texture))
            .apply(instance, Angel::new)));
    @Override
    public Identifier id() {
        return this.id;
    }
    @Override
    public Identifier texture() {
        return this.texture;
    }

    public static Angel fromInputStream(InputStream stream) {
        return fromJson(JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject());
    }

    public static Angel fromJson(JsonObject json) {
        AtomicReference<Angel> created = new AtomicReference<>();

        CODEC.decode(JsonOps.INSTANCE, json).get().ifLeft(var -> created.set(var.getFirst())).ifRight(err -> {
            created.set(null);
            LoqorsWeepingAngels.LOGGER.error("Error decoding datapack angel: {}", err);
        });

        return created.get();
    }
}
