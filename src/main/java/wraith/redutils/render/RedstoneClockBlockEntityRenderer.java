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
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import wraith.redutils.Utils;
import wraith.redutils.block.RedstoneClockBlock;
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

        matrices.push();
        Direction direction = entity.getCachedState().get(RedstoneClockBlock.FACING);
        if (direction == Direction.DOWN) {
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180));
            matrices.translate(0F, -1F, -1F);
        } else if (direction == Direction.SOUTH) {
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
            matrices.translate(0F, 0F, -1F);
        } else if (direction == Direction.NORTH) {
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(270));
            matrices.translate(0F, -1F, 0F);
        } else if (direction == Direction.EAST) {
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(270));
            matrices.translate(-1F, 0F, 0F);
        } else if (direction == Direction.WEST) {
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90));
            matrices.translate(0F, -1F, 0F);
        }

        float color = entity.getTimer() < entity.getTicktime() ? 0F : 1F - ((float)entity.getTimer() / (entity.getTickrate() + entity.getTicktime()));
        innerModel.render(matrices, vertexConsumer, light, overlay, 1, color, color, color);
        matrices.pop();
    }
}
