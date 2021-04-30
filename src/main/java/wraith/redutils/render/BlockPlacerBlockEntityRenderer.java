package wraith.redutils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import wraith.redutils.Utils;
import wraith.redutils.block.BlockPlacerBlockEntity;

public class BlockPlacerBlockEntityRenderer extends BlockEntityRenderer<BlockPlacerBlockEntity> {

    public BlockPlacerBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(BlockPlacerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!(entity.getStack(0).getItem() instanceof BlockItem)) {
            return;
        }
        BlockItem item = (BlockItem) entity.getStack(0).getItem();
        matrices.push();
        matrices.scale(0.75F, 0.75F, 0.75F);
        matrices.translate(0.2F, 0.2F, 0.2F);
        MinecraftClient.getInstance().getBlockRenderManager().renderBlock(item.getBlock().getDefaultState(), entity.getPos(), entity.getWorld(), matrices, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(item.getBlock().getDefaultState())), false, Utils.RANDOM);
        matrices.pop();
    }

}
