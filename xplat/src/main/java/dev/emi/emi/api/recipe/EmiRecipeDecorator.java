package dev.emi.emi.api.recipe;

import org.jetbrains.annotations.ApiStatus;

import dev.emi.emi.api.widget.WidgetHolder;

@ApiStatus.Experimental
public interface EmiRecipeDecorator {

	void decorateRecipe(EmiRecipe recipe, WidgetHolder widgets);
}
