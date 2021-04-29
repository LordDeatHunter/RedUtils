package wraith.redutils.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import wraith.redutils.RedUtils;
import wraith.redutils.block.RedstoneClockBlockEntity;
import wraith.redutils.registry.CustomScreenHandlerRegistry;

public class RedstoneClockScreenHandler extends ScreenHandler {

    private RedstoneClockBlockEntity entity;
    private int tickrate = 0;

    public RedstoneClockScreenHandler(int syncId, PlayerInventory inv, RedstoneClockBlockEntity entity) {
        this(syncId, inv, (PacketByteBuf) null);
        this.tickrate = entity.getTickrate();
        this.entity = entity;
    }

    public RedstoneClockScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.get("redstone_clock"), syncId);
        this.entity = null;

        if (buf != null) {
            CompoundTag tag = buf.readCompoundTag();
            this.tickrate = tag.getInt("tickrate");
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 51 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 109));
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
        switch (id) {
            case 0:
                this.tickrate = Math.max(1, this.tickrate - 1);
                break;
            case 1:
                ++this.tickrate;
                break;
            default:
                RedUtils.LOGGER.warn("How did you do this?");
        }
        if (this.entity != null) {
            this.entity.setTickrate(this.tickrate);
            this.entity.markDirty();
            this.entity.sync();
        }
        return super.onButtonClick(player, id);
    }

    public int getTickrate() {
        return this.tickrate;
    }

}
