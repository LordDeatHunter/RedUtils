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
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import wraith.redutils.RedUtils;
import wraith.redutils.registry.BlockEntityRegistry;
import wraith.redutils.screen.EntityDetectorScreenHandler;

import java.util.ArrayList;
import java.util.Collections;

public class EntityDetectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable, Tickable {

    private ArrayList<String> entities = new ArrayList<>();
    private int xRadius = 3;
    private int yRadius = 3;
    private int zRadius = 3;
    private int timer = 0;
    private int cooldown = 10;

    public EntityDetectorBlockEntity() {
        super(BlockEntityRegistry.get("entity_detector"));
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container." + RedUtils.MOD_ID + ".entity_detector");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new EntityDetectorScreenHandler(syncId, inv, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("xRadius", xRadius);
        tag.putInt("yRadius", yRadius);
        tag.putInt("zRadius", zRadius);
        ListTag list = new ListTag();
        for (String entity : entities) {
            list.add(StringTag.of(entity));
        }
        tag.put("entity_list", list);
        buf.writeCompoundTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.xRadius = tag.getInt("xRadius");
        this.yRadius = tag.getInt("yRadius");
        this.zRadius = tag.getInt("zRadius");
        entities.clear();
        for (Tag entity : tag.getList("entity_list", 8)) {
            entities.add(entity.asString());
        }
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putInt("xRadius", this.xRadius);
        tag.putInt("yRadius", this.yRadius);
        tag.putInt("zRadius", this.zRadius);
        ListTag list = new ListTag();
        for (String entity : entities) {
            list.add(StringTag.of(entity));
        }
        tag.put("entity_list", list);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.xRadius = tag.getInt("xRadius");
        this.yRadius = tag.getInt("yRadius");
        this.zRadius = tag.getInt("zRadius");
        entities.clear();
        for (Tag entity : tag.getList("entity_list", 8)) {
            entities.add(entity.asString());
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("xRadius", this.xRadius);
        tag.putInt("yRadius", this.yRadius);
        tag.putInt("zRadius", this.zRadius);
        ListTag list = new ListTag();
        for (String entity : entities) {
            list.add(StringTag.of(entity));
        }
        tag.put("entity_list", list);
        return super.toTag(tag);
    }

    public void setEntities(ArrayList<String> entities) {
        this.entities = entities;
    }

    public ArrayList<String> getEntities() {
        return this.entities;
    }

    public void addEntity(String entity) {
        if (!this.entities.contains(entity)) {
            this.entities.add(entity);
            Collections.sort(this.entities);
        }
    }

    public void removeEntity(int i) {
        if (i < this.entities.size()) {
            this.entities.remove(i);
        }
    }

    public int getxRadius() {
        return xRadius;
    }

    public void setxRadius(int xRadius) {
        this.xRadius = xRadius;
    }

    public int getyRadius() {
        return yRadius;
    }

    public void setyRadius(int yRadius) {
        this.yRadius = yRadius;
    }

    public int getzRadius() {
        return zRadius;
    }

    public void setzRadius(int zRadius) {
        this.zRadius = zRadius;
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
        Box box = new Box(pos.getX() - getxRadius(), pos.getY() - getyRadius(), pos.getZ() - getzRadius(), pos.getX() + getxRadius(), pos.getY() + getyRadius(), pos.getZ() + getzRadius());
        boolean inRange = false;
        for (String entityID : this.entities) {
            if (!world.getEntitiesByType(Registry.ENTITY_TYPE.get(new Identifier(entityID)), box, (entity) -> true).isEmpty()) {
                inRange = true;
                break;
            }
        }
        if (inRange && !getCachedState().get(EntityDetectorBlock.POWERED)) {
            world.setBlockState(pos, getCachedState().with(EntityDetectorBlock.POWERED, true));
        } else if (!inRange && getCachedState().get(EntityDetectorBlock.POWERED)) {
            world.setBlockState(pos, getCachedState().with(EntityDetectorBlock.POWERED, false));
        }
    }

}
