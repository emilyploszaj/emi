package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphicsExtractor.class)
public class DrawContextMixin {

	@Inject(at = @At("HEAD"), method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
	private void drawItemTooltip(Font text, ItemStack stack, int x, int y, CallbackInfo info) {
		EmiScreenManager.lastStackTooltipRendered = stack;
	}
}
