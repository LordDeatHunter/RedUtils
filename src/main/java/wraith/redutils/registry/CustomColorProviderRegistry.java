package wraith.redutils.registry;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import wraith.redutils.block.StickyRedstoneWire;

public class CustomColorProviderRegistry {

    private CustomColorProviderRegistry() {}

    public static void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.get("sticky_redstone"), RenderLayer.getCutout());

        ColorProviderRegistry.BLOCK.register((state, view, pos, index) -> StickyRedstoneWire.getWireColor(state.get(StickyRedstoneWire.POWER)), BlockRegistry.get("sticky_redstone"));
    }

}
