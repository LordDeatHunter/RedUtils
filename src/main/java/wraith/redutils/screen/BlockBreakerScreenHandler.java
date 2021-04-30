package wraith.redutils.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import wraith.redutils.registry.CustomScreenHandlerRegistry;
import wraith.redutils.registry.ItemRegistry;

public class BlockBreakerScreenHandler extends ScreenHandler {

    private Inventory inventory;

    public BlockBreakerScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, new SimpleInventory(1));
    }

    public BlockBreakerScreenHandler(int syncId, PlayerInventory inv, Inventory inventory) {
        super(CustomScreenHandlerRegistry.get("block_breaker"), syncId);
        this.inventory = inventory;

        this.addSlot(new Slot(inventory, 0, 80, 30){
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof PickaxeItem || stack.getItem() == ItemRegistry.get("pickaxe_upgrade");
            }
        });

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 70 + y * 18));
            }
        }
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inv, x, 8 + x * 18, 128));
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

}
