package com.loqor.core.entities;

import com.loqor.LoqorsWeepingAngels;
import com.loqor.core.LWADamageTypes;
import com.loqor.core.angels.Angel;
import com.loqor.core.angels.AngelRegistry;
import com.loqor.core.util.StackUtil;
import com.loqor.core.world.LWASounds;
import com.mojang.serialization.Dynamic;
import dev.amble.lib.util.ServerLifecycleHooks;
import dev.drtheo.scheduler.api.Scheduler;
import dev.drtheo.scheduler.api.TimeUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WeepingAngelEntity extends HostileEntity {
    private static final List<Item> ANGEL_DROPS = new ArrayList<>();
    private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER = difficulty -> difficulty == Difficulty.HARD;
    private boolean canBreakDoors;
    private static final TrackedData<Boolean> ISNOTSTONE = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ACTIVE = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> ANGEL = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedDataHandler<WeepingAngelEntity.AngelPose> ANGEL_POSES = TrackedDataHandler.ofEnum(WeepingAngelEntity.AngelPose.class);
    private static final TrackedData<AngelPose> ANGEL_POSE = DataTracker.registerData(WeepingAngelEntity.class, ANGEL_POSES);
    private static final TrackedData<Integer> BLOODLUST = DataTracker.registerData(WeepingAngelEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final String ANGEL_KEY = "Angel";
    public static final Predicate<LivingEntity> NOT_WEARING_GAZE_DISGUISE_PREDICATE = entity -> {
        if (entity instanceof PlayerEntity playerEntity) {
            ItemStack itemStack = playerEntity.getEquippedStack(EquipmentSlot.HEAD);
            return itemStack.getItem().equals(Items.CARVED_PUMPKIN);
        } else {
            return true;
        }
    };
    private static final Set<Block> LIGHT_SOURCES = Set.of(
            Blocks.TORCH,
            Blocks.WALL_TORCH,
            Blocks.LANTERN,
            Blocks.SOUL_TORCH,
            Blocks.SOUL_LANTERN,
            Blocks.GLOWSTONE,
            Blocks.REDSTONE_LAMP,
            Blocks.SEA_LANTERN,
            Blocks.END_ROD,
            Blocks.SHROOMLIGHT
    );
    private final Map<BlockPos, Integer> flickeringLights = new HashMap<>();
    private int extinguishCooldown = 100;

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
        mobNav.setRangeMultiplier(4.0f);
        mobNav.setCanWalkOverFences(true);
        mobNav.setCanEnterOpenDoors(true);
        mobNav.setCanPathThroughDoors(true);
        this.experiencePoints = 0;
    }

    public static DefaultAttributeContainer getAngelAttributes() {
        return WeepingAngelEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D).build();
    }

    @Override
    public float getMovementSpeed() {
        float base = super.getMovementSpeed();
        int bloodlust = getBloodlust();
        if (bloodlust > 0) {
            base *= 1.0F + (bloodlust / 100.0F) * 0.5F; // up to 50% speed boost
        }
        if (this.getWorld().getMoonSize() > 0.9F) {
            base *= 1.25F;
        }
        return base;
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
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) {
            extinguishNearbyLights();
            processFlickeringLights();
            decayBloodlust();
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ISNOTSTONE, true);
        this.dataTracker.startTracking(ACTIVE, false);
        this.dataTracker.startTracking(ANGEL, AngelRegistry.STONE.id().toString());
        this.dataTracker.startTracking(ANGEL_POSE, AngelPose.HIDING);
        this.dataTracker.startTracking(BLOODLUST, 0);
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
                        LWADamageTypes.ANGEL_NECK_SNAP), Math.max(20.0F, Float.MAX_VALUE * (getBloodlust() / 100.0F)));
                this.playSound(LWASounds.NECK_SNAP, 1.0F, 1F);
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

    public int getBloodlust() {
        return this.dataTracker.get(BLOODLUST);
    }

    public void addBloodlust(int amount) {
        int current = getBloodlust();
        int newValue = Math.min(100, Math.max(0, current + amount));
        this.dataTracker.set(BLOODLUST, newValue);
    }

    public void decayBloodlust() {
        int current = getBloodlust();
        if (current > 0) {
            this.dataTracker.set(BLOODLUST, current - 1);
        }
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
                    this.stopMovement();
                    this.playSound(SoundEvents.BLOCK_STONE_PLACE, 0.5f, 0.1F);
                    this.setVelocity(0, 0, 0);
                    this.setAngelPose(this.getRandomAngelPose());

                    // Play angry sound if angry enough and just became stone (player stopped looking)
                    if (this.isAngryEnough()) {
                        this.playSound(SoundEvents.ENTITY_WITHER_SPAWN, 1.0F, 0.1F);
                    }

                    // Add 1 point to bloodlust every 20 ticks if the entity is stoned (lol) - Loqor
                    if (ServerLifecycleHooks.get().getTicks() % 20 == 0) {
                        this.addBloodlust(1);
                    }
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

    @Override
    public boolean isCollidable() {
        return !this.isNotStone();
    }

    private void extinguishNearbyLights() {
        Box area = new Box(getBlockPos()).expand(5);
        BlockPos min = new BlockPos((int) area.minX, (int) area.minY, (int) area.minZ);
        BlockPos max = new BlockPos((int) area.maxX, (int) area.maxY, (int) area.maxZ);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            Block block = this.getWorld().getBlockState(pos).getBlock();
            if (LIGHT_SOURCES.contains(block) && !flickeringLights.containsKey(pos)) {

                flickeringLights.put(pos.toImmutable(), 40);
                this.getWorld().playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 1.0F);
                break;
            }
        }

    }

    private void processFlickeringLights() {
        Iterator<Map.Entry<BlockPos, Integer>> iterator = flickeringLights.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            BlockPos pos = entry.getKey();
            int ticksLeft = entry.getValue();

            if (ticksLeft <= 0) {
                this.getWorld().breakBlock(pos, true);
                iterator.remove();
            } else {
                // Flicker effect: spawn smoke every 5 ticks
                if (ticksLeft % 5 == 0 && this.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(
                            ParticleTypes.SMOKE,
                            pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                            2, 0.1, 0.1, 0.1, 0.01
                    );
                }

                entry.setValue(ticksLeft - 1);
            }
        }
    }

    public boolean shouldBeNotStone() {
        TargetPredicate targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(500.0);
        List<PlayerEntity> players = this.getWorld().getTargets(PlayerEntity.class,
                targetPredicate, this, this.getBoundingBox().expand(1000.0));
        List<WeepingAngelEntity> angels = this.getWorld().getTargets(WeepingAngelEntity.class,
                targetPredicate, this, this.getBoundingBox().expand(1000.0));
        boolean isActive = this.isActive();

        if (players.isEmpty() && angels.isEmpty()) {
            if (isActive) {
                this.deactivate();
            }
            return true;
        }

        for (WeepingAngelEntity angel : angels) {
            if (this.canTarget(angel) && (!this.getAngelPose().equals(AngelPose.HIDING) ||
                    !angel.getAngelPose().equals(AngelPose.HIDING)) && (!this.getAngelPose().equals(AngelPose.RETREATING) || !angel.getAngelPose().equals(AngelPose.RETREATING)) && !this.isTeammate(angel) &&
                    angel.isEntityLookingAtMe(this, 1, false, this.getEyeY(), this.getY() + 0.5 * this.getScaleFactor(), (this.getEyeY() + this.getY()) / 2.0) &&
                    this.isEntityLookingAtMe(angel, 1, false, angel.getEyeY(), angel.getY() + 0.5 * angel.getScaleFactor(), (angel.getEyeY() + angel.getY()) / 2.0)) {
                if (this.isActive()) {
                    if (this.getRandom().nextBoolean())
                        this.playSound(SoundEvents.ENTITY_GHAST_SCREAM, 1, 2.0f);
                    else
                        angel.playSound(SoundEvents.ENTITY_GHAST_SCREAM, 1, 2.0f);
                }
                this.deactivate();
                angel.deactivate();

                return false;
            }
        }

        for (LivingEntity entity : players) {
            if (this.canTarget(entity) && !this.isTeammate(entity)) {

                BlockPos entityPos = entity.getBlockPos();
                int lightLevel = this.getWorld().getLightLevel(entityPos);

                boolean tooDarkToSee = lightLevel < 2;

                if (tooDarkToSee) {
                    this.brain.remember(MemoryModuleType.ATTACK_TARGET, entity);
                    this.deactivate();
                    return true;
                }

                if ((isActive || !NOT_WEARING_GAZE_DISGUISE_PREDICATE.test(entity)) &&
                        this.isEntityLookingAtMe(entity, 1, false, this.getEyeY(), this.getY() + 0.5 * this.getScaleFactor(), (this.getEyeY() + this.getY()) / 2.0)) {

                    if (isActive) {
                        return false;
                    }

                    if (entity.squaredDistanceTo(this) < 500.0) {
                        if (entity instanceof PlayerEntity player) {
                            this.activate(player);
                        }
                        return false;
                    } else {
                        this.brain.remember(MemoryModuleType.ATTACK_TARGET, entity);
                        this.deactivate();
                    }
                }
            }
        }

        if (isActive) {
            this.deactivate();
        }

        return true;
    }



    public boolean isEntityLookingAtMe(LivingEntity entity, double d, boolean bl, double... checkedYs) {
        Vec3d lookVec = entity.getRotationVec(1.0F).normalize();

        for (double e : checkedYs) {
            Vec3d toAngel = new Vec3d(this.getX() - entity.getX(), e - entity.getEyeY(), this.getZ() - entity.getZ());
            double distance = toAngel.length();
            toAngel = toAngel.normalize();
            double dot = lookVec.dotProduct(toAngel);

            // Use a very tight threshold, making it nearly impossible to "not look" at the angel
            // (e.g., require the player to look away by more than 179.9 degrees)
            if (dot > Math.cos(Math.toRadians(90f)) && this.canSee(this)) {
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

    @Override
    public double squaredAttackRange(LivingEntity target) {
        return this.getWidth() * 2.0F * this.getWidth() * 2.0F + target.getWidth();
    }

    public void deactivate() {
        if (this.isActive()) {
            this.setActive(false);
        }
    }

    public AngelPose getRandomAngelPose() {
        // 70% chance to return HIDING, 30% to return a random other pose - Loqor
        if (this.getRandom().nextFloat() < 0.7f) {
            return AngelPose.HIDING;
        }
        AngelPose[] poses = AngelPose.values();
        AngelPose pose;
        do {
            pose = poses[this.getRandom().nextInt(poses.length)];
        } while (pose == AngelPose.HIDING && poses.length > 1);
        return pose;
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

    public boolean isAngryEnough() {
        return getBloodlust() >= 70;
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

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new WeepingAngelEntity.BreakDoorGoal(this));
    }

    class BreakDoorGoal extends net.minecraft.entity.ai.goal.BreakDoorGoal {
        public BreakDoorGoal(MobEntity mobEntity) {
            super(mobEntity, 6, WeepingAngelEntity.DOOR_BREAK_DIFFICULTY_CHECKER);
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean shouldContinue() {
            return WeepingAngelEntity.this.isNotStone() && super.shouldContinue();
        }

        @Override
        public void tick() {
            if (WeepingAngelEntity.this.isNotStone()) super.tick();

        }

        @Override
        public boolean canStart() {
            return WeepingAngelEntity.this.isNotStone() &&
                    WeepingAngelEntity.this.random.nextInt(toGoalTicks(10)) == 0 && super.canStart();
        }

        @Override
        public void start() {
            super.start();
            this.mob.setDespawnCounter(0);
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
