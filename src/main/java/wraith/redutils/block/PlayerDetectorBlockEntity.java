package wraith.redutils.block;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import org.jetbrains.annotations.Nullable;
import wraith.redutils.RedUtils;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.screen.PlayerDetectorScreenHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerDetectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable, Tickable {

    private ArrayList<String> playernames = new ArrayList<>();
    private int timer = 0;
    private int cooldown = 10;

    public PlayerDetectorBlockEntity() {
        super(BlockEntityRegistry.get("player_detector"));
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + RedUtils.MOD_ID + ".player_detector");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new PlayerDetectorScreenHandler(syncId, inv, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (String entity : playernames) {
            list.add(StringTag.of(entity));
        }
        tag.put("player_list", list);
        buf.writeCompoundTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        playernames.clear();
        for (Tag entity : tag.getList("player_list", 8)) {
            playernames.add(entity.asString());
        }
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String entity : playernames) {
            list.add(StringTag.of(entity));
        }
        tag.put("player_list", list);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        playernames.clear();
        for (Tag entity : tag.getList("player_list", 8)) {
            playernames.add(entity.asString());
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String entity : playernames) {
            list.add(StringTag.of(entity));
        }
        tag.put("player_list", list);
        return super.toTag(tag);
    }

    public void setPlayernames(ArrayList<String> playernames) {
        this.playernames = playernames;
    }

    public ArrayList<String> getPlayernames() {
        return this.playernames;
    }

    public void addPlayername(String playername) {
        if (!this.playernames.contains(playername)) {
            this.playernames.add(playername);
            Collections.sort(this.playernames);
        }
    }

    public void removePlayername(int i) {
        if (i < this.playernames.size()) {
            this.playernames.remove(i);
        }
    }

    @Override
    public void tick() {
        timer = (timer + 1) % cooldown;
        if (timer != 0) {
            return;
        }
        if (world == null) {
            return;
        }
        List<? extends PlayerEntity> players = world.getPlayers();
        boolean inRange = false;
        for (PlayerEntity player : players) {
            if (playernames.contains(player.getName().asString())) {
                inRange = true;
                break;
            }
        }
        if (inRange && !getCachedState().get(PlayerDetectorBlock.POWERED)) {
            world.setBlockState(pos, getCachedState().with(PlayerDetectorBlock.POWERED, true));
        } else if (!inRange && getCachedState().get(PlayerDetectorBlock.POWERED)) {
            world.setBlockState(pos, getCachedState().with(PlayerDetectorBlock.POWERED, false));
        }
    }

}
