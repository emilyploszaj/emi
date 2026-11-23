package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.RecipeBookAction;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookWidgetMixin {

	@Shadow
	public abstract boolean isVisible();

	@Shadow
	protected abstract void setVisible(boolean opened);
	
	@Inject(at = @At("HEAD"), method = "toggleVisibility", cancellable = true)
	public void toggleOpen(CallbackInfo info) {
		if (EmiConfig.recipeBookAction == RecipeBookAction.DEFAULT) {
			return;
		} else if (EmiConfig.recipeBookAction == RecipeBookAction.TOGGLE_CRAFTABLES) {
			EmiScreenManager.toggleSidebarType(SidebarType.CRAFTABLES);
		} else if (EmiConfig.recipeBookAction == RecipeBookAction.TOGGLE_VISIBILITY) {
			EmiScreenManager.toggleVisibility(false);
		}
		if (isVisible()) {
			setVisible(false);
		}
		info.cancel();
	}
}
