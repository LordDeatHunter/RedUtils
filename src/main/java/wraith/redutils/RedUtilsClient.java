package wraith.redutils;

import net.fabricmc.api.ClientModInitializer;
import wraith.redutils.registry.BlockEntityRendererRegistration;
import wraith.redutils.registry.CustomScreenRegistry;

public class RedUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CustomScreenRegistry.register();
        BlockEntityRendererRegistration.register();
    }

}
