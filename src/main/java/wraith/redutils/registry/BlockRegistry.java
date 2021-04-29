package wraith.redutils.registry;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.util.registry.Registry;
import wraith.redutils.Utils;
import wraith.redutils.block.LaunchPadBlock;
import wraith.redutils.block.RedstoneClockBlock;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {

    private static final HashMap<String, Block> BLOCKS = new HashMap<String, Block>() {{
        put("launch_pad", new LaunchPadBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.BLUE).strength(5f, 5f)));
        put("redstone_clock", new RedstoneClockBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.BLUE).strength(5f, 5f)));

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