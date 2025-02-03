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
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {

	@Shadow
	public abstract boolean isOpen();

	@Shadow
	protected abstract void setOpen(boolean opened);
	
	@Inject(at = @At("HEAD"), method = "toggleOpen", cancellable = true)
	public void toggleOpen(CallbackInfo info) {
		if (EmiConfig.recipeBookAction == RecipeBookAction.DEFAULT) {
			return;
		} else if (EmiConfig.recipeBookAction == RecipeBookAction.TOGGLE_CRAFTABLES) {
			EmiScreenManager.toggleSidebarType(SidebarType.CRAFTABLES);
		} else if (EmiConfig.recipeBookAction == RecipeBookAction.TOGGLE_VISIBILITY) {
			EmiScreenManager.toggleVisibility(false);
		}
		if (isOpen()) {
			setOpen(false);
		}
		info.cancel();
	}
}
