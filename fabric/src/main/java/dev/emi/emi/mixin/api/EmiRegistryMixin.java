package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.emi.emi.api.EmiRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

@Mixin(EmiRegistry.class)
public interface EmiRegistryMixin {

	@Shadow
	<T extends ScreenHandler> void addRecipeHandler(ScreenHandlerType<T> type, dev.emi.emi.api.recipe.handler.EmiRecipeHandler<T> handler);

	@SuppressWarnings("deprecation")
	default <T extends ScreenHandler> void addRecipeHandler(ScreenHandlerType<T> type, dev.emi.emi.api.EmiRecipeHandler<T> handler) {
		addRecipeHandler(type, (dev.emi.emi.api.recipe.handler.EmiRecipeHandler<T>) handler);
	}
}
