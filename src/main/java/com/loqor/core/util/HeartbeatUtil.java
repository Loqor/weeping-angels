package com.loqor.core.util;

import com.loqor.core.entities.WeepingAngelEntity;
import com.loqor.core.world.LWASounds;
import dev.drtheo.scheduler.api.common.Scheduler;
import dev.drtheo.scheduler.api.TimeUnit;
import dev.drtheo.scheduler.api.common.TaskStage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeartbeatUtil {

    private static final double ANGEL_DETECTION_RADIUS = 10.0;
    private static final Map<UUID, Long> lastHeartbeat = new HashMap<>();

    public static void checkHeartRate(ServerPlayerEntity player) {
        //if (player.getHealth() >= 10) return;

        List<WeepingAngelEntity> angels = getNearbyWeepingAngels(player);
        if (angels.isEmpty()) return;

        double closestDistance = angels.stream()
                .mapToDouble(angel -> angel.getPos().distanceTo(player.getPos()))
                .min().orElse(ANGEL_DETECTION_RADIUS);

        long interval = (long) Math.max(10, (closestDistance / ANGEL_DETECTION_RADIUS) * 60);
        long currentTime = player.getServerWorld().getTime();
        UUID uuid = player.getUuid();

        if (currentTime - lastHeartbeat.getOrDefault(uuid, 0L) >= interval) {
            lastHeartbeat.put(uuid, currentTime);
            playDoubleHeartbeat(player);
        }
    }

    private static void playDoubleHeartbeat(ServerPlayerEntity player) {
        World world = player.getWorld();

        world.playSound(null, player.getBlockPos(), LWASounds.HEART_BEAT, SoundCategory.PLAYERS, 0.25f, 1.0f);

        Scheduler scheduler = Scheduler.get();
        if (scheduler != null) {
            scheduler.runTaskLater(() -> {
                world.playSound(null, player.getBlockPos(), LWASounds.HEART_BEAT, SoundCategory.PLAYERS, 0.25f, 0.95f);
            }, TaskStage.END_SERVER_TICK, TimeUnit.TICKS, 6);
        }
    }

    private static List<WeepingAngelEntity> getNearbyWeepingAngels(ServerPlayerEntity player) {
        Vec3d pos = player.getPos();
        Box range = new Box(
                pos.subtract(ANGEL_DETECTION_RADIUS, ANGEL_DETECTION_RADIUS, ANGEL_DETECTION_RADIUS),
                pos.add(ANGEL_DETECTION_RADIUS, ANGEL_DETECTION_RADIUS, ANGEL_DETECTION_RADIUS)
        );

        return player.getWorld().getEntitiesByClass(WeepingAngelEntity.class, range, angel -> true);
    }
}
