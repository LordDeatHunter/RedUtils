package wraith.redutils.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import wraith.redutils.Utils;

public class ItemUserScreen extends HandledScreen<ScreenHandler> {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/item_user.png");

    public ItemUserScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 172;
        this.backgroundWidth = 176;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
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
        int v = ((ItemUserScreenHandler)this.handler).shouldUseOnBlock() ? 9 : 0;
        this.drawTexture(matrices, this.x + 82, this.y + 69, this.backgroundWidth, v, 12, 9);
        if (this.x + 82 <= mouseX && this.y + 69 <= mouseY && mouseX <= this.x + 93 && mouseY <= this.y + 77) {
            this.renderTooltip(matrices, new TranslatableText("redutils.item_user.use_on_block"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.x + 82 <= mouseX && this.y + 69 <= mouseY && mouseX <= this.x + 93 && mouseY <= this.y + 77) {
            this.handler.onButtonClick(this.client.player, 0);
            this.client.interactionManager.clickButton(this.handler.syncId, 0);
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
