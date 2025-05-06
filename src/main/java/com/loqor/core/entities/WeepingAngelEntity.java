package com.loqor.core.entities;

import com.loqor.core.angels.Angel;
import com.loqor.core.angels.AngelRegistry;
import com.mojang.serialization.Dynamic;
import dev.drtheo.scheduler.api.Scheduler;
import dev.drtheo.scheduler.api.TimeUnit;
import net.minecraft.block.WearableCarvedPumpkinBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WeepingAngelEntity extends HostileEntity {
    private static final TrackedData<Boolean> ISNTSTONE = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ACTIVE = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> ANGEL = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedDataHandler<WeepingAngelEntity.AngelPose> ANGEL_POSES = TrackedDataHandler.ofEnum(WeepingAngelEntity.AngelPose.class);
    private static final TrackedData<AngelPose> ANGEL_POSE = DataTracker.registerData(WeepingAngelEntity.class, ANGEL_POSES);
    private static final String ANGEL_KEY = "Angel";
    public static final Predicate<LivingEntity> NOT_WEARING_GAZE_DISGUISE_PREDICATE = entity -> {
        if (entity instanceof PlayerEntity playerEntity) {
            ItemStack itemStack = playerEntity.getEquippedStack(EquipmentSlot.HEAD);
            return itemStack.getItem().equals(Items.CARVED_PUMPKIN);
        } else {
            return true;
        }
    };
    public WeepingAngelEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.lookControl = new WeepingAngelEntity.AngelLookControl(this);
        this.moveControl = new WeepingAngelEntity.AngelMoveControl(this);
        this.jumpControl = new WeepingAngelEntity.AngelJumpControl(this);
        MobNavigation mobNav = (MobNavigation) this.getNavigation();
        mobNav.setCanSwim(true);
        this.experiencePoints = 0;
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setAngelPerDimension(world.toServerWorld());
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public void setAngelPerDimension(World world) {
        Stream<Angel> angelList = AngelRegistry.getInstance().toList().stream();
        if (world.getRegistryKey().equals(World.END)) {
            this.setAngel(angelList
                    .filter(angel -> angel.dimension().equals(World.END))
                    .findAny()
                    .orElse(AngelRegistry.ENDSTONE));
        } else if (world.getRegistryKey().equals(World.NETHER)) {
            this.setAngel(angelList
                    .filter(angel -> angel.dimension().equals(World.NETHER))
                    .findAny()
                    .orElse(AngelRegistry.BLACKSTONE));
        } else {
            this.setAngel(angelList
                    .filter(angel -> angel.dimension().equals(World.OVERWORLD))
                    .findAny()
                    .orElse(AngelRegistry.STONE));
        }
    }

    static {
        TrackedDataHandlerRegistry.register(ANGEL_POSES);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new WeepingAngelEntity.AngelNavigation(this, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ISNTSTONE, true);
        this.dataTracker.startTracking(ACTIVE, false);
        this.dataTracker.startTracking(ANGEL, AngelRegistry.STONE.id().toString());
        this.dataTracker.startTracking(ANGEL_POSE, AngelPose.HIDING);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString(ANGEL_KEY, this.getAngelData());
    }

    @Override
    public void onAttacking(Entity target) {
        if (target instanceof PlayerEntity player) {
            if (player.getHealth() == player.getMaxHealth()) {
                super.onAttacking(target);
                return;
            }
            int randomX = player.getBlockX() * this.getWorld().getRandom().nextBetween(-1500, 1500);
            int randomZ = player.getBlockZ() * this.getWorld().getRandom().nextBetween(-1500, 1500);
            player.teleport(randomX, player.getWorld().getChunk(ChunkSectionPos.getSectionCoord(randomX), ChunkSectionPos.getSectionCoord(randomZ))
                    .sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, randomX & 15, randomZ & 15) + 1, randomZ);
            Scheduler.get().runTaskLater(() ->
                    player.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 2.0F),
                    TimeUnit.SECONDS, 1);
        }
        super.onAttacking(target);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(ANGEL_KEY)) {
            this.setAngel(AngelRegistry.getInstance().get(Identifier.tryParse(nbt.getString(ANGEL_KEY))));
        }
    }

    public void setAngel(Angel angel) {
        this.dataTracker.set(ANGEL, angel.id().toString());
    }

    public String getAngelData() {
        return this.dataTracker.get(ANGEL);
    }

    public Angel getAngel() {
        return AngelRegistry.getInstance().get(Identifier.tryParse(this.getAngelData()));
    }

    @Override
    protected BodyControl createBodyControl() {
        return new WeepingAngelEntity.AngelBodyControl(this);
    }

    @Override
    public Brain<WeepingAngelEntity> getBrain() {
        return (Brain<WeepingAngelEntity>)super.getBrain();
    }

    @Override
    protected Brain.Profile<WeepingAngelEntity> createBrainProfile() {
        return WeepingAngelBrain.createBrainProfile();
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return WeepingAngelBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    @Override
    public boolean damage(DamageSource damageSource, float amount) {
        if (damageSource.getSource() instanceof PlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            if (stack.getItem() instanceof PickaxeItem || amount > 50000) {
                return super.damage(this.getWorld().getDamageSources().inWall(), amount);
            }
        }
        return false;
    }

    @Override
    protected void mobTick() {
        Profiler profiler = this.getWorld().getProfiler();
        profiler.push("angelBrain");
        this.getBrain().tick((ServerWorld)this.getWorld(), this);
        profiler.pop();
        WeepingAngelBrain.updateActivities(this);
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient) {
            boolean bl = this.dataTracker.get(ISNTSTONE);
            boolean bl2 = this.shouldBeNotStone();
            if (bl2 != bl) {
                if (bl2) {
                    this.playSound(SoundEvents.BLOCK_GRINDSTONE_USE, 1.0F, 1.0F);
                } else {
                    this.setAngelPose(this.getRandomAngelPose());
                    this.stopMovement();
                    this.playSound(SoundEvents.BLOCK_GRINDSTONE_USE, 1.0F, 0.1F);
                }
            }

            this.dataTracker.set(ISNTSTONE, bl2);
        }

        super.tickMovement();
    }

    public void stopMovement() {
        this.getNavigation().stop();
        this.setSidewaysSpeed(0.0F);
        this.setUpwardSpeed(0.0F);
        this.setMovementSpeed(0.0F);
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean canUsePortals() {
        return this.isntStone();
    }

    @Override
    protected boolean shouldDropLoot() {
        return true;
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(damageSource, causedByPlayer);
        Entity entity = damageSource.getAttacker();
        if (entity instanceof PlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            if (stack.getItem() instanceof PickaxeItem) {
                this.dropStack(new ItemStack(Items.LODESTONE));
            }
        }
    }

    public boolean shouldBeNotStone() {
        List<PlayerEntity> list = this.brain.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        //this.brain.forgetAll();
        boolean bl = this.isActive();
        //System.out.println(bl + " || " + list);
        if (list.isEmpty()) {
            if (bl) {
                this.deactivate();
            }

            return true;
        } else {
            boolean bl2 = false;

            for (PlayerEntity playerEntity : list) {
                if (this.canTarget(playerEntity) && !this.isTeammate(playerEntity)) {
                    bl2 = true;
                    if ((bl || !NOT_WEARING_GAZE_DISGUISE_PREDICATE.test(playerEntity))
                            && this.isEntityLookingAtMe(
                            playerEntity, 0.5, false,
                            this.getEyeY(), this.getY() + 0.5 * this.getScaleFactor(), (this.getEyeY() + this.getY()) / 2.0)) {
                        if (bl) {
                            return false;
                        }

                        if (playerEntity.squaredDistanceTo(this) < 144.0) {
                            this.activate(playerEntity);
                            return false;
                        }
                    }
                }
            }

            if (!bl2 && bl) {
                this.deactivate();
            }

            return true;
        }
    }

    public boolean isEntityLookingAtMe(LivingEntity entity, double d, boolean bl, double... checkedYs) {

        Vec3d vec3d = entity.getRotationVec(1.0F).normalize();

        for (double e : checkedYs) {
            Vec3d vec3d2 = new Vec3d(this.getX() - entity.getX(), e - entity.getEyeY(), this.getZ() - entity.getZ());
            double f = vec3d2.length();
            vec3d2 = vec3d2.normalize();
            double g = vec3d.dotProduct(vec3d2);
            if (g > 1.0 - d / (bl ? f : 1.0)
                    && this.canSee(this)) {
                return true;
            }
        }

        return false;
    }

    public boolean canSee(Entity entity) {
        if (entity.getWorld() != this.getWorld()) {
            return false;
        } else {
            Vec3d vec3d = new Vec3d(this.getX(), this.getEyeY(), this.getZ());
            Vec3d vec3d2 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
            return !(vec3d2.distanceTo(vec3d) > 128.0) && this.getWorld().raycast(new RaycastContext(vec3d, vec3d2,
                    RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this)).getType()
                    == HitResult.Type.MISS;
        }
    }

    public static boolean canSpawn(EntityType<WeepingAngelEntity> weepingAngelEntityEntityType,
                                   ServerWorldAccess serverWorldAccess, SpawnReason spawnReason, BlockPos blockPos, Random random) {
        return serverWorldAccess.getLightLevel(LightType.BLOCK, blockPos) <= 8 && canSpawnIgnoreLightLevel(weepingAngelEntityEntityType, serverWorldAccess, spawnReason, blockPos, random);
    }

    public void activate(PlayerEntity player) {
        if (!this.isActive()) {
            this.getBrain().remember(MemoryModuleType.ATTACK_TARGET, player);
            this.setAngelPose(AngelPose.MOVING);
            this.setActive(true);
        }
    }

    public void deactivate() {
        if (this.isActive()) {
            this.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            this.setActive(false);
        }
    }

    public AngelPose getRandomAngelPose() {
        return AngelPose.values()[this.getRandom().nextInt(AngelPose.values().length)];
    }

    public void setActive(boolean active) {
        this.dataTracker.set(ACTIVE, active);
    }

    public boolean isActive() {
        return this.dataTracker.get(ACTIVE);
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && this.isntStone();
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        if (this.isntStone()) {
            super.addVelocity(deltaX, deltaY, deltaZ);
        }
    }

    public boolean isntStone() {
        return this.dataTracker.get(ISNTSTONE);
    }

    public AngelPose getAngelPose() {
        return this.dataTracker.get(ANGEL_POSE);
    }

    public void setAngelPose(AngelPose pose) {
        this.dataTracker.set(ANGEL_POSE, pose);
    }

    class AngelLandPathNodeMaker extends LandPathNodeMaker {
        private static final int MAX_RANGE = 1024;

        @Override
        public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
            return super.getDefaultNodeType(world, x, y, z);
        }
    }

    class AngelBodyControl extends BodyControl {
        public AngelBodyControl(final WeepingAngelEntity angel) {
            super(angel);
        }

        @Override
        public void tick() {
            if (WeepingAngelEntity.this.isntStone()) {
                super.tick();
            }
        }
    }

    class AngelLookControl extends LookControl {
        public AngelLookControl(final WeepingAngelEntity angel) {
            super(angel);
        }

        @Override
        public void tick() {
            if (WeepingAngelEntity.this.isntStone()) {
                super.tick();
            }
        }
    }

    class AngelMoveControl extends MoveControl {
        public AngelMoveControl(final WeepingAngelEntity angel) {
            super(angel);
        }

        @Override
        public void tick() {
            if (WeepingAngelEntity.this.isntStone()) {
                super.tick();
            }
        }
    }

    class AngelNavigation extends MobNavigation {
        AngelNavigation(final WeepingAngelEntity angel, final World world) {
            super(angel, world);
        }

        @Override
        public void tick() {
            if (WeepingAngelEntity.this.isntStone()) {
                super.tick();
            }
        }

        @Override
        protected PathNodeNavigator createPathNodeNavigator(int range) {
            this.nodeMaker = WeepingAngelEntity.this.new AngelLandPathNodeMaker();
            this.nodeMaker.setCanEnterOpenDoors(true);
            return new PathNodeNavigator(this.nodeMaker, range);
        }
    }

    class AngelJumpControl extends JumpControl {
        public AngelJumpControl(final WeepingAngelEntity angel) {
            super(angel);
        }

        @Override
        public void tick() {
            if (WeepingAngelEntity.this.isntStone()) {
                super.tick();
            } else {
                WeepingAngelEntity.this.setJumping(false);
            }
        }
    }

    public enum AngelPose {
        DEFAULT,
        MOVING,
        HIDING,
        ATTACKING,
        ANGRY,
        AFRAID,
        RETREATING;
    }
}
