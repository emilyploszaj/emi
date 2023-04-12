package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Mixin(Screen.class)
public class ScreenMixin {
	
	@Inject(at = @At("HEAD"), method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V")
	private void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo info) {
		EmiScreenManager.lastStackTooltipRendered = stack;
	}
}
