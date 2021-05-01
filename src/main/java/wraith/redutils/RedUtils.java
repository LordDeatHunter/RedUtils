package wraith.redutils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.registry.BlockRegistry;
import wraith.redutils.registry.CustomScreenHandlerRegistry;
import wraith.redutils.registry.ItemRegistry;
import wraith.redutils.screen.EntityDetectorScreenHandler;
import wraith.redutils.screen.PlayerDetectorScreenHandler;

public class RedUtils implements ModInitializer {

    public static final String MOD_ID = "redutils";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Loading [Red Utils]");

        RegisterPacketHandlers();

        ItemRegistry.register();
        BlockRegistry.register();
        BlockEntityRegistry.register();
        CustomScreenHandlerRegistry.register();

        LOGGER.info("[Red Utils] has successfully been loaded!");
    }

    private void RegisterPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("entity_detector.rename"), (server, player, networkHandler, data, sender) -> {
            if (!(player.currentScreenHandler instanceof EntityDetectorScreenHandler)) {
                return;
            }
            CompoundTag tag = data.readCompoundTag();
            ((EntityDetectorScreenHandler)player.currentScreenHandler).addEntity(tag.getString("entity_id"));
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("player_detector.rename"), (server, player, networkHandler, data, sender) -> {
            if (!(player.currentScreenHandler instanceof PlayerDetectorScreenHandler)) {
                return;
            }
            CompoundTag tag = data.readCompoundTag();
            ((PlayerDetectorScreenHandler)player.currentScreenHandler).addPlayername(tag.getString("playername"));
        });
    }

}
