package com.loqor.core.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.loqor.core.entities.brain.tasks.MoveToTargetTask;
import com.loqor.core.entities.brain.tasks.UpdateLookControlTask;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.Optional;

public class WeepingAngelBrain {
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super WeepingAngelEntity>>> SENSORS = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS
    );
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
            MemoryModuleType.MOBS,
            MemoryModuleType.VISIBLE_MOBS,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN
    );

    static void addCoreTasks(Brain<WeepingAngelEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8F) {
            private boolean shouldRun(ServerWorld serverWorld, WeepingAngelEntity angelEntity) {
                return angelEntity.isntStone() && super.shouldRun(serverWorld, angelEntity);
            }
        }, new UpdateLookControlTask(45, 90), new MoveToTargetTask()));
    }

    static void addIdleTasks(Brain<WeepingAngelEntity> brain) {
        brain.setTaskList(
                Activity.IDLE,
                10,
                ImmutableList.of(
                        UpdateAttackTargetTask.create(WeepingAngelEntity::isRemoved,  e ->
                                        ((Optional<? extends LivingEntity>) brain.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER))
                        ),
                        LookAtMobWithIntervalTask.follow(8.0F, UniformIntProvider.create(30, 60)),
                        new RandomTask<>(ImmutableList.of(
                                Pair.of(StrollTask.create(0.3F), 2),
                                Pair.of(GoTowardsLookTargetTask.create(0.3F, 3), 2), // Ensure GoToLookTargetTask is defined and imported
                                Pair.of(new WaitTask(30, 60), 1)
                        ))
                )
        );
    }

    static void addFightTasks(Brain<WeepingAngelEntity> brain) {
        brain.setTaskList(
                Activity.FIGHT,
                10,
                ImmutableList.of(RangedApproachTask.create(1.0F), MeleeAttackTask.create(40), ForgetAttackTargetTask.create()),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    public static Brain.Profile<WeepingAngelEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    public static Brain<WeepingAngelEntity> create(Brain<WeepingAngelEntity> brain) {
        addCoreTasks(brain);
        addIdleTasks(brain);
        addFightTasks(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    public static void updateActivities(WeepingAngelEntity angel) {
        if (!angel.isntStone()) {
            angel.getBrain().resetPossibleActivities();
        } else {
            angel.getBrain().resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        }
    }
}
