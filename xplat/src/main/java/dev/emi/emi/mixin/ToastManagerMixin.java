package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
	
	@Inject(at = @At("HEAD"), method = "draw", cancellable = true)
	private void drawHead(MatrixStack raw, CallbackInfo info) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen != null && EmiConfig.enabled && EmiApi.getHandledScreen() != null) {
			info.cancel();
		}
	}
}
