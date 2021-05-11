package wraith.redutils.registry;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.registry.Registry;
import wraith.redutils.Utils;
import wraith.redutils.block.*;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {

    private static final HashMap<String, Block> BLOCKS = new HashMap<String, Block>() {{
        put("launch_pad", new LaunchPadBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.BLUE).strength(5f, 5f)));
        put("redstone_clock", new RedstoneClockBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.RED).strength(5f, 5f)));
        put("block_breaker", new BlockBreakerBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.GRAY).strength(5f, 5f)));
        put("block_placer", new BlockPlacerBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.GRAY).strength(5f, 5f)));
        put("item_collector", new ItemCollectorBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.WHITE).strength(5f, 5f)));
        put("entity_detector", new EntityDetectorBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.GREEN).strength(5f, 5f)));
        put("player_detector", new PlayerDetectorBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.WHITE).strength(5f, 5f)));
        put("item_user", new ItemUserBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.BLUE).strength(5f, 5f)));

        put("red_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.RED).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("orange_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.ORANGE).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("yellow_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.YELLOW).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("brown_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.BROWN).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("cyan_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.CYAN).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("blue_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.BLUE).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("light_blue_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.LIGHT_BLUE).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("magenta_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.MAGENTA).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("pink_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.PINK).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("purple_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.PURPLE).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("lime_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.LIME).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("green_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.GREEN).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("white_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.WHITE).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("light_gray_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.LIGHT_GRAY).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("gray_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.GRAY).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));
        put("black_lamp", new RedstoneLampBlock(FabricBlockSettings.of(Material.GLASS, MaterialColor.BLACK).strength(0.5f).luminance((bs) -> bs.get(Properties.LIT) ? 15 : 0).sounds(BlockSoundGroup.GLASS)));

        put("sticky_redstone", new StickyRedstoneWire(FabricBlockSettings.of(Material.SUPPORTED, MaterialColor.GREEN).breakInstantly().noCollision()));
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
