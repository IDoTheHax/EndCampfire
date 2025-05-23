package net.idothehax.endcampfire.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.idothehax.endcampfire.Endcampfire;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class EndcampfireClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(Endcampfire.END_CAMPFIRE_BLOCK_ENTITY, EndCampfireBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(Endcampfire.END_CAMPFIRE, RenderLayer.getCutout());
    }
}
