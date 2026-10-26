package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
	@Shadow @Final
	private Minecraft minecraft;
	@Shadow
	private MouseButtonInfo activeButton;
	@Shadow
	private double accumulatedDX;
	@Shadow
	private double accumulatedDY;

	@Shadow
	public abstract double getScaledXPos(com.mojang.blaze3d.platform.Window window);

	@Shadow
	public abstract double getScaledYPos(com.mojang.blaze3d.platform.Window window);

	@Inject(at = @At(value = "INVOKE", target =
			"net/minecraft/client/gui/screens/Screen.mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"),
		method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", cancellable = true)
	private void onMouseDown(long window, MouseButtonInfo rawButtonInfo, int action, CallbackInfo info) {
		try {
			Screen screen = minecraft.screen;
			if (screen instanceof AbstractContainerScreen<?> hs && activeButton != null) {
				if (EmiScreenManager.mouseClicked(getScaledXPos(minecraft.getWindow()), getScaledYPos(minecraft.getWindow()), activeButton.button())) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse press", e);
		}
	}

	@Inject(at = @At(value = "INVOKE", target =
			"net/minecraft/client/gui/screens/Screen.mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z"),
		method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", cancellable = true)
	private void onMouseUp(long window, MouseButtonInfo rawButtonInfo, int action, CallbackInfo info) {
		try {
			Screen screen = minecraft.screen;
			if (screen instanceof AbstractContainerScreen<?> hs) {
				if (EmiScreenManager.mouseReleased(getScaledXPos(minecraft.getWindow()), getScaledYPos(minecraft.getWindow()), rawButtonInfo.button())) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse release", e);
		}
	}

	@Inject(at = @At(value = "INVOKE", target =
			"net/minecraft/client/gui/screens/Screen.mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"),
		method = "handleAccumulatedMovement")
	private void onMouseDragged(CallbackInfo info) {
		try {
			Screen screen = minecraft.screen;
			if (screen instanceof AbstractContainerScreen<?> hs && activeButton != null) {
				double dx = this.accumulatedDX * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
				double dy = this.accumulatedDY * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
				EmiScreenManager.mouseDragged(getScaledXPos(minecraft.getWindow()), getScaledYPos(minecraft.getWindow()), activeButton.button(), dx, dy);
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse drag", e);
		}
	}

	@Inject(at = @At(value = "INVOKE", target =
			"net/minecraft/client/gui/screens/Screen.mouseScrolled(DDDD)Z"),
		method = "onScroll(JDD)V", cancellable = true)
	private void onMouseScrolled(long window, double horizontal, double vertical, CallbackInfo info) {
		try {
			Screen screen = minecraft.screen;
			if (screen instanceof AbstractContainerScreen<?> hs) {
				double amount = (minecraft.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical) * minecraft.options.mouseWheelSensitivity().get();
				if (EmiScreenManager.mouseScrolled(getScaledXPos(minecraft.getWindow()), getScaledYPos(minecraft.getWindow()), amount)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse scroll", e);
		}
	}
}
