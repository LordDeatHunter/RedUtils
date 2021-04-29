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
import wraith.redutils.block.LaunchPadBlockEntity;
import wraith.redutils.registry.CustomScreenHandlerRegistry;

public class LaunchPadScreenHandler extends ScreenHandler {

    private float xAmount = 0;
    private float yAmount = 0;
    private float zAmount = 0;
    private LaunchPadBlockEntity entity;

    public LaunchPadScreenHandler(int syncId, PlayerInventory inv, LaunchPadBlockEntity entity) {
        this(syncId, inv, (PacketByteBuf) null);
        this.xAmount = entity.getxAmount();
        this.yAmount = entity.getyAmount();
        this.zAmount = entity.getzAmount();
        this.entity = entity;
    }

    public LaunchPadScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.get("launch_pad"), syncId);

        if (buf != null) {
            CompoundTag tag = buf.readCompoundTag();
            this.xAmount = tag.getFloat("xAmount");
            this.yAmount = tag.getFloat("yAmount");
            this.zAmount = tag.getFloat("zAmount");
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
                this.xAmount = MathHelper.clamp(this.xAmount + (id % 2 == 0 ? -1 : 1), -64, 64);
                break;
            case 1:
                this.yAmount = MathHelper.clamp(this.yAmount + (id % 2 == 0 ? -1 : 1), -16, 16);
                break;
            case 2:
                this.zAmount = MathHelper.clamp(this.zAmount + (id % 2 == 0 ? -1 : 1), -64, 64);
                break;
            default:
                RedUtils.LOGGER.warn("How did you do this?");
        }
        if (this.entity != null) {
            this.entity.setxAmount(this.xAmount);
            this.entity.setyAmount(this.yAmount);
            this.entity.setzAmount(this.zAmount);
            this.entity.markDirty();
            this.entity.sync();
        }
        return super.onButtonClick(player, id);
    }

    public float getxAmount() {
        return this.xAmount;
    }

    public float getyAmount() {
        return this.yAmount;
    }

    public float getzAmount() {
        return this.zAmount;
    }

}
