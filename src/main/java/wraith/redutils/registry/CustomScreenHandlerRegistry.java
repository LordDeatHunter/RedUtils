package wraith.redutils.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import wraith.redutils.Utils;
import wraith.redutils.screen.LaunchPadScreenHandler;
import wraith.redutils.screen.RedstoneClockScreenHandler;

import java.util.HashMap;

public class CustomScreenHandlerRegistry {

    private static HashMap<String, ScreenHandlerType<? extends ScreenHandler>> SCREEN_HANDLERS = new HashMap<>();

    public static void register() {
        SCREEN_HANDLERS.put("launch_pad", ScreenHandlerRegistry.registerExtended(Utils.ID("launch_pad"), LaunchPadScreenHandler::new));
        SCREEN_HANDLERS.put("redstone_clock", ScreenHandlerRegistry.registerExtended(Utils.ID("redstone_clock"), RedstoneClockScreenHandler::new));
    }

    public static ScreenHandlerType<?> get(String id) {
        return SCREEN_HANDLERS.getOrDefault(id, null);
    }

}
