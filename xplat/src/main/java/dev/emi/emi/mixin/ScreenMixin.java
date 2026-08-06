package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(at = @At("RETURN"), method = "init(II)V")
	private void init(int width, int height, CallbackInfo ci) {
		if ((Object) this instanceof HandledScreen<?> hs && MinecraftClient.getInstance().currentScreen == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}

	@Inject(at = @At("RETURN"), method = "resize")
	private void resize(int width, int height, CallbackInfo ci) {
		if ((Object) this instanceof HandledScreen<?> hs && MinecraftClient.getInstance().currentScreen == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}
}
