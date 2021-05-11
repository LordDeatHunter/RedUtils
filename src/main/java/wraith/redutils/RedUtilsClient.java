package wraith.redutils;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.color.block.BlockColorProvider;
import wraith.redutils.registry.CustomBlockEntityRendererRegistry;
import wraith.redutils.registry.CustomColorProviderRegistry;
import wraith.redutils.registry.CustomScreenRegistry;

public class RedUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CustomScreenRegistry.register();
        CustomBlockEntityRendererRegistry.register();
        CustomColorProviderRegistry.register();
    }

}
