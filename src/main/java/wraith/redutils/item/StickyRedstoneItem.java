package wraith.redutils.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import wraith.redutils.block.StickyRedstoneWire;

import java.util.Iterator;

public class StickyRedstoneItem extends BlockItem {

    public StickyRedstoneItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
        if (itemPlacementContext == null) {
            return ActionResult.FAIL;
        }
        World world = itemPlacementContext.getWorld();
        BlockState placeState = this.getPlacementState(itemPlacementContext);
        if (placeState == null) {
            return ActionResult.FAIL;
        }
        BlockPos blockPos = itemPlacementContext.getBlockPos();
        PlayerEntity playerEntity = itemPlacementContext.getPlayer();
        ItemStack itemStack = itemPlacementContext.getStack();
        BlockState oldState = world.getBlockState(blockPos);
        Block oldBlock = oldState.getBlock();

        if (context.canPlace()) {
            if (!this.place(itemPlacementContext, placeState)) {
                return ActionResult.FAIL;
            }
        } else if (oldBlock instanceof StickyRedstoneWire) {
            if (!StickyRedstoneWire.placeOnWall(world, blockPos, oldState, placeState, itemPlacementContext.getPlacementDirections(), (StickyRedstoneWire) oldBlock)) {
                return ActionResult.FAIL;
            }
        } else {
            return ActionResult.FAIL;
        }
        BlockState newState = world.getBlockState(blockPos);
        Block newBlock = newState.getBlock();

        if (newBlock == placeState.getBlock()) {
            newState = this.placeFromTag(blockPos, world, itemStack, newState);
            this.postPlacement(blockPos, world, playerEntity, itemStack, newState);
            newBlock.onPlaced(world, blockPos, newState, playerEntity, itemStack);
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
            }
        }

        BlockSoundGroup blockSoundGroup = newState.getSoundGroup();
        world.playSound(playerEntity, blockPos, this.getPlaceSound(newState), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
        if (playerEntity == null || !playerEntity.abilities.creativeMode) {
            itemStack.decrement(1);
        }

        return ActionResult.success(world.isClient);
    }

    private BlockState placeFromTag(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockState blockState = state;
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag != null) {
            CompoundTag compoundTag2 = compoundTag.getCompound("BlockStateTag");
            StateManager<Block, BlockState> stateManager = state.getBlock().getStateManager();

            for (String string : compoundTag2.getKeys()) {
                Property<?> property = stateManager.getProperty(string);
                if (property != null) {
                    String string2 = compoundTag2.get(string).asString();
                    blockState = with(blockState, property, string2);
                }
            }
        }

        if (blockState != state) {
            world.setBlockState(pos, blockState, 2);
        }

        return blockState;
    }

    private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String name) {
        return property.parse(name).map((value) -> state.with(property, value)).orElse(state);
    }

}
