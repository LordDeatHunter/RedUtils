package wraith.redutils.block;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import wraith.redutils.RedUtils;
import wraith.redutils.Utils;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.screen.ItemCollectorScreenHandler;

import java.util.List;

public class ItemCollectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable, Tickable {

    private int xRadius = 3;
    private int yRadius = 3;
    private int zRadius = 3;
    private int timer = 0;
    private int cooldown = 10;

    public ItemCollectorBlockEntity() {
        super(BlockEntityRegistry.get("item_collector"));
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.xRadius = tag.getInt("xRadius");
        this.yRadius = tag.getInt("yRadius");
        this.zRadius = tag.getInt("zRadius");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("xRadius", this.xRadius);
        tag.putInt("yRadius", this.yRadius);
        tag.putInt("zRadius", this.zRadius);
        return super.toTag(tag);
    }

    public int getxRadius() {
        return xRadius;
    }

    public void setxRadius(int xRadius) {
        this.xRadius = xRadius;
    }

    public int getyRadius() {
        return yRadius;
    }

    public void setyRadius(int yRadius) {
        this.yRadius = yRadius;
    }

    public int getzRadius() {
        return zRadius;
    }

    public void setzRadius(int zRadius) {
        this.zRadius = zRadius;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("xRadius", xRadius);
        tag.putInt("yRadius", yRadius);
        tag.putInt("zRadius", zRadius);
        buf.writeCompoundTag(tag);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + RedUtils.MOD_ID + ".item_collector");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ItemCollectorScreenHandler(syncId, inv, this);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.xRadius = tag.getInt("xRadius");
        this.yRadius = tag.getInt("yRadius");
        this.zRadius = tag.getInt("zRadius");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("xRadius", this.xRadius);
        tag.putInt("yRadius", this.yRadius);
        tag.putInt("zRadius", this.zRadius);
        return tag;
    }

    @Override
    public void tick() {
        timer = (timer + 1) % cooldown;
        if (timer != 0) {
            return;
        }
        if (world == null) {
            return;
        }
        Direction facing = getCachedState().get(ItemCollectorBlock.FACING);
        BlockPos storagePos = pos.offset(facing.getOpposite());
        Inventory storage = HopperBlockEntity.getInventoryAt(world, storagePos);
        if (storage == null) {
            return;
        }

        Box box = new Box(pos.add(-xRadius, -yRadius, -zRadius), pos.add(xRadius, yRadius, zRadius));
        List<ItemEntity> items = world.getEntitiesByType(EntityType.ITEM, box, (entity) -> true);

        for (ItemEntity item : items) {
            if (!Utils.isInventoryFull(storage, facing)) {
                ItemStack stack = item.getStack();
                Utils.transferItem(stack, storage);
                if (stack.isEmpty()) {
                    item.remove();
                } else {
                    item.setStack(stack);
                }
            } else {
                break;
            }
        }

    }

}
