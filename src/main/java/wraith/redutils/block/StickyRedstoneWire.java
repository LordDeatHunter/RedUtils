package wraith.redutils.block;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import wraith.redutils.registry.BlockRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class StickyRedstoneWire extends Block {

    public static final DirectionProperty FACING = Properties.FACING;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_NORTH;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_EAST;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_SOUTH;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_WEST;
    public static final IntProperty POWER;
    public static final HashMap<Direction, HashMap<Direction, EnumProperty<WireConnection>>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY;
    private static final HashMap<Direction, HashMap<Direction, Direction>> ROTATED_DIRECTIONS;
    private static final HashMap<Direction, VoxelShape> DOT_SHAPES;
    private static final HashMap<Direction, HashMap<Direction, VoxelShape>> SIDE_OUTLINE_SHAPES;
    private static final HashMap<Direction, HashMap<Direction, VoxelShape>> UP_OUTLINE_SHAPES;
    private final Map<BlockState, VoxelShape> outlineShapes = Maps.newHashMap();
    private static final Vector3f[] COLORS;
    private final BlockState dotState;
    private boolean wiresGivePower = true;

    public StickyRedstoneWire(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WIRE_CONNECTION_NORTH, WireConnection.NONE).with(WIRE_CONNECTION_EAST, WireConnection.NONE).with(WIRE_CONNECTION_SOUTH, WireConnection.NONE).with(WIRE_CONNECTION_WEST, WireConnection.NONE).with(POWER, 0).with(FACING, Direction.UP));
        this.dotState = this.getDefaultState().with(WIRE_CONNECTION_NORTH, WireConnection.SIDE).with(WIRE_CONNECTION_EAST, WireConnection.SIDE).with(WIRE_CONNECTION_SOUTH, WireConnection.SIDE).with(WIRE_CONNECTION_WEST, WireConnection.SIDE).with(FACING, Direction.UP);

        for (BlockState blockState : this.getStateManager().getStates()) {
            if (blockState.get(POWER) == 0) {
                this.outlineShapes.put(blockState, this.createOutlineShape(blockState));
            }
        }

    }

    public static boolean placeOnWall(World world, BlockPos pos, BlockState oldState, BlockState placeState, Direction[] placementDirections, StickyRedstoneWire block) {
        if (placeState.getBlock() != block) {
            return false;
        }
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(oldState.get(FACING));

        for (Direction direction : placementDirections) {
            if (!map.containsKey(direction)) {
                continue;
            }
            BlockState testState = placeState.with(FACING, direction.getOpposite());

            if (testState.canPlaceAt(world, pos)) {
                EnumProperty<WireConnection> dir = map.get(direction);
                if (oldState.get(dir) == WireConnection.UP) {
                    continue;
                }
                world.setBlockState(pos, oldState.with(dir, WireConnection.UP));
                block.makeLineConnection(world, placeState, pos);
                return true;
            }
        }
        return false;
    }

    private VoxelShape createOutlineShape(BlockState state) {
        VoxelShape voxelShape = DOT_SHAPES.get(state.get(FACING));

        Direction facing = state.get(FACING);
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(facing);
        for (Direction direction : Direction.values()) {
            if (!map.containsKey(direction)) {
                continue;
            }
            WireConnection wireConnection = state.get(map.get(direction));
            HashMap<Direction, Direction> rotatedDirection = ROTATED_DIRECTIONS.get(facing);
            HashMap<Direction, VoxelShape> outline = null;
            if (wireConnection == WireConnection.SIDE) {
                outline = SIDE_OUTLINE_SHAPES.get(facing);
            } else if (wireConnection == WireConnection.UP) {
                outline = UP_OUTLINE_SHAPES.get(facing);
            }
            if (outline == null || !rotatedDirection.containsKey(direction) || !outline.containsKey(rotatedDirection.get(direction))) {
                continue;
            }
            voxelShape = VoxelShapes.union(voxelShape, outline.get(rotatedDirection.get(direction)));
        }

        return voxelShape;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.outlineShapes.get(state.with(POWER, 0));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction[] placementDirections = ctx.getPlacementDirections();

        BlockState placeState = this.dotState;
        for (Direction direction : placementDirections) {
            placeState = placeState.with(FACING, direction.getOpposite());

            if (placeState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                break;
            }
        }

        return this.makeLineConnection(ctx.getWorld(), placeState, ctx.getBlockPos());
    }

    private BlockState makeLineConnection(BlockView world, BlockState state, BlockPos pos) {
        boolean bl = isNotConnected(state);
        state = getDefaultState().with(POWER, state.get(POWER)).with(FACING, state.get(FACING));
        state = this.connect(world, state, pos);
        if (!bl || !isNotConnected(state)) {
            boolean northConnection = state.get(WIRE_CONNECTION_NORTH).isConnected();
            boolean southConnection = state.get(WIRE_CONNECTION_SOUTH).isConnected();
            boolean eastConnection = state.get(WIRE_CONNECTION_EAST).isConnected();
            boolean westConnection = state.get(WIRE_CONNECTION_WEST).isConnected();
            boolean eastWestLine = !northConnection && !southConnection;
            boolean northSouthLine = !eastConnection && !westConnection;
            if (!westConnection && eastWestLine) {
                state = state.with(WIRE_CONNECTION_WEST, WireConnection.SIDE);
            }
            if (!eastConnection && eastWestLine) {
                state = state.with(WIRE_CONNECTION_EAST, WireConnection.SIDE);
            }
            if (!northConnection && northSouthLine) {
                state = state.with(WIRE_CONNECTION_NORTH, WireConnection.SIDE);
            }
            if (!southConnection && northSouthLine) {
                state = state.with(WIRE_CONNECTION_SOUTH, WireConnection.SIDE);
            }
        }
        return state;
    }

    private BlockState connect(BlockView world, BlockState state, BlockPos pos) {
        Direction facing = state.get(FACING);
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(facing);

        boolean isNotSolid = !world.getBlockState(pos.offset(facing)).isSolidBlock(world, pos);

        for (Direction direction : Direction.values()) {
            if (!map.containsKey(direction)) {
                continue;
            }
            if (!state.get(map.get(direction)).isConnected()) {
                WireConnection wireConnection = this.getRenderConnectionType(world, pos, state, direction, isNotSolid);
                state = state.with(map.get(direction), wireConnection);
            }
        }

        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        Direction facing = state.get(FACING);
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(facing);

        if (direction == facing.getOpposite()) {
            return state;
        } else if (direction == facing) {
            return this.makeLineConnection(world, state, pos);
        } else {
            WireConnection wireConnection = this.getRenderConnectionType(world, pos, state, direction);
            return !map.containsKey(direction) && wireConnection.isConnected() == state.get(map.get(direction)).isConnected() && !isFullyConnected(state) ? state.with(map.get(direction), wireConnection) : this.makeLineConnection(world, this.dotState.with(POWER, state.get(POWER)).with(map.get(direction), wireConnection).with(FACING, state.get(FACING)), pos);
        }
    }

    private static boolean isFullyConnected(BlockState state) {
        return state.get(WIRE_CONNECTION_NORTH).isConnected() && state.get(WIRE_CONNECTION_SOUTH).isConnected() && state.get(WIRE_CONNECTION_EAST).isConnected() && state.get(WIRE_CONNECTION_WEST).isConnected();
    }

    private static boolean isNotConnected(BlockState state) {
        return !state.get(WIRE_CONNECTION_NORTH).isConnected() && !state.get(WIRE_CONNECTION_SOUTH).isConnected() && !state.get(WIRE_CONNECTION_EAST).isConnected() && !state.get(WIRE_CONNECTION_WEST).isConnected();
    }

    @Override
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        Direction facing = state.get(FACING);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(facing);

        for (Direction direction : Direction.values()) {
            if (!map.containsKey(direction)) {
                continue;
            }
            WireConnection wireConnection = state.get(map.get(direction));
            if (wireConnection != WireConnection.NONE && !world.getBlockState(mutable.set(pos, direction)).isOf(this)) {
                mutable.move(facing.getOpposite());
                BlockState blockState = world.getBlockState(mutable);
                if (!blockState.isOf(Blocks.OBSERVER)) {
                    BlockPos blockPos = mutable.offset(direction.getOpposite());
                    BlockState blockState2 = blockState.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockPos), world, mutable, blockPos);
                    replace(blockState, blockState2, world, mutable, flags, maxUpdateDepth);
                }

                mutable.set(pos, direction).move(facing);
                BlockState blockState3 = world.getBlockState(mutable);
                if (!blockState3.isOf(Blocks.OBSERVER)) {
                    BlockPos blockPos2 = mutable.offset(direction.getOpposite());
                    BlockState blockState4 = blockState3.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockPos2), world, mutable, blockPos2);
                    replace(blockState3, blockState4, world, mutable, flags, maxUpdateDepth);
                }
            }
        }

    }

    private WireConnection getRenderConnectionType(BlockView blockView, BlockPos blockPos, BlockState originalState, Direction direction) {
        Direction facing = blockView.getBlockState(blockPos).get(FACING);
        return this.getRenderConnectionType(blockView, blockPos, originalState, direction, !blockView.getBlockState(blockPos.offset(facing)).isSolidBlock(blockView, blockPos));
    }

    private WireConnection getRenderConnectionType(BlockView blockView, BlockPos blockPos, BlockState originalState, Direction direction, boolean isNotSolid) {
        BlockPos wallPos = blockPos.offset(direction);
        BlockState wallBlockState = blockView.getBlockState(wallPos);
        if (isNotSolid) {
            boolean canRunOnTop = this.canRunOnTop(blockView, originalState, wallPos, wallBlockState);
            if (canRunOnTop && connectsTo(originalState, blockView.getBlockState(wallPos.offset(originalState.get(FACING))))) {
                if (wallBlockState.isSideSolidFullSquare(blockView, wallPos, direction.getOpposite())) {
                    return WireConnection.UP;
                }
                return WireConnection.SIDE;
            }
        }
        return !connectsTo(originalState, wallBlockState, direction) && (wallBlockState.isSolidBlock(blockView, wallPos) || !connectsTo(originalState, blockView.getBlockState(wallPos.offset(originalState.get(FACING).getOpposite())))) ? WireConnection.NONE : WireConnection.SIDE;
    }

    private boolean canRunOnTop(BlockView world, BlockState originalState, BlockPos wallPos, BlockState floor) {
        return floor.isSideSolidFullSquare(world, wallPos, originalState.get(FACING)) || floor.isOf(Blocks.HOPPER);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos floorPos = pos.offset(direction.getOpposite());
        BlockState floorBlockState = world.getBlockState(floorPos);
        return floorBlockState.isSideSolidFullSquare(world, pos, direction) || floorBlockState.getBlock() == Blocks.HOPPER;
    }

    private void update(World world, BlockPos pos, BlockState state) {
        int i = this.getReceivedRedstonePower(world, pos);
        if (state.get(POWER) != i) {
            if (world.getBlockState(pos) == state) {
                world.setBlockState(pos, state.with(POWER, i), 2);
            }

            Set<BlockPos> set = Sets.newHashSet();
            set.add(pos);
            for (Direction direction : Direction.values()) {
                set.add(pos.offset(direction));
            }

            for (BlockPos blockPos : set) {
                world.updateNeighborsAlways(blockPos, this);
            }
        }

    }

    private int getReceivedRedstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = world.getReceivedRedstonePower(pos);
        this.wiresGivePower = true;
        int j = 0;
        if (i < 15) {
            for (Direction direction : Direction.values()) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increasePower(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increasePower(world.getBlockState(blockPos.up())));
                } else if (!blockState.isSolidBlock(world, blockPos)) {
                    j = Math.max(j, this.increasePower(world.getBlockState(blockPos.down())));
                }
            }
            return Math.max(i, j - 1);
        }
        return i;
    }

    private int increasePower(BlockState state) {
        return state.isOf(this) ? state.get(POWER) : 0;
    }

    private void updateNeighbors(World world, BlockPos pos) {
        if (world.getBlockState(pos).isOf(this)) {
            world.updateNeighborsAlways(pos, this);
            for (Direction direction : Direction.values()) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }

        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock()) && !world.isClient) {
            this.update(world, pos, state);

            for (Direction direction : Direction.values()) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }

            this.method_27844(world, pos);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, false);
            if (!world.isClient) {
                for (Direction direction : Direction.values()) {
                    world.updateNeighborsAlways(pos.offset(direction), this);
                }

                this.update(world, pos, state);
                this.method_27844(world, pos);
            }
        }
    }

    private void method_27844(World world, BlockPos pos) {

        for (Direction direction : Direction.values()) {
            this.updateNeighbors(world, pos.offset(direction));
        }

        for (Direction direction : Direction.values()) {
            BlockPos blockPos = pos.offset(direction);
            if (world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                this.updateNeighbors(world, blockPos.up());
            } else {
                this.updateNeighbors(world, blockPos.down());
            }
        }

    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            if (state.canPlaceAt(world, pos)) {
                this.update(world, pos, state);
            } else {
                dropStacks(state, world, pos);
                world.removeBlock(pos, false);
            }

        }
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return !this.wiresGivePower ? 0 : state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        Direction facing = state.get(FACING);
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(facing);
        if (this.wiresGivePower && direction != facing.getOpposite()) {
            int i = state.get(POWER);
            if (i == 0) {
                return 0;
            } else {
                return direction != facing && !this.makeLineConnection(world, state, pos).get(map.get(direction.getOpposite())).isConnected() ? 0 : i;
            }
        } else {
            return 0;
        }
    }

    protected static boolean connectsTo(BlockState originalState, BlockState testState) {
        return connectsTo(originalState, testState, null);
    }

    protected static boolean connectsTo(BlockState originalState, BlockState testState, @Nullable Direction dir) {
        if (testState.isOf(BlockRegistry.get("sticky_redstone"))) {
            return testState.get(FACING) == originalState.get(FACING);
        } else if (testState.isOf(Blocks.REPEATER)) {
            Direction direction = testState.get(RepeaterBlock.FACING);
            return direction == dir || direction.getOpposite() == dir;
        } else if (testState.isOf(Blocks.OBSERVER)) {
            return dir == testState.get(ObserverBlock.FACING);
        } else {
            return testState.emitsRedstonePower() && dir != null && (!testState.contains(FACING) || testState.get(FACING) == originalState.get(FACING));
        }
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return this.wiresGivePower;
    }

    @Environment(EnvType.CLIENT)
    private void addParticles(World world, Random random, BlockPos pos, Vector3f vector3f, Direction direction, Direction direction2, float f, float g) {
        float h = g - f;
        if (!(random.nextFloat() >= 0.2F * h)) {
            float j = f + h * random.nextFloat();
            double d = 0.5D + (double)(0.4375F * (float)direction.getOffsetX()) + (double)(j * (float)direction2.getOffsetX());
            double e = 0.5D + (double)(0.4375F * (float)direction.getOffsetY()) + (double)(j * (float)direction2.getOffsetY());
            double k = 0.5D + (double)(0.4375F * (float)direction.getOffsetZ()) + (double)(j * (float)direction2.getOffsetZ());
            world.addParticle(new DustParticleEffect(vector3f.getX(), vector3f.getY(), vector3f.getZ(), 1.0F), (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + k, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Direction facing = state.get(FACING);
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(facing);
        int power = state.get(POWER);
        if (power != 0) {
            for (Direction direction : Direction.values()) {
                if (!map.containsKey(direction)) {
                    continue;
                }
                WireConnection wireConnection = state.get(map.get(direction));
                switch (wireConnection) {
                    case UP:
                        this.addParticles(world, random, pos, COLORS[power], direction, facing, -0.5F, 0.5F);
                    case SIDE:
                        this.addParticles(world, random, pos, COLORS[power], facing.getOpposite(), direction, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        this.addParticles(world, random, pos, COLORS[power], facing.getOpposite(), direction, 0.0F, 0.3F);
                }
            }

        }
    }
    @Environment(EnvType.CLIENT)
    public static int getWireColor(int powerLevel) {
        Vector3f vector3f = COLORS[powerLevel];
        return MathHelper.packRgb(vector3f.getX(), vector3f.getY(), vector3f.getZ());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        switch(rotation) {
            case CLOCKWISE_180:
                return state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_WEST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_EAST));
            case COUNTERCLOCKWISE_90:
                return state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_WEST)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_NORTH));
            case CLOCKWISE_90:
                return state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_WEST)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_SOUTH));
            default:
                return state;
        }
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        switch(mirror) {
            case LEFT_RIGHT:
                return state.with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_NORTH));
            case FRONT_BACK:
                return state.with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_WEST)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_EAST));
            default:
                return super.mirror(state, mirror);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WIRE_CONNECTION_NORTH, WIRE_CONNECTION_EAST, WIRE_CONNECTION_SOUTH, WIRE_CONNECTION_WEST, POWER, FACING);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.abilities.allowModifyWorld) {
            if (isFullyConnected(state) || isNotConnected(state)) {
                BlockState blockState = isFullyConnected(state) ? this.getDefaultState() : this.dotState;
                blockState = blockState.with(POWER, state.get(POWER)).with(FACING, state.get(FACING));
                /*
                HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(state.get(FACING));
                for (Direction direction : Direction.values()) {
                    if (!(map.containsKey(direction))) {
                        continue;
                    }
                    EnumProperty<WireConnection> dir = map.get(direction);
                    if (state.get(dir) == WireConnection.UP) {
                        blockState = blockState.with(dir, WireConnection.UP);
                    }
                }
                 */
                blockState = this.makeLineConnection(world, blockState, pos);
                if (blockState != state) {
                    world.setBlockState(pos, blockState, 3);
                    this.updateNeighbors(world, pos, state, blockState);
                    return ActionResult.SUCCESS;
                }
            }

        }
        return ActionResult.PASS;
    }

    private void updateNeighbors(World world, BlockPos pos, BlockState blockState, BlockState blockState2) {
        Direction facing = blockState.get(FACING);
        HashMap<Direction, EnumProperty<WireConnection>> map = DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(facing);
        for (Direction direction : Direction.values()) {
            if (!map.containsKey(direction)) {
                continue;
            }
            BlockPos blockPos = pos.offset(direction);
            if (blockState.get(map.get(direction)).isConnected() != blockState2.get(map.get(direction)).isConnected() && world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                world.updateNeighborsExcept(blockPos, blockState2.getBlock(), direction.getOpposite());
            }
        }

    }


    static {
        WIRE_CONNECTION_NORTH = Properties.NORTH_WIRE_CONNECTION;
        WIRE_CONNECTION_EAST = Properties.EAST_WIRE_CONNECTION;
        WIRE_CONNECTION_SOUTH = Properties.SOUTH_WIRE_CONNECTION;
        WIRE_CONNECTION_WEST = Properties.WEST_WIRE_CONNECTION;
        POWER = Properties.POWER;
        DIRECTION_TO_WIRE_CONNECTION_PROPERTY = new HashMap<Direction, HashMap<Direction, EnumProperty<WireConnection>>>(){{
            put(Direction.UP, new HashMap<Direction, EnumProperty<WireConnection>>(){{
                put(Direction.NORTH, WIRE_CONNECTION_NORTH);
                put(Direction.EAST, WIRE_CONNECTION_EAST);
                put(Direction.WEST, WIRE_CONNECTION_WEST);
                put(Direction.SOUTH, WIRE_CONNECTION_SOUTH);
            }});
            put(Direction.DOWN, new HashMap<Direction, EnumProperty<WireConnection>>(){{
                put(Direction.NORTH, WIRE_CONNECTION_NORTH);
                put(Direction.EAST, WIRE_CONNECTION_EAST);
                put(Direction.WEST, WIRE_CONNECTION_WEST);
                put(Direction.SOUTH, WIRE_CONNECTION_SOUTH);
            }});
            put(Direction.NORTH, new HashMap<Direction, EnumProperty<WireConnection>>(){{
                put(Direction.UP, WIRE_CONNECTION_NORTH);
                put(Direction.EAST, WIRE_CONNECTION_WEST);
                put(Direction.WEST, WIRE_CONNECTION_EAST);
                put(Direction.DOWN, WIRE_CONNECTION_SOUTH);
            }});
            put(Direction.EAST, new HashMap<Direction, EnumProperty<WireConnection>>(){{
                put(Direction.UP, WIRE_CONNECTION_NORTH);
                put(Direction.NORTH, WIRE_CONNECTION_EAST);
                put(Direction.SOUTH, WIRE_CONNECTION_WEST);
                put(Direction.DOWN, WIRE_CONNECTION_SOUTH);
            }});
            put(Direction.SOUTH, new HashMap<Direction, EnumProperty<WireConnection>>(){{
                put(Direction.UP, WIRE_CONNECTION_NORTH);
                put(Direction.EAST, WIRE_CONNECTION_EAST);
                put(Direction.WEST, WIRE_CONNECTION_WEST);
                put(Direction.DOWN, WIRE_CONNECTION_SOUTH);
            }});
            put(Direction.WEST, new HashMap<Direction, EnumProperty<WireConnection>>(){{
                put(Direction.UP, WIRE_CONNECTION_NORTH);
                put(Direction.SOUTH, WIRE_CONNECTION_EAST);
                put(Direction.NORTH, WIRE_CONNECTION_WEST);
                put(Direction.DOWN, WIRE_CONNECTION_SOUTH);
            }});
        }};
        ROTATED_DIRECTIONS = new HashMap<Direction, HashMap<Direction, Direction>>(){{
            put(Direction.UP, new HashMap<Direction, Direction>(){{
                put(Direction.NORTH, Direction.NORTH);
                put(Direction.EAST, Direction.EAST);
                put(Direction.WEST, Direction.WEST);
                put(Direction.SOUTH, Direction.SOUTH);
            }});
            put(Direction.DOWN, new HashMap<Direction, Direction>(){{
                put(Direction.NORTH, Direction.NORTH);
                put(Direction.EAST, Direction.EAST);
                put(Direction.WEST, Direction.WEST);
                put(Direction.SOUTH, Direction.SOUTH);
            }});
            put(Direction.NORTH, new HashMap<Direction, Direction>(){{
                put(Direction.UP, Direction.NORTH);
                put(Direction.EAST, Direction.WEST);
                put(Direction.WEST, Direction.EAST);
                put(Direction.DOWN, Direction.SOUTH);
            }});
            put(Direction.EAST, new HashMap<Direction, Direction>(){{
                put(Direction.UP, Direction.NORTH);
                put(Direction.NORTH, Direction.EAST);
                put(Direction.SOUTH, Direction.WEST);
                put(Direction.DOWN, Direction.SOUTH);
            }});
            put(Direction.SOUTH, new HashMap<Direction, Direction>(){{
                put(Direction.UP, Direction.NORTH);
                put(Direction.EAST, Direction.EAST);
                put(Direction.WEST, Direction.WEST);
                put(Direction.DOWN, Direction.SOUTH);
            }});
            put(Direction.WEST, new HashMap<Direction, Direction>(){{
                put(Direction.UP, Direction.NORTH);
                put(Direction.SOUTH, Direction.EAST);
                put(Direction.NORTH, Direction.WEST);
                put(Direction.DOWN, Direction.SOUTH);
            }});
        }};
        DOT_SHAPES = new HashMap<Direction, VoxelShape>() {{
            put(Direction.UP, Block.createCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D));
            put(Direction.DOWN, Block.createCuboidShape(3.0D, 15.0D, 3.0D, 13.0D, 16.0D, 13.0D));
            put(Direction.NORTH, Block.createCuboidShape(3.0D, 3.0D, 15.0D, 13.0D, 13.0D, 16.0D));
            put(Direction.SOUTH, Block.createCuboidShape(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 1.0D));
            put(Direction.WEST, Block.createCuboidShape(15.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D));
            put(Direction.EAST, Block.createCuboidShape(0.0D, 3.0D, 3.0D, 1.0D, 13.0D, 13.0D));
        }};
        SIDE_OUTLINE_SHAPES = new HashMap<Direction, HashMap<Direction, VoxelShape>>(){{
            put(Direction.UP, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D));
                put(Direction.SOUTH, Block.createCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D));
                put(Direction.EAST, Block.createCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D));
                put(Direction.WEST, Block.createCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D));
            }});
            put(Direction.DOWN, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, Block.createCuboidShape(3.0D, 15.0D, 0.0D, 13.0D, 16.0D, 13.0D));
                put(Direction.SOUTH, Block.createCuboidShape(3.0D, 15.0D, 3.0D, 13.0D, 16.0D, 16.0D));
                put(Direction.EAST, Block.createCuboidShape(3.0D, 15.0D, 3.0D, 16.0D, 16.0D, 13.0D));
                put(Direction.WEST, Block.createCuboidShape(0.0D, 15.0D, 3.0D, 13.0D, 16.0D, 13.0D));
            }});
            put(Direction.NORTH, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, Block.createCuboidShape(3.0D, 3.0D, 15.0D, 13.0D, 16.0D, 16.0D));
                put(Direction.SOUTH, Block.createCuboidShape(3.0D, 0.0D, 15.0D, 13.0D, 13.0D, 16.0D));
                put(Direction.EAST, Block.createCuboidShape(0.0D, 3.0D, 15.0D, 13.0D, 13.0D, 16.0D));
                put(Direction.WEST, Block.createCuboidShape(3.0D, 3.0D, 15.0D, 16.0D, 13.0D, 16.0D));
            }});
            put(Direction.SOUTH, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, Block.createCuboidShape(3.0D, 3.0D, 0.0D, 13.0D, 16.0D, 1.0D));
                put(Direction.SOUTH, Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 13.0D, 1.0D));
                put(Direction.EAST, Block.createCuboidShape(3.0D, 3.0D, 0.0D, 16.0D, 13.0D, 1.0D));
                put(Direction.WEST, Block.createCuboidShape(0.0D, 3.0D, 0.0D, 13.0D, 13.0D, 1.0D));
            }});
            put(Direction.WEST, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, Block.createCuboidShape(15.0D, 3.0D, 3.0D, 16.0D, 16.0D, 13.0D));
                put(Direction.SOUTH, Block.createCuboidShape(15.0D, 0.0D, 3.0D, 16.0D, 13.0D, 13.0D));
                put(Direction.EAST, Block.createCuboidShape(15.0D, 3.0D, 3.0D, 16.0D, 13.0D, 16.0D));
                put(Direction.WEST, Block.createCuboidShape(15.0D, 3.0D, 0.0D, 16.0D, 13.0D, 13.0D));
            }});
            put(Direction.EAST, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, Block.createCuboidShape(0.0D, 3.0D, 3.0D, 1.0D, 16.0D, 13.0D));
                put(Direction.SOUTH, Block.createCuboidShape(0.0D, 0.0D, 3.0D, 1.0D, 13.0D, 13.0D));
                put(Direction.WEST, Block.createCuboidShape(0.0D, 3.0D, 3.0D, 1.0D, 13.0D, 16.0D));
                put(Direction.EAST, Block.createCuboidShape(0.0D, 3.0D, 0.0D, 1.0D, 13.0D, 13.0D));
            }});
        }};

        UP_OUTLINE_SHAPES = new HashMap<Direction, HashMap<Direction, VoxelShape>>(){{
            put(Direction.UP, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.UP).get(Direction.NORTH), Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)));
                put(Direction.SOUTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.UP).get(Direction.SOUTH), Block.createCuboidShape(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)));
                put(Direction.EAST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.UP).get(Direction.EAST), Block.createCuboidShape(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)));
                put(Direction.WEST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.UP).get(Direction.WEST), Block.createCuboidShape(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D)));
            }});
            put(Direction.DOWN, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.DOWN).get(Direction.NORTH), Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)));
                put(Direction.SOUTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.DOWN).get(Direction.SOUTH), Block.createCuboidShape(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)));
                put(Direction.EAST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.DOWN).get(Direction.EAST), Block.createCuboidShape(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)));
                put(Direction.WEST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.DOWN).get(Direction.WEST), Block.createCuboidShape(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D)));
            }});
            put(Direction.NORTH, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.NORTH).get(Direction.NORTH), Block.createCuboidShape(3.0D, 15.0D, 0.0D, 13.0D, 16.0D, 16.0D)));
                put(Direction.SOUTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.NORTH).get(Direction.SOUTH), Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 16.0D)));
                put(Direction.WEST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.NORTH).get(Direction.WEST), Block.createCuboidShape(15.0D, 3.0D, 0.0D, 16.0D, 13.0D, 16.0D)));
                put(Direction.EAST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.NORTH).get(Direction.EAST), Block.createCuboidShape(0.0D, 3.0D, 0.0D, 1.0D, 13.0D, 16.0D)));
            }});
            put(Direction.EAST, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.EAST).get(Direction.NORTH), Block.createCuboidShape(0.0D, 15.0D, 3.0D, 16.0D, 16.0D, 13.0D)));
                put(Direction.SOUTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.EAST).get(Direction.SOUTH), Block.createCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D)));
                put(Direction.EAST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.EAST).get(Direction.EAST), Block.createCuboidShape(0.0D, 3.0D, 0.0D, 16.0D, 13.0D, 1.0D)));
                put(Direction.WEST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.EAST).get(Direction.WEST), Block.createCuboidShape(0.0D, 3.0D, 15.0D, 16.0D, 13.0D, 16.0D)));
            }});
            put(Direction.SOUTH, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.SOUTH).get(Direction.NORTH), Block.createCuboidShape(3.0D, 15.0D, 0.0D, 13.0D, 16.0D, 16.0D)));
                put(Direction.SOUTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.SOUTH).get(Direction.SOUTH), Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 16.0D)));
                put(Direction.EAST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.SOUTH).get(Direction.EAST), Block.createCuboidShape(15.0D, 3.0D, 0.0D, 16.0D, 13.0D, 16.0D)));
                put(Direction.WEST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.SOUTH).get(Direction.WEST), Block.createCuboidShape(0.0D, 3.0D, 0.0D, 1.0D, 13.0D, 16.0D)));
            }});
            put(Direction.WEST, new HashMap<Direction, VoxelShape>(){{
                put(Direction.NORTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.WEST).get(Direction.NORTH), Block.createCuboidShape(0.0D, 15.0D, 3.0D, 16.0D, 16.0D, 13.0D)));
                put(Direction.SOUTH, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.WEST).get(Direction.SOUTH), Block.createCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D)));
                put(Direction.WEST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.WEST).get(Direction.WEST), Block.createCuboidShape(0.0D, 3.0D, 0.0D, 16.0D, 13.0D, 1.0D)));
                put(Direction.EAST, VoxelShapes.union(SIDE_OUTLINE_SHAPES.get(Direction.WEST).get(Direction.EAST), Block.createCuboidShape(0.0D, 3.0D, 15.0D, 16.0D, 13.0D, 16.0D)));
            }});
        }};

        COLORS = new Vector3f[16];

        float[] darkSlime = new float[]{52F/255F, 83F/255F, 47F/255F};
        float[] lightSlime = new float[]{201F/255F, 254F/255F, 206F/255F};
        float[] difference = new float[]{
            Math.abs(lightSlime[0] - darkSlime[0]),
            Math.abs(lightSlime[1] - darkSlime[1]),
            Math.abs(lightSlime[2] - darkSlime[2])
        };

        for(int i = 0; i < 16; ++i) {
            float percent = i / 15.0F;
            float R = darkSlime[0] + difference[0] * percent;
            float G = darkSlime[1] + difference[1] * percent;
            float B = darkSlime[2] + difference[2] * percent;
            COLORS[i] = new Vector3f(R, G, B);
        }

    }
}
