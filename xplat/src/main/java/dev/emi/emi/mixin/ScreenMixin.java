package dev.emi.emi.mixin;

import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.platform.EmiAgnos;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(at = @At("RETURN"), method = "init(II)V")
	private void init(int width, int height, CallbackInfo info) {
		Minecraft client = Minecraft.getInstance();
		if ((Object) this instanceof AbstractContainerScreen hs && client.screen == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}

	@Inject(at = @At("RETURN"), method = "resize(II)V")
	private void resize(int width, int height, CallbackInfo info) {
		Minecraft client = Minecraft.getInstance();
		if ((Object) this instanceof AbstractContainerScreen hs && client.screen == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}

	// Dragged stacks and tooltips go above everything the screen draws, including deferred tooltips
	@Inject(at = @At("RETURN"), method = "extractRenderStateWithTooltipAndSubtitles")
	private void extractForeground(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if ((Object) this instanceof AbstractContainerScreen && !EmiAgnos.isForge()) {
			raw.nextStratum();
			EmiScreenManager.drawForeground(EmiDrawContext.wrap(raw), mouseX, mouseY, delta);
		}
	}

	@Inject(at = @At(value = "INVOKE",
			target = "net/minecraft/client/gui/screens/Screen.extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
			shift = Shift.AFTER),
		method = "extractRenderStateWithTooltipAndSubtitles")
	private void extractBackground(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if ((Object) this instanceof AbstractContainerScreen) {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			EmiScreenManager.drawBackground(context, mouseX, mouseY, delta);
		}
	}
}
