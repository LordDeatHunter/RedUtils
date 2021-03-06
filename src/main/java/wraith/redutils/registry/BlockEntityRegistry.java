package wraith.redutils.registry;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import wraith.redutils.Utils;
import wraith.redutils.block.*;

import java.util.HashMap;
import java.util.Map;

public class BlockEntityRegistry {

    private BlockEntityRegistry(){}

    private static final HashMap<String, BlockEntityType<?>> BLOCK_ENTITIES = new HashMap<String, BlockEntityType<?>>() {{
        put("launch_pad", BlockEntityType.Builder.create(LaunchPadBlockEntity::new, BlockRegistry.get("launch_pad")).build(null));
        put("redstone_clock", BlockEntityType.Builder.create(RedstoneClockBlockEntity::new, BlockRegistry.get("redstone_clock")).build(null));
        put("block_breaker", BlockEntityType.Builder.create(BlockBreakerBlockEntity::new, BlockRegistry.get("block_breaker")).build(null));
        put("block_placer", BlockEntityType.Builder.create(BlockPlacerBlockEntity::new, BlockRegistry.get("block_placer")).build(null));
        put("item_collector", BlockEntityType.Builder.create(ItemCollectorBlockEntity::new, BlockRegistry.get("item_collector")).build(null));
        put("entity_detector", BlockEntityType.Builder.create(EntityDetectorBlockEntity::new, BlockRegistry.get("entity_detector")).build(null));
        put("player_detector", BlockEntityType.Builder.create(PlayerDetectorBlockEntity::new, BlockRegistry.get("player_detector")).build(null));
        put("item_user", BlockEntityType.Builder.create(ItemUserBlockEntity::new, BlockRegistry.get("item_user")).build(null));
    }};

    public static void register() {
        for (Map.Entry<String, BlockEntityType<?>> entry : BLOCK_ENTITIES.entrySet()) {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, Utils.ID(entry.getKey()), entry.getValue());
        }
    }

    public static BlockEntityType<?> get(String id) {
        return BLOCK_ENTITIES.getOrDefault(id, null);
    }


}
