package wraith.redutils.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.util.registry.Registry;
import wraith.redutils.Utils;
import wraith.redutils.block.*;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {

    private static final HashMap<String, Block> BLOCKS = new HashMap<String, Block>() {{
        put("launch_pad", new LaunchPadBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.BLUE).strength(5f, 5f)));
        put("redstone_clock", new RedstoneClockBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.RED).strength(5f, 5f)));
        put("block_breaker", new BlockBreakerBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.WHITE).strength(5f, 5f)));
        put("block_placer", new BlockPlacerBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.WHITE).strength(5f, 5f)));
        put("item_collector", new ItemCollectorBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.WHITE).strength(5f, 5f)));
    }};

    public static void register() {
        for (Map.Entry<String, Block> entry : BLOCKS.entrySet()) {
            Registry.register(Registry.BLOCK, Utils.ID(entry.getKey()), entry.getValue());
        }
    }

    public static Block get(String block) {
        return BLOCKS.getOrDefault(block, Blocks.AIR);
    }

}
