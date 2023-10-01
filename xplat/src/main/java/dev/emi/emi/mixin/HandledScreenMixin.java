package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiPort;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreen;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen implements EmiScreen {
	@Shadow
	protected int backgroundWidth, backgroundHeight, x, y;

	private HandledScreenMixin() { super(null); }

	@Dynamic
	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screen/Screen.renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V",
			shift = Shift.AFTER),
		method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V")
	private void renderBackground(DrawContext raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		EmiScreenManager.drawBackground(context, mouseX, mouseY, delta);
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screen/ingame/HandledScreen.drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V",
			shift = Shift.AFTER),
		method = "render")
	private void renderForeground(DrawContext raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (EmiAgnos.isForge()) {
			return;
		}
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		context.matrices().translate(-x, -y, 0.0);
		EmiPort.setPositionTexShader();
		EmiScreenManager.render(context, mouseX, mouseY, delta);
		EmiScreenManager.drawForeground(context, mouseX, mouseY, delta);
		context.pop();
	}

	@Override
	public int emi$getLeft() {
		if (this instanceof RecipeBookProvider provider) {
			if (provider.getRecipeBookWidget().isOpen()) {
				return x - 177;
			}
		}
		return x;
	}

	@Override
	public int emi$getRight() {
		return x + backgroundWidth;
	}

	@Override
	public int emi$getTop() {
		return y;
	}

	@Override
	public int emi$getBottom() {
		return y + backgroundHeight;
	}
}