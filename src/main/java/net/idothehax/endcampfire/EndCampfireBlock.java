package net.idothehax.endcampfire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class EndCampfireBlock extends BlockWithEntity implements Waterloggable {
    public static final MapCodec<EndCampfireBlock> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    Codec.BOOL.fieldOf("spawn_particles")
                            .forGetter((block) -> block.emitsParticles),
                    Codec.intRange(0, 1000).fieldOf("fire_damage")
                            .forGetter((block) -> block.fireDamage), createSettingsCodec())
                    .apply(instance, EndCampfireBlock::new));

    public static final BooleanProperty LIT;
    public static final BooleanProperty SIGNAL_FIRE;
    public static final BooleanProperty WATERLOGGED;
    public static final EnumProperty<Direction> FACING;
    private static final VoxelShape SHAPE;
    private static final VoxelShape SMOKEY_SHAPE;
    private final boolean emitsParticles;
    private final int fireDamage;

    public MapCodec<EndCampfireBlock> getCodec() {
        return CODEC;
    }

    public EndCampfireBlock(boolean emitsParticles, int fireDamage, AbstractBlock.Settings settings) {
        super(settings);

        this.emitsParticles = emitsParticles;
        this.fireDamage = fireDamage;
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, true)).with(SIGNAL_FIRE, false)).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));

    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof EndCampfireBlockEntity endCampfireBlockEntity) {
            ItemStack itemStack = player.getStackInHand(hand);
            if (world.getRecipeManager().getPropertySet(RecipePropertySet.CAMPFIRE_INPUT).canUse(itemStack)) {
                if (world instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld)world;
                    if (endCampfireBlockEntity.addItem(serverWorld, player, itemStack)) {
                        player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
                        return ActionResult.SUCCESS_SERVER;
                    }
                }

                return ActionResult.CONSUME;
            }
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        if (!world.isClient && state.get(LIT) && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            // Apply slowness effect to simulate freezing
            livingEntity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    100, // Duration: 5 seconds (20 ticks per second)
                    3,   // Amplifier: Slowness IV
                    false,
                    true
            ));
            entity.serverDamage(world.getDamageSources().campfire(), (float)this.fireDamage);
        }

        super.onEntityCollision(state, world, pos, entity, handler);
    }

    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if ((Boolean)state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction == Direction.DOWN ? (BlockState)state.with(SIGNAL_FIRE, this.isSignalFireBaseBlock(neighborState)) : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    private boolean isSignalFireBaseBlock(BlockState state) {
        return state.isOf(Blocks.HAY_BLOCK);
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if ((Boolean)state.get(LIT)) {
            if (random.nextInt(10) == 0) {
                world.playSoundClient((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            if (this.emitsParticles && random.nextInt(5) == 0) {
                for(int i = 0; i < random.nextInt(1) + 1; ++i) {
                    world.addParticleClient(ParticleTypes.LAVA, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, (double)(random.nextFloat() / 2.0F), 5.0E-5, (double)(random.nextFloat() / 2.0F));
                }
            }

        }
    }

    public static void extinguish(@Nullable Entity entity, WorldAccess world, BlockPos pos, BlockState state) {
        if (world.isClient()) {
            for(int i = 0; i < 20; ++i) {
                spawnSmokeParticle((World)world, pos, (Boolean)state.get(SIGNAL_FIRE), true);
            }
        }

        world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
    }

    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!(Boolean)state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            boolean bl = (Boolean)state.get(LIT);
            if (bl) {
                if (!world.isClient()) {
                    world.playSound((Entity)null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                extinguish((Entity)null, world, pos, state);
            }

            world.setBlockState(pos, (BlockState)((BlockState)state.with(WATERLOGGED, true)).with(LIT, false), 3);
            world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            return true;
        } else {
            return false;
        }
    }

    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerWorld serverWorld) {
            if (projectile.isOnFire() && projectile.canModifyAt(serverWorld, blockPos) && !(Boolean)state.get(LIT) && !(Boolean)state.get(WATERLOGGED)) {
                world.setBlockState(blockPos, (BlockState)state.with(Properties.LIT, true), 11);
            }
        }

    }

    public static void spawnSmokeParticle(World world, BlockPos pos, boolean isSignal, boolean lotsOfSmoke) {
        Random random = world.getRandom();
        SimpleParticleType simpleParticleType = isSignal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        world.addImportantParticleClient(simpleParticleType, true, (double)pos.getX() + (double)0.5F + random.nextDouble() / (double)3.0F * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + random.nextDouble() + random.nextDouble(), (double)pos.getZ() + (double)0.5F + random.nextDouble() / (double)3.0F * (double)(random.nextBoolean() ? 1 : -1), (double)0.0F, 0.07, (double)0.0F);
        if (lotsOfSmoke) {
            world.addParticleClient(ParticleTypes.SMOKE, (double)pos.getX() + (double)0.5F + random.nextDouble() / (double)4.0F * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + 0.4, (double)pos.getZ() + (double)0.5F + random.nextDouble() / (double)4.0F * (double)(random.nextBoolean() ? 1 : -1), (double)0.0F, 0.005, (double)0.0F);
        }

    }

    public static boolean isLitCampfireInRange(World world, BlockPos pos) {
        for(int i = 1; i <= 5; ++i) {
            BlockPos blockPos = pos.down(i);
            BlockState blockState = world.getBlockState(blockPos);
            if (isLitCampfire(blockState)) {
                return true;
            }

            boolean bl = VoxelShapes.matchesAnywhere(SMOKEY_SHAPE, blockState.getCollisionShape(world, pos, ShapeContext.absent()), BooleanBiFunction.AND);
            if (bl) {
                BlockState blockState2 = world.getBlockState(blockPos.down());
                return isLitCampfire(blockState2);
            }
        }

        return false;
    }

    public static boolean isLitCampfire(BlockState state) {
        return state.contains(LIT) && state.isIn(BlockTags.CAMPFIRES) && (Boolean)state.get(LIT);
    }

    protected FluidState getFluidState(BlockState state) {
        return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{LIT, SIGNAL_FIRE, WATERLOGGED, FACING});
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndCampfireBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world instanceof ServerWorld serverWorld) {
            if ((Boolean)state.get(LIT)) {
                ServerRecipeManager.MatchGetter<SingleStackRecipeInput, CampfireCookingRecipe> matchGetter = ServerRecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING);
                return validateTicker(type, Endcampfire.END_CAMPFIRE_BLOCK_ENTITY, (worldx, pos, statex, blockEntity) -> EndCampfireBlockEntity.litServerTick(serverWorld, pos, statex, blockEntity, matchGetter));
            } else {
                return validateTicker(type, Endcampfire.END_CAMPFIRE_BLOCK_ENTITY, EndCampfireBlockEntity::unlitServerTick);
            }
        } else {
            return (Boolean)state.get(LIT) ? validateTicker(type, Endcampfire.END_CAMPFIRE_BLOCK_ENTITY, EndCampfireBlockEntity::clientTick) : null;
        }
    }

    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    public static boolean canBeLit(BlockState state) {
        return state.isIn(BlockTags.CAMPFIRES, (statex) -> statex.contains(WATERLOGGED) && statex.contains(LIT)) && !(Boolean)state.get(WATERLOGGED) && !(Boolean)state.get(LIT);
    }

    static {
        LIT = Properties.LIT;
        SIGNAL_FIRE = Properties.SIGNAL_FIRE;
        WATERLOGGED = Properties.WATERLOGGED;
        FACING = Properties.HORIZONTAL_FACING;
        SHAPE = Block.createColumnShape((double)16.0F, (double)0.0F, (double)7.0F);
        SMOKEY_SHAPE = Block.createColumnShape((double)4.0F, (double)0.0F, (double)16.0F);
    }
}