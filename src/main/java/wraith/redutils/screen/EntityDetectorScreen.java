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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import wraith.redutils.Utils;

import java.util.ArrayList;

public class EntityDetectorScreen extends HandledScreen<ScreenHandler> {

    private static final Identifier TEXTURE_0 = Utils.ID("textures/gui/entity_detector_0.png");
    private static final Identifier TEXTURE_1 = Utils.ID("textures/gui/entity_detector_1.png");
    private boolean ignoreTypedCharacter;
    private TextFieldWidget nameInputField;
    private Button addEntityButton = new Button(137, 22, 16, 16, 207, 0);
    protected float scrollAmount;
    protected boolean mouseClicked;
    protected int scrollOffset;
    private int page = 0;
    private Button nextPageButton = new Button(157, 63, 12, 16, 176, 15);
    private Button previousPageButton = new Button(12, 42, 12, 16, 202, 0);
    private ArrayList<Button> secondPageButtons = new ArrayList<>();

    public EntityDetectorScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 198; // 182
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        secondPageButtons.add(new Button(61, 19, 13, 13, 176, 0));
        secondPageButtons.add(new Button(102, 19, 13, 13, 189, 0));

        secondPageButtons.add(new Button(61, 46, 13, 13, 176, 0));
        secondPageButtons.add(new Button(102, 46, 13, 13, 189, 0));

        secondPageButtons.add(new Button(61, 73, 13, 13, 176, 0));
        secondPageButtons.add(new Button(102, 73, 13, 13, 189, 0));
    }

    @Override
    protected void init() {
        super.init();
        this.nameInputField = new TextFieldWidget(this.textRenderer, this.x + 23, this.y + 22, 110, 16, new TranslatableText("redutils.entity_detector.rename"));
        this.nameInputField.setMaxLength(128);
        this.nameInputField.setEditableColor(0xFFFFFF);
        this.nameInputField.setVisible(true);
        this.nameInputField.setFocusUnlocked(true);
        this.nameInputField.setText("");
        this.children.add(this.nameInputField);
    }

    @Override
    public void tick() {
        super.tick();
        if (page != 0) {
            return;
        }
        this.nameInputField.tick();
        Identifier id = Identifier.tryParse(this.nameInputField.getText());
        if (id != null && Registry.ENTITY_TYPE.containsId(id)) {
            this.nameInputField.setEditableColor(0x00AE00);
        } else {
            this.nameInputField.setEditableColor(0xAE0000);
        }
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
        this.client.getTextureManager().bindTexture(page == 0 ? TEXTURE_0 : TEXTURE_1);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (page == 0) {
            int v = nextPageButton.getV();
            if (nextPageButton.isInBounds(mouseX - this.x, mouseY - this.y)) {
                v += nextPageButton.getHeight() * (mouseClicked ? 1 : 2);
            }
            this.drawTexture(matrices, this.x + nextPageButton.getX(), this. y + nextPageButton.getY(), nextPageButton.getU(), v, nextPageButton.getWidth(), nextPageButton.getHeight());
            int k = (int) (41.0F * this.scrollAmount);
            this.drawTexture(matrices, x + 140, y + 44 + k, 176 + (this.shouldScroll() ? 0 : 12), 0, 12, 15);
            int l = this.x + 36;
            int m = this.y + 43;
            int n = this.scrollOffset + 3;
            this.renderForgetButtons(matrices, mouseX, mouseY, x + 25, y + 49);
            this.renderAddButton(matrices, mouseX, mouseY);
            this.renderEntityBackground(matrices, mouseX, mouseY, l, m, n);
            this.nameInputField.render(matrices, mouseX, mouseY, delta);
            this.renderEntityTooltip(matrices, mouseX, mouseY, l, m, n);
        } else {
            int v = previousPageButton.getV();
            if (previousPageButton.isInBounds(mouseX - this.x, mouseY - this.y)) {
                v += previousPageButton.getHeight() * (mouseClicked ? 1 : 2);
            }
            this.drawTexture(matrices, this.x + previousPageButton.getX(), this. y + previousPageButton.getY(), previousPageButton.getU(), v, previousPageButton.getWidth(), previousPageButton.getHeight());
            for (Button button : secondPageButtons) {
                v = button.getV();
                if (button.isInBounds(mouseX - this.x, mouseY - this.y)) {
                    v += button.getHeight() * (mouseClicked ? 1 : 2);
                }
                this.drawTexture(matrices, this.x + button.getX(), this. y + button.getY(), button.getU(), v, button.getWidth(), button.getHeight());
            }
            int xAmount = ((EntityDetectorScreenHandler)handler).getxRadius();
            int yAmount = ((EntityDetectorScreenHandler)handler).getyRadius();
            int zAmount = ((EntityDetectorScreenHandler)handler).getzRadius();

            this.textRenderer.draw(matrices, new LiteralText("X:"), this.x + 50, this.y + 22, 4210752);
            this.textRenderer.draw(matrices, new LiteralText("Y:"), this.x + 50, this.y + 49, 4210752);
            this.textRenderer.draw(matrices, new LiteralText("Z:"), this.x + 50, this.y + 76, 4210752);

            this.textRenderer.draw(matrices, new LiteralText(String.valueOf(xAmount)), this.x + 80, this.y + 22, 4210752);
            this.textRenderer.draw(matrices, new LiteralText(String.valueOf(yAmount)), this.x + 80, this.y + 49, 4210752);
            this.textRenderer.draw(matrices, new LiteralText(String.valueOf(zAmount)), this.x + 80, this.y + 76, 4210752);
        }
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
        if (page != 0) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
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
        if (page == 0) {
            this.ignoreTypedCharacter = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        super.onMouseClick(slot, invSlot, clickData, actionType);
        if (page != 0) {
            return;
        }
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
        if (page == 0) {
            if (this.nextPageButton.isInBounds((int) mouseX - this.x, (int) mouseY - this.y)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.handler.onButtonClick(client.player, 1);
                this.client.interactionManager.clickButton((this.handler).syncId, 1);
                switchPage();
            } else {
                if (this.addEntityButton.isInBounds((int) mouseX - this.x, (int) mouseY - this.y)) {
                    Identifier id = Identifier.tryParse(this.nameInputField.getText());
                    if (id != null && Registry.ENTITY_TYPE.containsId(new Identifier(this.nameInputField.getText()))) {
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        ((EntityDetectorScreenHandler) handler).addEntity(this.nameInputField.getText());
                        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
                        CompoundTag tag = new CompoundTag();
                        tag.putString("entity_id", this.nameInputField.getText());
                        data.writeCompoundTag(tag);
                        ClientPlayNetworking.send(Utils.ID("entity_detector.rename"), data);
                        this.nameInputField.setText("");
                    } else {
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, 1.0F));
                    }
                }
                if (this.hasEntities()) {
                    int i1 = this.x + 25;
                    int j1 = this.y + 49;
                    int k = this.scrollOffset + 3;

                    int n = getEntityCount();
                    for (int l = this.scrollOffset; l < k; ++l) {
                        int m = l - this.scrollOffset;
                        double x1 = mouseX - (double) (i1);
                        double y1 = mouseY - (double) (j1 + m * 18);

                        if (m < n && x1 >= 0.0D && y1 >= 0.0D && x1 < 7 && y1 < 7 && (this.handler).onButtonClick(this.client.player, l + 9)) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, 1.0F));
                            this.scrollOffset = Math.max(0, this.scrollOffset - 1);
                            this.client.interactionManager.clickButton((this.handler).syncId, l + 9);
                            return true;
                        }
                    }
                }
            }
        } else {
            if (this.previousPageButton.isInBounds((int) mouseX - this.x, (int) mouseY - this.y)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.client.interactionManager.clickButton((this.handler).syncId, 2);
                this.handler.onButtonClick(client.player, 2);
                switchPage();
            } else {
                for (int i = 0; i < secondPageButtons.size(); ++i) {
                    Button btn = secondPageButtons.get(i);
                    if (btn.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.handler.onButtonClick(null, i + 3);
                        this.client.interactionManager.clickButton(this.handler.syncId, i + 3);
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void switchPage() {
        this.page = (this.page + 1) % 2;
        if (page == 0) {
            this.backgroundHeight = 198;
        } else {
            this.backgroundHeight = 182;
        }
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (page == 0) {
            if (this.shouldScroll()) {
                int i = this.getMaxScroll();
                this.scrollAmount = (float) ((double) this.scrollAmount - amount / (double) i);
                this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
                this.scrollOffset = (int) ((double) (this.scrollAmount * (float) i) + 0.5D);
            }
        } else {
            int val = 2;
            if (amount > 0) {
                val += 2;
            } else if (amount < 0) {
                ++val;
            } else {
                return false;
            }

            if (mouseX >= this.x + 58 && mouseY >= this.y + 43 && mouseX <= this.x + 117 && mouseY <= this.y + 61) {
                val += 2;
            } else if (mouseX >= this.x + 58 && mouseY >= this.y + 70 && mouseX <= this.x + 117 && mouseY <= this.y + 88) {
                val += 4;
            } else if (!(mouseX >= this.x + 58 && mouseY >= this.y + 16 && mouseX <= this.x + 117 && mouseY <= this.y + 34)) {
                return false;
            }

            this.handler.onButtonClick(null, val);
            this.client.interactionManager.clickButton(this.handler.syncId, val);
            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (page != 0) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
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
        int n = getEntityCount();
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

    protected void renderEntityBackground(MatrixStack matrixStack, int mouseX, int mouseY, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < getEntityCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= k && mouseY >= r && mouseX < k + 101 && mouseY < r + 18) {
                s += 36;
            }
            this.drawTexture(matrixStack, k, r - 1, 0, s, 101, 18);
        }
    }

    protected void renderEntityTooltip(MatrixStack matrixStack, int mouseX, int mouseY, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < getEntityCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= k && mouseY >= r && mouseX < k + 101 && mouseY < r + 18) {
                s += 36;
            }
            String entityName = ((EntityDetectorScreenHandler)handler).getEntities().get(n);
            String trimmedEntityName = this.textRenderer.trimToWidth(entityName, 95);
            this.textRenderer.draw(matrixStack, trimmedEntityName, k + 5f, r - 1 + 5f, 0x161616);
            if (s > this.backgroundHeight) {
                this.renderTooltip(matrixStack, Text.of(entityName), mouseX, mouseY);
            }
        }
    }


    protected boolean hasEntities() {
        return getEntityCount() > 0;
    }

    protected boolean shouldScroll() {
        return getEntityCount() > 3;
    }

    protected int getMaxScroll() {
        return getEntityCount() - 3;
    }

    protected int getEntityCount() {
        return ((EntityDetectorScreenHandler)handler).getEntities().size();
    }

}
