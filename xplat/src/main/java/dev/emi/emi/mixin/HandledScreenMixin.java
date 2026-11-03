package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {
	@Shadow
	protected int imageWidth, imageHeight, leftPos, topPos;

	private HandledScreenMixin() { super(null); }

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.extractSlotHighlightFront(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
			shift = Shift.AFTER),
		method = "extractContents")
	private void renderForeground(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (EmiAgnos.isForge()) {
			return;
		}
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		context.translate(-leftPos, -topPos);
		EmiScreenManager.render(context, mouseX, mouseY, delta);
		context.pop();
	}
}