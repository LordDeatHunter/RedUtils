package wraith.redutils.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import wraith.redutils.RedUtilsGroups;
import wraith.redutils.Utils;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {

    private static final HashMap<String, Item> ITEMS = new HashMap<String, Item>() {{
        put("launch_pad", new BlockItem(BlockRegistry.get("launch_pad"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("redstone_clock", new BlockItem(BlockRegistry.get("redstone_clock"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("block_breaker", new BlockItem(BlockRegistry.get("block_breaker"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("block_placer", new BlockItem(BlockRegistry.get("block_placer"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("item_collector", new BlockItem(BlockRegistry.get("item_collector"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("entity_detector", new BlockItem(BlockRegistry.get("entity_detector"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));

        put("redstone_configurator", new Item(new FabricItemSettings().group(RedUtilsGroups.RED_UTILS).maxCount(1)));
        put("pickaxe_upgrade", new Item(new FabricItemSettings().group(RedUtilsGroups.RED_UTILS).maxCount(1)));
    }};

    public static void register() {
        for (Map.Entry<String, Item> entry : ITEMS.entrySet()) {
            Registry.register(Registry.ITEM, Utils.ID(entry.getKey()), entry.getValue());
        }
    }

    public static Item get(String item) {
        return ITEMS.getOrDefault(item, Items.AIR);
    }

}
