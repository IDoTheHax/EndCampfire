package net.idothehax.endcampfire;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EndCampfireBlock extends CampfireBlock {
    public EndCampfireBlock(boolean emitsParticles, int fireDamage, AbstractBlock.Settings settings) {
        super(emitsParticles, fireDamage, settings);
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
        }

        super.onEntityCollision(state, world, pos, entity, handler);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{LIT, SIGNAL_FIRE, WATERLOGGED, FACING});
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndCampfireBlockEntity(pos, state);
    }

}