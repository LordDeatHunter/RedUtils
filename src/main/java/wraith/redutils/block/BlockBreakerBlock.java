package wraith.redutils.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
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

import java.util.List;
import java.util.stream.IntStream;

public class BlockBreakerBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty POWERED = Properties.LIT;

    public BlockBreakerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (world.isClient) {
            return;
        }
        BlockState oldState = state;
        world.setBlockState(pos, state.with(POWERED, false), 2);
        for(Direction direction : Direction.values()) {
            if (direction != state.get(FACING) && world.isEmittingRedstonePower(pos.offset(direction), direction)) {
                state = state.with(POWERED, true);
                world.setBlockState(pos, state, 2);
                break;
            }
        }
        if (!state.get(POWERED) || oldState.get(POWERED)) {
            return;
        }
        BlockPos frontPos = pos.offset(state.get(FACING));
        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof BlockBreakerBlockEntity)) {
            return;
        }
        BlockState frontBlockState = world.getBlockState(frontPos);
        BlockEntity frontEntity = world.getBlockEntity(frontPos);
        if (frontBlockState.isAir()) {
            return;
        }
        ItemStack stack = ((BlockBreakerBlockEntity) entity).getStack(0);

        boolean isUpgrade = stack.getItem() == ItemRegistry.get("pickaxe_upgrade");

        boolean canMine = stack.getItem().canMine(frontBlockState, world, pos, null);
        boolean toolNotRequired = !frontBlockState.isToolRequired();
        boolean isEffectiveOn = isUpgrade || stack.isEffectiveOn(frontBlockState);
        boolean mineable = frontBlockState.getHardness(world, frontPos) >= 0;

        boolean broken = false;
        if (canMine && mineable && (toolNotRequired || isEffectiveOn)) {
            broken = world.breakBlock(frontPos, false);
            if (broken && !isUpgrade && stack.damage(1, Utils.RANDOM, null)) {
                stack.decrement(1);
            }
        }
        BlockPos backPos = pos.offset(state.get(FACING).getOpposite());
        Inventory storage = HopperBlockEntity.getInventoryAt(world, backPos);

        if (broken) {
            List<ItemStack> items = Block.getDroppedStacks(frontBlockState, (ServerWorld) world, frontPos, frontEntity);
            for (ItemStack item : items) {
                if (storage != null && !Utils.isInventoryFull(storage, state.get(FACING))) {
                    Utils.transferItem(item, storage);
                }
                if (!item.isEmpty()) {
                    ItemEntity drop = new ItemEntity(world, backPos.getX() + 0.5D, backPos.getY() + 0.5D, backPos.getZ() + 0.5D, item);
                    BlockPos velocity = backPos.subtract(pos);
                    drop.setVelocity(velocity.getX() * 0.125D, velocity.getY() * 0.125D, velocity.getZ() * 0.125D);
                    world.spawnEntity(drop);
                }
            }
        }
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
        return new BlockBreakerBlockEntity();
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
