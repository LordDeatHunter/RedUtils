package wraith.redutils.registry;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntityType;
import wraith.redutils.block.RedstoneClockBlockEntity;
import wraith.redutils.render.RedstoneClockBlockEntityRenderer;

public class BlockEntityRendererRegistration {

    @SuppressWarnings("unchecked")
    public static void register() {

        BlockEntityRendererRegistry.INSTANCE.register((BlockEntityType<RedstoneClockBlockEntity>) BlockEntityRegistry.get("redstone_clock"), RedstoneClockBlockEntityRenderer::new);

    }

}
