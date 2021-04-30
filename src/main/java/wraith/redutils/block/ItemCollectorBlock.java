package wraith.redutils.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import wraith.redutils.registry.ItemRegistry;

public class ItemCollectorBlock extends BlockWithEntity {

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty FACING = Properties.FACING;

    private static final VoxelShape SHAPE_BOT = Block.createCuboidShape(6, 0, 6, 10, 11, 10).simplify();
    private static final VoxelShape SHAPE_TOP = Block.createCuboidShape(6, 5, 6, 10, 16, 10).simplify();

    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(5, 6, 6, 16, 10, 10).simplify();
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(0, 6, 6, 11, 10, 10).simplify();

    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(6, 6,0, 10, 10, 11).simplify();
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(6, 6,5, 10, 10, 16).simplify();

    public ItemCollectorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.UP).with(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new ItemCollectorBlockEntity();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER).with(FACING, ctx.getSide());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch(state.get(FACING)) {
            case UP:
                return SHAPE_BOT;
            case DOWN:
                return SHAPE_TOP;
            case EAST:
                return SHAPE_WEST;
            case WEST:
                return SHAPE_EAST;
            case NORTH:
                return SHAPE_SOUTH;
            default:
                return SHAPE_NORTH;
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    @Override
    public boolean canMobSpawnInside() {
        return true;
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

}
