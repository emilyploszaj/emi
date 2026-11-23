package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Window;

import dev.emi.emi.runtime.EmiScreenshotRecorder;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Redirect(at = @At(value = "INVOKE", target = "net/minecraft/client/gui/GuiGraphicsExtractor.applyCursor(Lcom/mojang/blaze3d/platform/Window;)V"),
		method = "extractGui")
	private void extractGuiEnd(GuiGraphicsExtractor graphics, Window window) {
		EmiScreenshotRecorder.extract(graphics);
		graphics.applyCursor(window);
	}

	@Inject(at = @At(value = "INVOKE", target = "net/minecraft/client/gui/render/GuiRenderer.render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
		shift = Shift.AFTER), method = "render")
	private void renderGui(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo info) {
		EmiScreenshotRecorder.capture();
	}
}
