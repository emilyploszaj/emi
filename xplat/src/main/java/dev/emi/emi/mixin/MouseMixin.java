package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(Mouse.class)
public class MouseMixin {
	@Shadow @Final
	private MinecraftClient client;
	@Shadow
	private double x, y;
	@Shadow
	private int activeButton = -1;

	@Inject(at = @At(value = "INVOKE", ordinal = 0, target =
			"net/minecraft/client/gui/screen/Screen.wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"),
		method = "onMouseButton(JIII)V", cancellable = true)
	private void onMouseDown(long window, int button, int action, int mods, CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof HandledScreen<?> hs) {
				double mx = this.x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
				double my = this.y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
				if (EmiScreenManager.mouseClicked(mx, my, button)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse press");
			e.printStackTrace();
		}
	}

	@Inject(at = @At(value = "INVOKE", ordinal = 1, target =
			"net/minecraft/client/gui/screen/Screen.wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"),
		method = "onMouseButton(JIII)V", cancellable = true)
	private void onMouseUp(long window, int button, int action, int mods, CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof HandledScreen<?> hs) {
				double mx = this.x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
				double my = this.y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
				if (EmiScreenManager.mouseReleased(mx, my, button)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse release");
			e.printStackTrace();
		}
	}

	@Inject(at = @At(value = "INVOKE", ordinal = 1, target =
			"net/minecraft/client/gui/screen/Screen.wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"),
		method = "onCursorPos(JDD)V", cancellable = true)
	private void onMouseDragged(long window, double x, double y, CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof HandledScreen<?> hs) {
				double mx = this.x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
				double my = this.y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
				double dx = (x - this.x) * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
				double dy = (y - this.y) * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
				EmiScreenManager.mouseDragged(mx, my, activeButton, dx, dy);
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse drag");
			e.printStackTrace();
		}
	}

	@Inject(at = @At(value = "INVOKE", target =
			"net/minecraft/client/gui/screen/Screen.mouseScrolled(DDDD)Z"),
		method = "onMouseScroll(JDD)V", cancellable = true)
	private void onMouseScrolled(long window, double horizontal, double vertical, CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof HandledScreen<?> hs) {
				double amount = (client.options.getDiscreteMouseScroll().getValue() ? Math.signum(vertical) : vertical) * client.options.getMouseWheelSensitivity().getValue();
				double mx = x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
				double my = y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
				if (EmiScreenManager.mouseScrolled(mx, my, amount)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse scroll");
			e.printStackTrace();
		}
	}
}
