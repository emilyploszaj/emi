package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {
	@Shadow
	private static int selectedTab;
	@Shadow
	protected abstract boolean hasScrollbar();

	@Inject(at = @At("HEAD"), method = "mouseScrolled", cancellable = true)
	private void mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> info) {
		if (EmiScreenManager.mouseScrolled(mouseX, mouseY, amount)) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
		if (selectedTab == ItemGroup.SEARCH.getIndex()) {
			return;
		}
		if (EmiScreenManager.keyPressed(keyCode, scanCode, modifiers)) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "charTyped", cancellable = true)
	private void charTyped(char chr, int modifiers, CallbackInfoReturnable<Boolean> info) {
		if (EmiScreenManager.search.charTyped(chr, modifiers)) {
			info.setReturnValue(true);
		}
	}
}
