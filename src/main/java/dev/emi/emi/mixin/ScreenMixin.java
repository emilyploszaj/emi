package dev.emi.emi.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(Screen.class)
public class ScreenMixin {
	@Shadow
	private int width;

	@Unique
	private int emi$originalWidth;

	@Inject(at = @At("HEAD"), method = "renderTooltipFromComponents")
	private void fakeWidth(MatrixStack matrices, List<TooltipComponent> components, int x, int y, CallbackInfo info) {
		if (EmiClient.shiftTooltipsLeft) {
			this.emi$originalWidth = this.width;
			this.width = x;
		}
	}

	@Inject(at = @At("RETURN"), method = "renderTooltipFromComponents")
	private void restoreWidth(MatrixStack matrices, List<TooltipComponent> components, int x, int y, CallbackInfo info) {
		if (EmiClient.shiftTooltipsLeft) {
			this.width = this.emi$originalWidth;
		}
	}

	/* Old, different solution. Not sure if this is less hacky, or more, but it breaks botania's tooltip components
	@Unique
	private int emi$tooltipLeftMutation;
	
	@Inject(at = @At(value = "INVOKE", target = "net/minecraft/client/util/math/MatrixStack.push()V", shift = At.Shift.BEFORE),
		method = "renderTooltipFromComponents", locals = LocalCapture.CAPTURE_FAILHARD)
	private void getTooltipPosition(MatrixStack matrices, List<TooltipComponent> components, int x, int y,
			CallbackInfo info, int width, int height, int tx, int ty) {
		emi$tooltipLeftMutation = x - width - 16;
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "net/minecraft/client/util/math/MatrixStack.push()V", shift = At.Shift.AFTER),
		method = "renderTooltipFromComponents", ordinal = 4)
	private int adjustTooltipPosition(int original) {
		if (EmiClient.shiftTooltipsLeft) {
			return emi$tooltipLeftMutation;
		} else {
			return original;
		}
	}*/
}
