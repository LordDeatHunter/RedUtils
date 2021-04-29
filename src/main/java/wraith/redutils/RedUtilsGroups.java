package wraith.redutils;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import wraith.redutils.registry.ItemRegistry;

public class RedUtilsGroups {

    public static final ItemGroup RED_UTILS = FabricItemGroupBuilder.create(Utils.ID("redutils")).icon(() -> new ItemStack(ItemRegistry.get("launch_pad"))).build();

}
