package dev.emi.emi.api;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.recipe.RecipeManager;

public interface EmiRegistry {

	RecipeManager getRecipeManager();
	
	void addCategory(EmiRecipeCategory category);

	void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation);

	void addRecipe(EmiRecipe recipe);

	void addRecipeHandler(EmiRecipeCategory category, EmiRecipeHandler<?> handler);
}
