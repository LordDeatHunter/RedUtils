package wraith.redutils.block;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import wraith.redutils.InventoryImpl;
import wraith.redutils.RedUtils;
import wraith.redutils.Utils;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.screen.ItemUserScreenHandler;

public class ItemUserBlockEntity extends BlockEntity implements InventoryImpl, ExtendedScreenHandlerFactory, BlockEntityClientSerializable {

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private ServerPlayerEntity player = null;
    private boolean useOnBlock = false;

    public PlayerEntity getPlayer() {
        if (player == null) {
            setPlacer();
        }
        return player;
    }

    public ItemUserBlockEntity() {
        super(BlockEntityRegistry.get("item_user"));
    }

    public void setPlacer() {
        this.player = Utils.getFakePlayer(world, pos, getCachedState());
        this.player.setCustomName(new LiteralText("redutils:item_user"));
        this.player.setCustomNameVisible(false);
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
        return new TranslatableText("container." + RedUtils.MOD_ID + ".item_user");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ItemUserScreenHandler(syncId, inv, this);
    }
    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        this.items.clear();
        this.useOnBlock = tag.getBoolean("use_on_block");
        super.fromTag(state, tag);
        Inventories.fromTag(tag, this.items);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean("use_on_block", this.useOnBlock);
        Inventories.toTag(tag, this.items);
        return super.toTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.items.clear();
        this.useOnBlock = tag.getBoolean("use_on_block");
        Inventories.fromTag(tag, this.items);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putBoolean("use_on_block", this.useOnBlock);
        Inventories.toTag(tag, this.items);
        return tag;
    }

    public void toggleUseOnBlock() {
        this.useOnBlock = !this.useOnBlock;
    }

    public boolean shouldUseOnBlock() {
        return useOnBlock;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("use_on_block", this.useOnBlock);
        buf.writeCompoundTag(tag);
    }

}
