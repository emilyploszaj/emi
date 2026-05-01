package dev.emi.emi.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.jspecify.annotations.Nullable;
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
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.Window;

@Mixin(Mouse.class)
public abstract class MouseMixin {
	@Shadow @Final
	private MinecraftClient client;
	@Shadow
	private double x, y;
    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;
    @Shadow
    private @Nullable MouseInput activeButton;

    @Shadow
    public abstract double getScaledX(Window window);

    @Shadow
    public abstract double getScaledY(Window window);

    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z"),
            method = "onMouseButton", cancellable = true)
	private void onMouseDown(long window, MouseInput input, int action, CallbackInfo info, @Local(ordinal = 0) Click click, @Local(ordinal = 1) boolean bl2) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof HandledScreen<?>) {
				if (EmiScreenManager.mouseClicked(click, bl2)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse press", e);
		}
	}

    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(Lnet/minecraft/client/gui/Click;)Z"),
            method = "onMouseButton", cancellable = true)
	private void onMouseUp(long window, MouseInput input, int action, CallbackInfo info, @Local(ordinal = 0) Click click) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof HandledScreen<?>) {
				if (EmiScreenManager.mouseReleased(click)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse release", e);
		}
	}

    @Inject(at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/screen/Screen;mouseDragged(Lnet/minecraft/client/gui/Click;DD)Z"),
            method = "tick", cancellable = true)
	private void onMouseDragged(CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof HandledScreen<?>) {
                Window window = this.client.getWindow();
                Click click = new Click(this.getScaledX(window), this.getScaledY(window), this.activeButton);
                double dx = Mouse.scaleX(window, this.cursorDeltaX);
                double dy = Mouse.scaleY(window, this.cursorDeltaY);
				EmiScreenManager.mouseDragged(click, dx, dy);
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse drag", e);
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
			EmiLog.error("Error while handling mouse scroll", e);
		}
	}
}
