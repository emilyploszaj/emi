package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
	@Shadow @Final
	private Minecraft minecraft;
	
	@Inject(at = @At(value = "INVOKE", target =
			"net/minecraft/client/gui/screens/Screen.wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"),
		method = "keyPress(JIIII)V", cancellable = true)
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
		try {
			Screen screen = minecraft.screen;
			if (screen instanceof AbstractContainerScreen<?> hs) {
				if (action == 1 || action == 2) {
					if (EmiScreenManager.keyPressed(key, scancode, modifiers)) {
						info.cancel();
					}
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling key press", e);
		}
	}
	
	@Inject(at = @At("HEAD"),
		method = "charTyped(JII)V", cancellable = true)
	public void onChar(long window, int codePoint, int modifiers, CallbackInfo info) {
		try {
			if (window == minecraft.getWindow().getWindow()) {
				Screen screen = minecraft.screen;
				if (screen instanceof AbstractContainerScreen<?> hs && this.minecraft.getOverlay() == null) {
					boolean consume = false;
					if (Character.charCount(codePoint) == 1) {
						consume = EmiScreenManager.search.charTyped((char) codePoint, modifiers) || consume;
					} else {
						for (char c : Character.toChars(codePoint)) {
							consume = EmiScreenManager.search.charTyped(c, modifiers) || consume;
						}
					}
					if (consume) {
						info.cancel();
					}
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling char", e);
		}
	}
}
