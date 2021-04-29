package wraith.redutils;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.registry.BlockRegistry;
import wraith.redutils.registry.CustomScreenHandlerRegistry;
import wraith.redutils.registry.ItemRegistry;

public class RedUtils implements ModInitializer {

    public static final String MOD_ID = "redutils";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Loading [Red Utils]");

        ItemRegistry.register();
        BlockRegistry.register();
        BlockEntityRegistry.register();
        CustomScreenHandlerRegistry.register();

        LOGGER.info("[Red Utils] has successfully been loaded!");
    }

}
