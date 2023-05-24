package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.mixinsupport.annotation.Transform;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(EmiApi.class)
public class EmiApiMixin {

	@Transform(visibility = "PUBLIC")
	private static boolean performFill(EmiRecipe recipe, EmiFillAction action, int amount) {
		HandledScreen<?> hs = EmiApi.getHandledScreen();
		if (hs != null) {
			return EmiRecipeFiller.performFill(recipe, hs, action, amount);
		}
		return false;
	}
}
