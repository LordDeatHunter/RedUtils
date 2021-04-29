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
import wraith.redutils.RedUtils;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.screen.LaunchPadScreenHandler;

public class LaunchPadBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable {

    private float xAmount = 0;
    private float yAmount = 1;
    private float zAmount = 0;

    public LaunchPadBlockEntity() {
        super(BlockEntityRegistry.get("launch_pad"));
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.xAmount = tag.getFloat("xAmount");
        this.yAmount = tag.getFloat("yAmount");
        this.zAmount = tag.getFloat("zAmount");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putFloat("xAmount", this.xAmount);
        tag.putFloat("yAmount", this.yAmount);
        tag.putFloat("zAmount", this.zAmount);
        return super.toTag(tag);
    }

    public float getxAmount() {
        return xAmount;
    }

    public void setxAmount(float xAmount) {
        this.xAmount = xAmount;
    }

    public float getyAmount() {
        return yAmount;
    }

    public void setyAmount(float yAmount) {
        this.yAmount = yAmount;
    }

    public float getzAmount() {
        return zAmount;
    }

    public void setzAmount(float zAmount) {
        this.zAmount = zAmount;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("xAmount", xAmount);
        tag.putFloat("yAmount", yAmount);
        tag.putFloat("zAmount", zAmount);
        buf.writeCompoundTag(tag);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + RedUtils.MOD_ID + ".launch_pad");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new LaunchPadScreenHandler(syncId, inv, this);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.xAmount = tag.getFloat("xAmount");
        this.yAmount = tag.getFloat("yAmount");
        this.zAmount = tag.getFloat("zAmount");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putFloat("xAmount", this.xAmount);
        tag.putFloat("yAmount", this.yAmount);
        tag.putFloat("zAmount", this.zAmount);
        return tag;
    }
}
