package wraith.redutils.block;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import wraith.redutils.InventoryImpl;
import wraith.redutils.RedUtils;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.screen.BlockBreakerScreenHandler;
import wraith.redutils.screen.BlockPlacerScreenHandler;

public class BlockPlacerBlockEntity extends BlockEntity implements InventoryImpl, NamedScreenHandlerFactory, BlockEntityClientSerializable {

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public BlockPlacerBlockEntity() {
        super(BlockEntityRegistry.get("block_placer"));
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + RedUtils.MOD_ID + ".block_placer");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BlockPlacerScreenHandler(syncId, inv, this);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        this.items.clear();
        super.fromTag(state, tag);
        Inventories.fromTag(tag, this.items);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        Inventories.toTag(tag, this.items);
        return super.toTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.items.clear();
        Inventories.fromTag(tag, this.items);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        Inventories.toTag(tag, this.items);
        return tag;
    }

}
