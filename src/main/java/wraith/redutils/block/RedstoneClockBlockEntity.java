package wraith.redutils.block;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import wraith.redutils.RedUtils;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.screen.RedstoneClockScreenHandler;

public class RedstoneClockBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable, Tickable {

    private int tickrate = 1;
    private int timer = 0;

    public RedstoneClockBlockEntity() {
        super(BlockEntityRegistry.get("redstone_clock"));
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.tickrate = tag.getInt("tickrate");
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.tickrate = tag.getInt("tickrate");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("tickrate", this.tickrate);
        return super.toTag(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("tickrate", this.tickrate);
        return tag;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickrate", this.tickrate);
        buf.writeCompoundTag(tag);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + RedUtils.MOD_ID + ".redstone_clock");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new RedstoneClockScreenHandler(syncId, inv, this);
    }

    @Override
    public void tick() {
        if (tickrate != 0) {
            timer = (timer + 1) % tickrate;
            BlockPos offset = pos.offset(getCachedState().get(RedstoneClockBlock.FACING).getOpposite());
            world.updateNeighbor(offset, getCachedState().getBlock(), pos);
        }
    }

    public int getTickrate() {
        return this.tickrate;
    }

    public void setTickrate(int tickrate) {
        this.tickrate = tickrate;
    }

    public double getTickPercent() {
        return (float)(timer + 1) / (float)this.tickrate;
    }

    public int getTimer() {
        return this.timer;
    }

}
