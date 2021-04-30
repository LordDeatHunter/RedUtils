package wraith.redutils.registry;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntityType;
import wraith.redutils.block.BlockPlacerBlockEntity;
import wraith.redutils.block.RedstoneClockBlockEntity;
import wraith.redutils.render.BlockPlacerBlockEntityRenderer;
import wraith.redutils.render.RedstoneClockBlockEntityRenderer;

public class CustomBlockEntityRendererRegistry {

    @SuppressWarnings("unchecked")
    public static void register() {

        BlockEntityRendererRegistry.INSTANCE.register((BlockEntityType<RedstoneClockBlockEntity>) BlockEntityRegistry.get("redstone_clock"), RedstoneClockBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register((BlockEntityType<BlockPlacerBlockEntity>) BlockEntityRegistry.get("block_placer"), BlockPlacerBlockEntityRenderer::new);

    }

}
