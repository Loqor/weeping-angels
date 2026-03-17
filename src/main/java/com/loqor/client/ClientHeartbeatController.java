package com.loqor.client;

import com.loqor.client.config.LWAClientConfig;
import com.loqor.core.entities.WeepingAngelEntity;
import com.loqor.core.world.LWASounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ClientHeartbeatController {
    private long lastHeartbeatTick = Long.MIN_VALUE;
    private int secondBeatDelayTicks = -1;

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            secondBeatDelayTicks = -1;
            return;
        }

        double effectDistance = Math.max(1.0D, LWAClientConfig.INSTANCE.instance().dangerEffectDistance);
        PlayerEntity player = client.player;

        if (secondBeatDelayTicks == 0) {
            player.playSound(LWASounds.HEART_BEAT, 0.25f, 0.95f);
            secondBeatDelayTicks = -1;
        } else if (secondBeatDelayTicks > 0) {
            secondBeatDelayTicks--;
        }

        double closestDistance = getClosestAngelDistance(player, effectDistance);
        if (closestDistance > effectDistance) {
            return;
        }

        long currentTick = client.world.getTime();
        long interval = Math.max(8L, Math.round((closestDistance / effectDistance) * 40.0D));
        if (currentTick - lastHeartbeatTick < interval) {
            return;
        }

        lastHeartbeatTick = currentTick;
        player.playSound(LWASounds.HEART_BEAT, 0.25f, 1.0f);
        secondBeatDelayTicks = 6;
    }

    private static double getClosestAngelDistance(PlayerEntity player, double radius) {
        Vec3d pos = player.getPos();
        Box range = new Box(pos.subtract(radius, radius, radius), pos.add(radius, radius, radius));
        List<WeepingAngelEntity> angels = player.getWorld().getEntitiesByClass(WeepingAngelEntity.class, range, angel -> true);

        return angels.stream()
                .mapToDouble(angel -> angel.getPos().distanceTo(pos))
                .min()
                .orElse(Double.MAX_VALUE);
    }
}

