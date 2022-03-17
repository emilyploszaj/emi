package dev.emi.emi;

import com.google.common.collect.Sets;

import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.RecipeManager;

public class EmiRegistryImpl implements EmiRegistry {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public RecipeManager getRecipeManager() {
		return client.world.getRecipeManager();
	}

	@Override
	public void addCategory(EmiRecipeCategory category) {
		EmiRecipes.addCategory(category);
	}

	@Override
	public void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation) {
		EmiRecipes.addWorkstation(category, workstation);
	}

	@Override
	public void addRecipe(EmiRecipe recipe) {
		EmiRecipes.addRecipe(recipe);
	}
	
	@Override
	public void addRecipeHandler(EmiRecipeCategory category, EmiRecipeHandler<?> handler) {
		EmiRecipeFiller.RECIPE_HANDLERS.computeIfAbsent(category, (c) -> Sets.newHashSet()).add(handler);
	}
}
