package wraith.redutils.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import wraith.redutils.Utils;

public class PlayerDetectorScreen extends HandledScreen<ScreenHandler> {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/player_detector.png");
    private boolean ignoreTypedCharacter;
    private TextFieldWidget nameInputField;
    private Button addEntityButton = new Button(137, 22, 16, 16, 207, 0);
    protected float scrollAmount;
    protected boolean mouseClicked;
    protected int scrollOffset;

    public PlayerDetectorScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 198;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.nameInputField = new TextFieldWidget(this.textRenderer, this.x + 23, this.y + 22, 110, 16, new TranslatableText("redutils.player_detector.rename"));
        this.nameInputField.setMaxLength(128);
        this.nameInputField.setEditableColor(0x00AE00);
        this.nameInputField.setVisible(true);
        this.nameInputField.setFocusUnlocked(true);
        this.nameInputField.setText("");
        this.children.add(this.nameInputField);
    }

    @Override
    public void tick() {
        super.tick();
        this.nameInputField.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int k = (int) (41.0F * this.scrollAmount);
        this.drawTexture(matrices, x + 140, y + 44 + k, 176 + (this.shouldScroll() ? 0 : 12), 0, 12, 15);
        int l = this.x + 36;
        int m = this.y + 43;
        int n = this.scrollOffset + 3;
        this.renderForgetButtons(matrices, mouseX, mouseY, x + 25, y + 49);
        this.renderAddButton(matrices, mouseX, mouseY);
        this.renderPlayerBackground(matrices, mouseX, mouseY, l, m, n);
        this.nameInputField.render(matrices, mouseX, mouseY, delta);
        this.renderPlayerTooltip(matrices, mouseX, mouseY, l, m, n);
    }

    private void renderAddButton(MatrixStack matrices, int mouseX, int mouseY) {
        int v = 0;
        if (addEntityButton.isInBounds(mouseX - this.x, mouseY - this.y)) {
            v = mouseClicked ? 16 : 32;
        }
        this.drawTexture(matrices, x + 137, y + 22, 207, v, 16, 16);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else {
            return this.nameInputField.charTyped(chr, keyCode);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (InputUtil.fromKeyCode(keyCode, scanCode).method_30103().isPresent() && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
        } else {
            if (this.nameInputField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            } else {
                return this.nameInputField.isFocused() && this.nameInputField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        super.onMouseClick(slot, invSlot, clickData, actionType);
        this.nameInputField.setCursorToEnd();
        this.nameInputField.setSelectionEnd(0);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.nameInputField.getText();
        this.init(client, width, height);
        this.nameInputField.setText(string);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mouseClicked = true;
        if (this.addEntityButton.isInBounds((int) mouseX - this.x, (int) mouseY - this.y)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ((PlayerDetectorScreenHandler) handler).addPlayername(this.nameInputField.getText());
            PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
            CompoundTag tag = new CompoundTag();
            tag.putString("playername", this.nameInputField.getText());
            data.writeCompoundTag(tag);
            ClientPlayNetworking.send(Utils.ID("player_detector.rename"), data);
            this.nameInputField.setText("");
        }
        if (this.hasPlayernames()) {
            int i1 = this.x + 25;
            int j1 = this.y + 49;
            int k = this.scrollOffset + 3;

            int n = getPlayernamesCount();
            for (int l = this.scrollOffset; l < k; ++l) {
                int m = l - this.scrollOffset;
                double x1 = mouseX - (double) (i1);
                double y1 = mouseY - (double) (j1 + m * 18);

                if (m < n && x1 >= 0.0D && y1 >= 0.0D && x1 < 7 && y1 < 7 && (this.handler).onButtonClick(this.client.player, l + 1)) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, 1.0F));
                    this.scrollOffset = Math.max(0, this.scrollOffset - 1);
                    this.client.interactionManager.clickButton((this.handler).syncId, l + 1);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            this.scrollAmount = (float) ((double) this.scrollAmount - amount / (double) i);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) i) + 0.5D);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int i3 = this.x + 140;
        int j3 = this.y + 35;
        if (!(mouseX >= (double)i3 && mouseX < (double)(i3 + 12) && mouseY >= (double)j3 && mouseY < (double)(j3 + 54))) {
            return false;
        }
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 14;
            int j = i + 54;
            this.scrollAmount = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5D);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    protected void renderForgetButtons(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y) {
        int n = getPlayernamesCount();
        for (int i = 0; i < 3; ++i) {
            int r = y + i * 18;
            int v = 0;
            if (i >= n) {
                v = 7;
            }
            else if (mouseX >= x && mouseY >= r && mouseX < x + 7 && mouseY < r + 7) {
                if (mouseClicked) {
                    v += 7;
                } else {
                    v += 14;
                }
            }
            this.drawTexture(matrixStack, x, r, 200, v, 7, 7);
        }
    }

    protected void renderPlayerBackground(MatrixStack matrixStack, int mouseX, int mouseY, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < getPlayernamesCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= k && mouseY >= r && mouseX < k + 101 && mouseY < r + 18) {
                s += 36;
            }
            this.drawTexture(matrixStack, k, r - 1, 0, s, 101, 18);
        }
    }

    protected void renderPlayerTooltip(MatrixStack matrixStack, int mouseX, int mouseY, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < getPlayernamesCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= k && mouseY >= r && mouseX < k + 101 && mouseY < r + 18) {
                s += 36;
            }
            String entityName = ((PlayerDetectorScreenHandler)handler).getPlayernames().get(n);
            String trimmedEntityName = this.textRenderer.trimToWidth(entityName, 95);
            this.textRenderer.draw(matrixStack, trimmedEntityName, k + 5f, r - 1 + 5f, 0x161616);
            if (s > this.backgroundHeight) {
                this.renderTooltip(matrixStack, Text.of(entityName), mouseX, mouseY);
            }
        }
    }

    protected boolean hasPlayernames() {
        return getPlayernamesCount() > 0;
    }

    protected boolean shouldScroll() {
        return getPlayernamesCount() > 3;
    }

    protected int getMaxScroll() {
        return getPlayernamesCount() - 3;
    }

    protected int getPlayernamesCount() {
        return ((PlayerDetectorScreenHandler)handler).getPlayernames().size();
    }

}
