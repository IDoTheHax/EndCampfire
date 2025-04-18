package net.idothehax.endcampfire;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.idothehax.endcampfire.registry.EndCampfireBlocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Endcampfire implements ModInitializer {
    public static final String MOD_ID = "endcampfire";
    @Override
    public void onInitialize() {
        EndCampfireBlocks.init();
        EndCampfireBlockEntity.END_CAMPFIRE_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(MOD_ID, "end_campfire"),
                FabricBlockEntityTypeBuilder.create(EndCampfireBlockEntity::new, EndCampfireBlocks.END_CAMPFIRE).build()
        );
    }
}