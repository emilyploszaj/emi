package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.Identifier;

public class EmiSyntheticIngredientRecipe extends EmiIngredientRecipe {
	private final EmiIngredient ingredient;

	public EmiSyntheticIngredientRecipe(EmiIngredient ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	protected EmiIngredient getIngredient() {
		return ingredient;
	}

	@Override
	protected List<EmiStack> getStacks() {
		return ingredient.getEmiStacks();
	}

	@Override
	protected EmiRecipe getRecipeContext(EmiStack stack, int offset) {
		return new EmiResolutionRecipe(ingredient, stack);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.INGREDIENT;
	}

	@Override
	public Identifier getId() {
		return null;
	}
}
