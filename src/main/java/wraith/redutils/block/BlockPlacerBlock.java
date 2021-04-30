package wraith.redutils.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.redutils.Utils;
import wraith.redutils.registry.ItemRegistry;

public class BlockPlacerBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty POWERED = Properties.LIT;

    public BlockPlacerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (world.isClient) {
            return;
        }
        BlockState oldState = state;
        world.setBlockState(pos, state.with(POWERED, false));
        for(Direction direction : Direction.values()) {
            if (direction != state.get(FACING) && world.isEmittingRedstonePower(pos.offset(direction), direction)) {
                state = state.with(POWERED, true);
                world.setBlockState(pos, state);
                break;
            }
        }
        if (!state.get(POWERED) || oldState.get(POWERED)) {
            return;
        }
        BlockPos frontPos = pos.offset(state.get(FACING));
        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof BlockPlacerBlockEntity)) {
            return;
        }
        ItemStack stack = ((BlockPlacerBlockEntity) entity).getStack(0);
        if (!(stack.getItem() instanceof BlockItem)) {
            return;
        }
        BlockItem blockItem = (BlockItem) stack.getItem();
        Direction side = world.isAir(frontPos.down()) ? state.get(FACING) : Direction.UP;

        blockItem.place(new AutomaticItemPlacementContext(world, frontPos, state.get(FACING), stack, side));
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite()).with(POWERED, false);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new BlockPlacerBlockEntity();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.FAIL;
        }
        if (player.getStackInHand(hand).getItem() == ItemRegistry.get("redstone_configurator")) {
            NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
            if (factory != null) {
                player.openHandledScreen(factory);
                return ActionResult.CONSUME;
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Inventory) {
                ItemScatterer.spawn(world, pos, (Inventory)blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

}
