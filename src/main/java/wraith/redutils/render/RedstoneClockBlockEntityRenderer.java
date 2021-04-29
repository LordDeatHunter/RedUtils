package wraith.redutils.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import wraith.redutils.Utils;
import wraith.redutils.block.RedstoneClockBlockEntity;

public class RedstoneClockBlockEntityRenderer extends BlockEntityRenderer<RedstoneClockBlockEntity> {

    public static final SpriteIdentifier MODEL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Utils.ID("block/redstone_clock_inner"));

    private static final ModelPart innerModel = new ModelPart(64, 64, 0, 0);

    static {
        innerModel.addCuboid(1, 1, 1, 14, 2, 14);
    }

    public RedstoneClockBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(RedstoneClockBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        VertexConsumer vertexConsumer = MODEL_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);

        float not_red = entity.getTickrate() < 1 ? 0 : 1f - entity.getTimer() / (float) entity.getTickrate();
        innerModel.render(matrices, vertexConsumer, light, overlay, 1, not_red, not_red, not_red);

    }
}
