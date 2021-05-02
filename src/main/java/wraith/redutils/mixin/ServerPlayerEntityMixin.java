package wraith.redutils.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.redutils.RedUtils;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "openHandledScreen", at = @At("HEAD"), cancellable = true)
    public void openHandledScreen(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir) {
        Text name = ((PlayerEntity)(Object)this).getCustomName();
        if (name != null && RedUtils.REDUTILS_PLAYERS.contains(name.asString())) {
            cir.setReturnValue(OptionalInt.empty());
            cir.cancel();
        }
    }

}
