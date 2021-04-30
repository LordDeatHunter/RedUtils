package wraith.redutils.registry;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import wraith.redutils.screen.BlockBreakerScreen;
import wraith.redutils.screen.BlockPlacerScreen;
import wraith.redutils.screen.LaunchPadScreen;
import wraith.redutils.screen.RedstoneClockScreen;

public class CustomScreenRegistry {

    public static void register() {
        ScreenRegistry.register(CustomScreenHandlerRegistry.get("launch_pad"), LaunchPadScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.get("redstone_clock"), RedstoneClockScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.get("block_breaker"), BlockBreakerScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.get("block_placer"), BlockPlacerScreen::new);
    }

}
