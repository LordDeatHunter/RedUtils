package wraith.redutils.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
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
import wraith.redutils.screen.BlockBreakerScreenHandler;

public class BlockBreakerBlockEntity extends BlockEntity implements InventoryImpl, NamedScreenHandlerFactory {

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private ServerPlayerEntity player = null;

    public PlayerEntity getPlayer() {
        if (player == null) {
            setPlacer();
        }
        return player;
    }

    public BlockBreakerBlockEntity() {
        super(BlockEntityRegistry.get("block_breaker"));
    }

    public void setPlacer() {
        this.player = Utils.getFakePlayer(world, pos, getCachedState());
        this.player.setCustomName(new LiteralText("redutils:block_breaker"));
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
        return new TranslatableText("container." + RedUtils.MOD_ID + ".block_breaker");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BlockBreakerScreenHandler(syncId, inv, this);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, this.items);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        Inventories.toTag(tag, this.items);
        return super.toTag(tag);
    }

}
