package dev.emi.emi.jemi.runtime;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IRecipesGui;

public class JemiRecipesGui implements IRecipesGui {

	@Override
	public void show(List<IFocus<?>> focuses) {
		for (IFocus<?> focus : focuses) {
		}
	}

	@Override
	public void showTypes(List<RecipeType<?>> recipeTypes) {
		float f; // TODO
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		float f; // TODO
		return Optional.empty();
	}
	
}
