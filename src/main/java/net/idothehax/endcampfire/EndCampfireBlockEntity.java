package net.idothehax.endcampfire;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.util.math.BlockPos;

public class EndCampfireBlockEntity extends CampfireBlockEntity {
    public static BlockEntityType<EndCampfireBlockEntity> END_CAMPFIRE_BLOCK_ENTITY;

    public EndCampfireBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return END_CAMPFIRE_BLOCK_ENTITY != null ? END_CAMPFIRE_BLOCK_ENTITY : super.getType();
    }
}