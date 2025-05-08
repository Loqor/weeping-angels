package com.loqor.core.entities;

import com.loqor.core.LWADamageTypes;
import com.loqor.core.angels.Angel;
import com.loqor.core.angels.AngelRegistry;
import com.loqor.core.util.StackUtil;
import com.mojang.serialization.Dynamic;
import dev.drtheo.scheduler.api.Scheduler;
import dev.drtheo.scheduler.api.TimeUnit;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WeepingAngelEntity extends HostileEntity {
    private static final List<Item> ANGEL_DROPS = new ArrayList<>();
    private static final TrackedData<Boolean> ISNOTSTONE = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
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
        ANGEL_DROPS.add(Items.STONE);
        ANGEL_DROPS.add(Items.COBBLESTONE);
        ANGEL_DROPS.add(Items.LODESTONE);
        ANGEL_DROPS.add(Items.STONE_BRICKS);
        ANGEL_DROPS.add(Items.MOSSY_COBBLESTONE);
        ANGEL_DROPS.add(Items.DEEPSLATE);
        ANGEL_DROPS.add(Items.DEEPSLATE_BRICKS);
        ANGEL_DROPS.add(Items.DEEPSLATE_TILES);
        ANGEL_DROPS.add(Items.OAK_SAPLING); // THE ANGELS ARE FULL OF FORESTS - Loqor
        ANGEL_DROPS.add(Items.COBBLED_DEEPSLATE);
        ANGEL_DROPS.add(Items.ANDESITE);
        ANGEL_DROPS.add(Items.POLISHED_ANDESITE);
        ANGEL_DROPS.add(Items.GRANITE);
        ANGEL_DROPS.add(Items.POLISHED_GRANITE);
        ANGEL_DROPS.add(Items.DIORITE);
        ANGEL_DROPS.add(Items.POLISHED_DIORITE);
        this.lookControl = new WeepingAngelEntity.AngelLookControl(this);
        this.moveControl = new WeepingAngelEntity.AngelMoveControl(this);
        this.jumpControl = new WeepingAngelEntity.AngelJumpControl(this);
        MobNavigation mobNav = (MobNavigation) this.getNavigation();
        mobNav.setCanSwim(true);
        this.experiencePoints = 0;
    }

    public static DefaultAttributeContainer getAngelAttributes() {
        return WeepingAngelEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D).build();
    }

    @Override
    public float getMovementSpeed() {
        // Make the angels faster if the moon is BIG - Loqor
        if (this.getWorld().getMoonSize() > 0.9F) {
            return super.getMovementSpeed() * 1.25F;
        }
        return super.getMovementSpeed();
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
        this.dataTracker.startTracking(ISNOTSTONE, true);
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
        if (target.getWorld().isClient()) return;
        if (target instanceof PlayerEntity player) {
            if (player.getHealth() == player.getMaxHealth()) {
                super.onAttacking(target);
                return;
            }
            if (this.getWorld().getRandom().nextBoolean()) {
                MinecraftServer server = target.getWorld().getServer();
                if (server == null) {
                    super.onAttacking(target);
                    return;
                }
                server.execute(() -> {
                    int randomX = player.getBlockX() + this.getWorld().getRandom().nextBetween(-1500, 1500);
                    int randomZ = player.getBlockZ() + this.getWorld().getRandom().nextBetween(-1500, 1500);
                    player.teleport(randomX, player.getWorld().getChunk(ChunkSectionPos.getSectionCoord(randomX), ChunkSectionPos.getSectionCoord(randomZ))
                            .sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, randomX & 15, randomZ & 15) + 1, randomZ);
                    Scheduler.get().runTaskLater(() ->
                                    player.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 2.0F),
                            TimeUnit.SECONDS, 2);
                });
            } else {
                player.damage(LWADamageTypes.of(target.getWorld(),
                        LWADamageTypes.ANGEL_NECK_SNAP), Float.MAX_VALUE);
                player.playSound(SoundEvents.ENTITY_PLAYER_BIG_FALL, 1.0F, 1.5F);
            }
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
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.BLOCK_GRINDSTONE_USE, 0.2f, 1f);
    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient) {
            if (this.isAiDisabled() ||
                    !this.getAngelPose().equals(AngelPose.MOVING))
                this.stopMovement();
            boolean bl = this.dataTracker.get(ISNOTSTONE);
            boolean bl2 = this.shouldBeNotStone();
            if (bl2 != bl) {
                if (bl2) {
                    this.setAngelPose(AngelPose.MOVING);
                    this.playSound(SoundEvents.BLOCK_STONE_BREAK, 0.5f, 1.0F);
                } else {
                    this.setAngelPose(this.getRandomAngelPose());
                    this.stopMovement();
                    this.playSound(SoundEvents.BLOCK_STONE_PLACE, 0.5f, 0.1F);
                }
            }

            this.dataTracker.set(ISNOTSTONE, bl2);
        }

        super.tickMovement();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_STONE_HIT;
    }

    public void stopMovement() {
        this.getNavigation().stop();
        this.setSidewaysSpeed(0.0F);
        this.setUpwardSpeed(0.0F);
        this.setMovementSpeed(0.0F);
    }

    @Override
    public boolean canUsePortals() {
        return this.isNotStone();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        ItemStack stack = new ItemStack(ANGEL_DROPS.get(this.getWorld().getRandom().nextBetween(0, ANGEL_DROPS.size() - 1)));
        stack.setCount(this.getWorld().getRandom().nextBetween(1, 4));
        StackUtil.spawn(this.getWorld(), this.getBlockPos(), stack);
    }

    public boolean shouldBeNotStone() {
        /*List<PlayerEntity> list = this.brain.getOptionalRegisteredMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());*/
        TargetPredicate targetPredicate = TargetPredicate.createAttackable().ignoreVisibility().setBaseMaxDistance(144.0D);
        List<PlayerEntity> list = this.getWorld().getPlayers(targetPredicate, this, this.getBoundingBox().expand(144.0D));
        boolean bl = this.isActive();
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
                        } else {
                            this.deactivate();
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
        return super.isPushable() && this.isNotStone();
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        if (this.isNotStone()) {
            super.addVelocity(deltaX, deltaY, deltaZ);
        }
    }

    public boolean isNotStone() {
        return this.dataTracker.get(ISNOTSTONE);
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
            if (WeepingAngelEntity.this.isNotStone()) {
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
            if (WeepingAngelEntity.this.isNotStone()) {
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
            if (WeepingAngelEntity.this.isNotStone()) {
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
            if (WeepingAngelEntity.this.isNotStone()) {
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
            if (WeepingAngelEntity.this.isNotStone()) {
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
