package wraith.redutils.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import wraith.redutils.RedUtilsGroups;
import wraith.redutils.Utils;
import wraith.redutils.item.StickyRedstoneItem;

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
        put("player_detector", new BlockItem(BlockRegistry.get("player_detector"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("item_user", new BlockItem(BlockRegistry.get("item_user"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));

        put("red_lamp", new BlockItem(BlockRegistry.get("red_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("orange_lamp", new BlockItem(BlockRegistry.get("orange_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("yellow_lamp", new BlockItem(BlockRegistry.get("yellow_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("brown_lamp", new BlockItem(BlockRegistry.get("brown_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("cyan_lamp", new BlockItem(BlockRegistry.get("cyan_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("blue_lamp", new BlockItem(BlockRegistry.get("blue_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("light_blue_lamp", new BlockItem(BlockRegistry.get("light_blue_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("magenta_lamp", new BlockItem(BlockRegistry.get("magenta_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("pink_lamp", new BlockItem(BlockRegistry.get("pink_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("purple_lamp", new BlockItem(BlockRegistry.get("purple_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("lime_lamp", new BlockItem(BlockRegistry.get("lime_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("green_lamp", new BlockItem(BlockRegistry.get("green_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("white_lamp", new BlockItem(BlockRegistry.get("white_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("light_gray_lamp", new BlockItem(BlockRegistry.get("light_gray_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("gray_lamp", new BlockItem(BlockRegistry.get("gray_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));
        put("black_lamp", new BlockItem(BlockRegistry.get("black_lamp"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS)));

        put("redstone_configurator", new Item(new FabricItemSettings().group(RedUtilsGroups.RED_UTILS).maxCount(1)));
        put("pickaxe_upgrade", new Item(new FabricItemSettings().group(RedUtilsGroups.RED_UTILS).maxCount(1)));

        put("sticky_redstone", new StickyRedstoneItem(BlockRegistry.get("sticky_redstone"), new FabricItemSettings().group(RedUtilsGroups.RED_UTILS).maxCount(1)));
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
