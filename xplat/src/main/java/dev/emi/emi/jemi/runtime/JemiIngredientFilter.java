package dev.emi.emi.jemi.runtime;

import java.util.List;

import dev.emi.emi.api.EmiApi;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;

public class JemiIngredientFilter implements IIngredientFilter {

	@Override
	public void setFilterText(String filterText) {
		EmiApi.setSearchText(filterText);
	}

	@Override
	public String getFilterText() {
		return EmiApi.getSearchText();
	}

	@Override
	public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
		float f; // TODO
		return List.of();
	}
}
