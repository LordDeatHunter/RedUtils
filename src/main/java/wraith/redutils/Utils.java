package wraith.redutils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.LootTable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import wraith.redutils.block.ItemUserBlock;

import java.util.*;
import java.util.stream.IntStream;

public class Utils {

    public static final Random RANDOM = new Random();
    public static Identifier ID(String path) {
        return new Identifier(RedUtils.MOD_ID, path);
    }

    public static List<Item> extractDrops(LootTable table) {
        Gson GSON = LootGsons.getTableGsonBuilder().create();

        JsonObject tableJSON = GSON.toJsonTree(table).getAsJsonObject();
        List<Item> drops = new ArrayList<>();

        try {
            for (JsonElement poolElement : tableJSON.get("pools").getAsJsonArray()) {

                JsonObject pool = poolElement.getAsJsonObject();
                JsonArray entries = pool.get("entries").getAsJsonArray();

                for (JsonElement entryElement : entries) {

                    JsonObject entry = entryElement.getAsJsonObject();

                    drops.add(Registry.ITEM.get(new Identifier(entry.get("name").getAsString())));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return drops;
    }

    public static void transferItem(ItemStack stack, Inventory storage) {
        for (int i = 0; i < storage.size() && !stack.isEmpty(); ++i) {
            ItemStack storageStack = storage.getStack(i);
            if (storageStack.isEmpty()) {
                ItemStack itemCopy = stack.copy();
                int amount = Math.min(Math.min(stack.getCount(), storage.getMaxCountPerStack()), stack.getMaxCount());
                itemCopy.setCount(amount);
                storage.setStack(i, itemCopy);
                stack.decrement(amount);
            } else if (ItemStack.areItemsEqual(stack, storageStack) && storageStack.getCount() < storageStack.getMaxCount() && storageStack.getCount() < storage.getMaxCountPerStack()) {
                int amount = Math.min(Math.min(storageStack.getCount() + stack.getCount(), storage.getMaxCountPerStack()), stack.getMaxCount());
                storageStack.setCount(amount);
                stack.decrement(amount);
            }
        }
    }

    public static boolean isInventoryFull(Inventory inv, Direction direction) {
        return getAvailableSlots(inv, direction).allMatch((i) -> {
            ItemStack itemStack = inv.getStack(i);
            return itemStack.getCount() >= itemStack.getMaxCount();
        });
    }

    public static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        return inventory instanceof SidedInventory ? IntStream.of(((SidedInventory)inventory).getAvailableSlots(side)) : IntStream.range(0, inventory.size());
    }

    public static ServerPlayerEntity getFakePlayer(World world, BlockPos pos, BlockState state) {
        ServerPlayerEntity player = null;
        if (world != null && !world.isClient) {
            player = new ServerPlayerEntity(((ServerWorld) world).getServer(), (ServerWorld) world, new GameProfile(UUID.randomUUID(), ""), new ServerPlayerInteractionManager((ServerWorld) world));
            Direction direction = state.get(ItemUserBlock.FACING);
            switch (direction) {
                case UP:
                    player.yaw = 0;
                    player.pitch = -90;
                    break;
                case DOWN:
                    player.yaw = 0;
                    player.pitch = 90;
                    break;
                case EAST:
                    player.yaw = -90;
                    player.pitch = 0;
                    break;
                case WEST:
                    player.yaw = 90;
                    player.pitch = 0;
                    break;
                case SOUTH:
                    player.yaw = 0;
                    player.pitch = 0;
                    break;
                case NORTH:
                    player.yaw = 180;
                    player.pitch = 0;
                    break;
            }
            BlockPos frontPos = pos.offset(direction);
            player.setPos(frontPos.getX() + 0.5D, frontPos.getY() - 1.0D, frontPos.getZ() + 0.5D);
        }
        return player;
    }

}
