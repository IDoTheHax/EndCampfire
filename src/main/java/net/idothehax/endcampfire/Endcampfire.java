package net.idothehax.endcampfire;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Set;
import java.util.function.Function;

public class Endcampfire implements ModInitializer {
    public static final String MOD_ID = "endcampfire";

    public static final Block END_CAMPFIRE = register(
            "end_campfire",
            (settings) -> new EndCampfireBlock(true, 1, settings),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.SPRUCE_BROWN)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)
                    .sounds(BlockSoundGroup.WOOD)
                    .luminance(Blocks.createLightLevelFromLitBlockState(15))
                    .nonOpaque()
                    .burnable()
    );

    public static final BlockEntityType<EndCampfireBlockEntity> END_CAMPFIRE_BLOCK_ENTITY =
            create("campfire", EndCampfireBlockEntity::new, END_CAMPFIRE);

    public static final Item END_CAMPFIRE_ITEM = registerItem(
            "end_campfire",
            (settings) -> new BlockItem(END_CAMPFIRE, settings),
            new Item.Settings());

    private static <T extends BlockEntity> BlockEntityType<T> create(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Identifier.of(MOD_ID, name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static Block register(RegistryKey<Block> key, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static Item registerItem(RegistryKey<Item> key, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    private static Block register(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return register(keyOf(id), factory, settings);
    }

    private static Item registerItem(String id, Function<Item.Settings, Item> factory, Item.Settings settings) {
        return registerItem(keyOfItem(id), factory, settings);
    }

    private static RegistryKey<Block> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, id));
    }

    private static RegistryKey<Item> keyOfItem(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, id));
    }

    @Override
    public void onInitialize() {
        // Add the BlockItem to the FUNCTIONAL item group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((itemGroup) -> {
            itemGroup.add(END_CAMPFIRE_ITEM);
        });
    }
}