package net.idothehax.endcampfire;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EndCampfireBlock extends CampfireBlock {
    public EndCampfireBlock(Settings settings) {
        super(false, 1, settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(LIT, true)
                .with(FACING, net.minecraft.util.math.Direction.NORTH)
                .with(SIGNAL_FIRE, false)
                .with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING, SIGNAL_FIRE, WATERLOGGED);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        if (!world.isClient && entity instanceof PlayerEntity) {
            entity.setFrozenTicks(entity.getFrozenTicks() + 5);
        }
        super.onEntityCollision(state, world, pos, entity, handler);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT) && world.isClient) {
            world.addParticleClient(ParticleTypes.SNOWFLAKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    0, 0.1, 0);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndCampfireBlockEntity(pos, state);
    }
}