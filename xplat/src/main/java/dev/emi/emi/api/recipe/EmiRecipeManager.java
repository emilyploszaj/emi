package dev.emi.emi.api.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.Identifier;

public interface EmiRecipeManager {
	
	List<EmiRecipeCategory> getCategories();

	List<EmiIngredient> getWorkstations(EmiRecipeCategory category);

	List<EmiRecipe> getRecipes();

	List<EmiRecipe> getRecipes(EmiRecipeCategory category);

	@Nullable EmiRecipe getRecipe(Identifier id);

	List<EmiRecipe> getRecipesByInput(EmiStack stack);

	List<EmiRecipe> getRecipesByOutput(EmiStack stack);
}
