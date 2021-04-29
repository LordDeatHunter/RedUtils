package wraith.redutils.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.redutils.Utils;

import java.util.ArrayList;

public class RedstoneClockScreen extends HandledScreen<ScreenHandler> {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/redstone_clock.png");
    private ArrayList<Button> buttons = new ArrayList<>();
    private boolean mouseClicked = false;

    public RedstoneClockScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 160;
        this.backgroundWidth = 176;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.titleX += 52;

        buttons.add(new Button(61, 19, 13, 13, 176, 0));
        buttons.add(new Button(102, 19, 13, 13, 189, 0));

        buttons.add(new Button(61, 46, 13, 13, 176, 0));
        buttons.add(new Button(102, 46, 13, 13, 189, 0));
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

        for (Button button : buttons) {
            int v = button.getV();
            if (button.isInBounds(mouseX - this.x, mouseY - this.y)) {
                v = button.getHeight() * (mouseClicked ? 1 : 2);
            }
            this.drawTexture(matrices, this.x + button.getX(), this. y + button.getY(), button.getU(), v, button.getWidth(), button.getWidth());
        }

        String tickrate = String.valueOf(((RedstoneClockScreenHandler)handler).getTickrate());
        this.textRenderer.draw(matrices, new LiteralText(tickrate), this.x + 80, this.y + 22, 4210752);
        String ticktime = String.valueOf(((RedstoneClockScreenHandler)handler).getTicktime());
        this.textRenderer.draw(matrices, new LiteralText(ticktime), this.x + 80, this.y + 49, 4210752);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        this.mouseClicked = true;
        for (int i = 0; i < buttons.size(); ++i) {
            Button button = buttons.get(i);
            if (button.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.handler.onButtonClick(null, i);
                this.client.interactionManager.clickButton(this.handler.syncId, i);
            }
        }
        return super.mouseClicked(mouseX, mouseY, buttonId);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int val = -1;
        if (amount > 0) {
            val += 2;
        } else if (amount < 0) {
            ++val;
        } else {
            return false;
        }

        if (!(mouseX >= this.x + 58 && mouseY >= this.y + 16 && mouseX <= this.x + 117 && mouseY <= this.y + 34)) {
            return false;
        }

        this.handler.onButtonClick(null, val);
        this.client.interactionManager.clickButton(this.handler.syncId, val);

        return super.mouseScrolled(mouseX, mouseY, amount);
    }


}
