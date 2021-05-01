package wraith.redutils.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;
import wraith.redutils.RedUtils;
import wraith.redutils.block.ItemCollectorBlockEntity;
import wraith.redutils.registry.CustomScreenHandlerRegistry;

public class ItemCollectorScreenHandler extends ScreenHandler {

    private int xRadius = 0;
    private int yRadius = 0;
    private int zRadius = 0;
    private ItemCollectorBlockEntity entity;

    public ItemCollectorScreenHandler(int syncId, PlayerInventory inv, ItemCollectorBlockEntity entity) {
        this(syncId, inv, (PacketByteBuf) null);
        this.xRadius = entity.getxRadius();
        this.yRadius = entity.getyRadius();
        this.zRadius = entity.getzRadius();
        this.entity = entity;
    }

    public ItemCollectorScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.get("item_collector"), syncId);

        if (buf != null) {
            CompoundTag tag = buf.readCompoundTag();
            this.xRadius = tag.getInt("xRadius");
            this.yRadius = tag.getInt("yRadius");
            this.zRadius = tag.getInt("zRadius");
        }
        this.entity = null;

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 100 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 158));
        }

    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.slots.size() - 9) {
                if (!this.insertItem(originalStack, this.slots.size() - 9, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.insertItem(originalStack, 0, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }

        return newStack;

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        int button = id / 2;
        switch (button) {
            case 0:
                this.xRadius = MathHelper.clamp(this.xRadius + (id % 2 == 0 ? -1 : 1), 1, 8);
                break;
            case 1:
                this.yRadius = MathHelper.clamp(this.yRadius + (id % 2 == 0 ? -1 : 1), 1, 8);
                break;
            case 2:
                this.zRadius = MathHelper.clamp(this.zRadius + (id % 2 == 0 ? -1 : 1), 1, 8);
                break;
            default:
                RedUtils.LOGGER.warn("How did you do this?");
        }
        if (this.entity != null) {
            this.entity.setxRadius(this.xRadius);
            this.entity.setyRadius(this.yRadius);
            this.entity.setzRadius(this.zRadius);
            this.entity.markDirty();
            this.entity.sync();
        }
        return super.onButtonClick(player, id);
    }

    public int getxRadius() {
        return this.xRadius;
    }

    public int getyRadius() {
        return this.yRadius;
    }

    public int getzRadius() {
        return this.zRadius;
    }

}
