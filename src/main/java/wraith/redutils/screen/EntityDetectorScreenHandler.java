package wraith.redutils.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;
import wraith.redutils.block.EntityDetectorBlockEntity;
import wraith.redutils.mixin.SlotAccessor;
import wraith.redutils.registry.CustomScreenHandlerRegistry;

import java.util.ArrayList;
import java.util.Collections;

public class EntityDetectorScreenHandler extends ScreenHandler {

    private EntityDetectorBlockEntity entity;
    private ArrayList<String> entities = null;
    private int xRadius = 0;
    private int yRadius = 0;
    private int zRadius = 0;

    public EntityDetectorScreenHandler(int syncId, PlayerInventory inv, EntityDetectorBlockEntity entity) {
        this(syncId, inv, (PacketByteBuf) null);
        this.entity = entity;
        this.entities = entity.getEntities();
        this.xRadius = entity.getxRadius();
        this.yRadius = entity.getyRadius();
        this.zRadius = entity.getzRadius();
    }

    public EntityDetectorScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.get("entity_detector"), syncId);
        this.entity = null;

        if (buf != null) {
            CompoundTag tag = buf.readCompoundTag();
            this.xRadius = tag.getInt("xRadius");
            this.yRadius = tag.getInt("yRadius");
            this.zRadius = tag.getInt("zRadius");
            entities = new ArrayList<>();
            for (Tag entity : tag.getList("entity_list", 8)) {
                entities.add(entity.asString());
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

    public ArrayList<String> getEntities() {
        return this.entities;
    }

    public void addEntity(String entityID) {
        if (!entityID.contains(":")) {
            entityID = "minecraft:" + entityID;
        }
        if (!this.entities.contains(entityID)) {
            this.entities.add(entityID);
        }
        Collections.sort(entities);
        if (entity == null) {
            return;
        }
        entity.addEntity(entityID);
        entity.markDirty();
        entity.sync();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id <= 0) {
            return false;
        }
        if (id <= 2) {
            setPageInventory(id - 1);
        }
        else if (id <= 8) {
            id -= 3;
            switch (id / 2) {
                case 0:
                    this.xRadius = MathHelper.clamp(this.xRadius + (id % 2 == 0 ? -1 : 1), 1, 8);
                    break;
                case 1:
                    this.yRadius = MathHelper.clamp(this.yRadius + (id % 2 == 0 ? -1 : 1), 1, 8);
                    break;
                case 2:
                    this.zRadius = MathHelper.clamp(this.zRadius + (id % 2 == 0 ? -1 : 1), 1, 8);
                    break;
            }
            if (this.entity != null) {
                this.entity.setxRadius(this.xRadius);
                this.entity.setyRadius(this.yRadius);
                this.entity.setzRadius(this.zRadius);
                this.entity.markDirty();
                this.entity.sync();
            }
            return true;
        } else {
            id -= 9;
            if (id < this.entities.size()) {
                this.entities.remove(id);
            }
            if (entity != null) {
                entity.removeEntity(id);
                entity.markDirty();
                entity.sync();
            }
        }
        return true;
    }

    public int getxRadius() {
        return xRadius;
    }

    public int getyRadius() {
        return yRadius;
    }

    public int getzRadius() {
        return zRadius;
    }
}
