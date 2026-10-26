package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.ToastManager;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
	
	@Inject(at = @At("HEAD"), method = "extractRenderState", cancellable = true)
	private void drawHead(GuiGraphicsExtractor raw, CallbackInfo info) {
		Minecraft client = Minecraft.getInstance();
		if (client.screen != null && EmiConfig.enabled && EmiApi.getHandledScreen() != null) {
			info.cancel();
		}
	}
}
