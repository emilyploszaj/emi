package dev.emi.emi.api.render;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.screen.tooltip.RecipeCostTooltipComponent;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public class EmiTooltipComponents {

	/**
	 * @return A tooltip component that displays a provided recipe.
	 */
	public static TooltipComponent getRecipeTooltipComponent(EmiRecipe recipe) {
		return new RecipeTooltipComponent(recipe);
	}

	/**
	 * @return A tooltip component that displays the remainder of a provided ingredient.
	 */
	public static TooltipComponent getRemainderTooltipComponent(EmiIngredient ingredient) {
		return new RemainderTooltipComponent(ingredient);
	}

	/**
	 * @return A tooltip component that displays the the cost breakdown of a provided recipe.
	 */
	public static TooltipComponent getRecipeCostTooltipComponent(EmiRecipe recipe) {
		return new RecipeCostTooltipComponent(recipe);
	}
}
