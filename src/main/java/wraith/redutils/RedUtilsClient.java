package wraith.redutils;

import net.fabricmc.api.ClientModInitializer;
import wraith.redutils.registry.CustomBlockEntityRendererRegistry;
import wraith.redutils.registry.CustomScreenRegistry;

public class RedUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CustomScreenRegistry.register();
        CustomBlockEntityRendererRegistry.register();
    }

}
