package wraith.redutils.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import wraith.redutils.Utils;
import wraith.redutils.screen.*;

import java.util.HashMap;

public class CustomScreenHandlerRegistry {

    private static HashMap<String, ScreenHandlerType<? extends ScreenHandler>> SCREEN_HANDLERS = new HashMap<>();

    public static void register() {
        SCREEN_HANDLERS.put("launch_pad", ScreenHandlerRegistry.registerExtended(Utils.ID("launch_pad"), LaunchPadScreenHandler::new));
        SCREEN_HANDLERS.put("redstone_clock", ScreenHandlerRegistry.registerExtended(Utils.ID("redstone_clock"), RedstoneClockScreenHandler::new));
        SCREEN_HANDLERS.put("block_breaker", ScreenHandlerRegistry.registerSimple(Utils.ID("block_breaker"), BlockBreakerScreenHandler::new));
        SCREEN_HANDLERS.put("block_placer", ScreenHandlerRegistry.registerSimple(Utils.ID("block_placer"), BlockPlacerScreenHandler::new));
        SCREEN_HANDLERS.put("item_collector", ScreenHandlerRegistry.registerExtended(Utils.ID("item_collector"), ItemCollectorScreenHandler::new));
        SCREEN_HANDLERS.put("entity_detector", ScreenHandlerRegistry.registerExtended(Utils.ID("entity_detector"), EntityDetectorScreenHandler::new));
    }

    public static ScreenHandlerType<?> get(String id) {
        return SCREEN_HANDLERS.getOrDefault(id, null);
    }

}
