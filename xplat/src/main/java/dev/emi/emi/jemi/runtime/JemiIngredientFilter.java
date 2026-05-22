package dev.emi.emi.jemi.runtime;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.common.ingredients.IngredientFilterApi;


/**
 * The extension of main work class instead of implementing interface is needed, as JEI do not have build in overwrite
 * functions yet. A lot of rely on implementation class.
 */
public class JemiIngredientFilter extends IngredientFilterApi {

	public JemiIngredientFilter(IngredientFilter ingredientFilter,
		IFilterTextSource filterTextSource) {
		super(ingredientFilter, filterTextSource);
	}


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
		SidebarPanel search = EmiScreenManager.getSearchPanel();
		if (search == null || search.space == null) {
			return List.of();
		}
		return search.space.getStacks().stream()
			.map(i -> JemiUtil.getTyped(i.getEmiStacks().get(0)))
			.filter(Optional::isPresent).map(Optional::get)
			.map(i -> i.getIngredient(ingredientType))
			.filter(Optional::isPresent).map(Optional::get)
			.toList();
	}
}
