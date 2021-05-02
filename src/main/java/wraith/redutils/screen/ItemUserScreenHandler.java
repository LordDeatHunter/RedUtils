package wraith.redutils.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import wraith.redutils.block.ItemUserBlockEntity;
import wraith.redutils.registry.CustomScreenHandlerRegistry;
import wraith.redutils.registry.ItemRegistry;

public class ItemUserScreenHandler extends ScreenHandler {

    private Inventory inventory;
    private ItemUserBlockEntity entity;
    private boolean useOnBlock = false;

    public ItemUserScreenHandler(int syncId, PlayerInventory inv, ItemUserBlockEntity entity) {
        this(syncId, inv, null, entity);
        this.entity = entity;
    }

    public ItemUserScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        this(syncId, inv, buf, new SimpleInventory(1));
    }

    public ItemUserScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf, Inventory inventory) {
        super(CustomScreenHandlerRegistry.get("item_user"), syncId);
        this.entity = null;
        this.inventory = inventory;

        if (buf != null) {
            CompoundTag tag = buf.readCompoundTag();
            this.useOnBlock = tag.getBoolean("use_on_block");
        }

        this.addSlot(new Slot(inventory, 0, 80, 35));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 90 + y * 18));
            }
        }
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inv, x, 8 + x * 18, 148));
        }

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            int invSize = this.inventory.size();
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < invSize) {
                if (!this.insertItem(originalStack, invSize, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, invSize, false)) {
                if (invSlot >= invSize && invSlot < this.slots.size() - 9) {
                    if (!this.insertItem(originalStack, this.slots.size() - 9, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (invSlot >= this.slots.size() - 9) {
                    if (!this.insertItem(originalStack, invSize, this.slots.size() - 9, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
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
    public boolean onButtonClick(PlayerEntity player, int id) {
        this.useOnBlock = !this.useOnBlock;
        if (this.entity != null) {
            this.entity.toggleUseOnBlock();
            this.entity.markDirty();
            this.entity.sync();
        }
        return super.onButtonClick(player, id);
    }

    public boolean shouldUseOnBlock() {
        return this.useOnBlock;
    }

}
