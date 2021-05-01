package wraith.redutils.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import wraith.redutils.block.PlayerDetectorBlockEntity;
import wraith.redutils.mixin.SlotAccessor;
import wraith.redutils.registry.CustomScreenHandlerRegistry;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerDetectorScreenHandler extends ScreenHandler {

    private PlayerDetectorBlockEntity entity;
    private ArrayList<String> playernames = null;

    public PlayerDetectorScreenHandler(int syncId, PlayerInventory inv, PlayerDetectorBlockEntity entity) {
        this(syncId, inv, (PacketByteBuf) null);
        this.entity = entity;
        this.playernames = entity.getPlayernames();
    }

    public PlayerDetectorScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.get("player_detector"), syncId);
        this.entity = null;

        if (buf != null) {
            CompoundTag tag = buf.readCompoundTag();
            playernames = new ArrayList<>();
            for (Tag entity : tag.getList("player_list", 8)) {
                playernames.add(entity.asString());
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 116 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 174));
        }
    }

    private void setPageInventory(int page) {
        for (int i = 0; i < slots.size(); ++i) {
            Slot oldSlot = slots.get(i);
            Slot newSlot = new Slot(oldSlot.inventory, ((SlotAccessor)oldSlot).getIndex(), oldSlot.x, oldSlot.y + (page == 0 ? -16 : 16));
            newSlot.id = oldSlot.id;
            this.slots.set(i, newSlot);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
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

    public ArrayList<String> getPlayernames() {
        return this.playernames;
    }

    public void addPlayername(String entityID) {
        if (!this.playernames.contains(entityID)) {
            this.playernames.add(entityID);
        }
        Collections.sort(playernames);
        if (entity == null) {
            return;
        }
        entity.addPlayername(entityID);
        entity.markDirty();
        entity.sync();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id <= 0) {
            return false;
        }
        --id;
        if (id < this.playernames.size()) {
            this.playernames.remove(id);
        }
        if (entity != null) {
            entity.removePlayername(id);
            entity.markDirty();
            entity.sync();
        }
        return true;
    }

}
