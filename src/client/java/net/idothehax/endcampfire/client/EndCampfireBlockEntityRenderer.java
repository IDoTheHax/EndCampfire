package net.idothehax.endcampfire.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.idothehax.endcampfire.EndCampfireBlockEntity;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class EndCampfireBlockEntityRenderer implements BlockEntityRenderer<EndCampfireBlockEntity> {
    private static final float SCALE = 0.375F;
    private final ItemRenderer itemRenderer;

    public EndCampfireBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    public void render(EndCampfireBlockEntity endCampfireBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, Vec3d vec3d) {
        Direction direction = (Direction)endCampfireBlockEntity.getCachedState().get(CampfireBlock.FACING);
        DefaultedList<ItemStack> defaultedList = endCampfireBlockEntity.getItemsBeingCooked();
        int k = (int)endCampfireBlockEntity.getPos().asLong();

        for(int l = 0; l < defaultedList.size(); ++l) {
            ItemStack itemStack = (ItemStack)defaultedList.get(l);
            if (itemStack != ItemStack.EMPTY) {
                matrixStack.push();
                matrixStack.translate(0.5F, 0.44921875F, 0.5F);
                Direction direction2 = Direction.fromHorizontalQuarterTurns((l + direction.getHorizontalQuarterTurns()) % 4);
                float g = -direction2.getPositiveHorizontalDegrees();
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                matrixStack.translate(-0.3125F, -0.3125F, 0.0F);
                matrixStack.scale(0.375F, 0.375F, 0.375F);
                this.itemRenderer.renderItem(itemStack, ItemDisplayContext.FIXED, i, j, matrixStack, vertexConsumerProvider, endCampfireBlockEntity.getWorld(), k + l);
                matrixStack.pop();
            }
        }

    }
}

