package dev.emi.emi.data;

import java.util.Comparator;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;

public class EmiRecipeCategoryProperties {
	public int order;
	public Comparator<EmiRecipe> sort;
	
	public static int getOrder(EmiRecipeCategory category) {
		EmiRecipeCategoryProperties props = EmiData.categoryPriorities.get(category.getId().toString());
		if (props != null) {
			return props.order;
		}
		return 0;
	}
	
	public static Comparator<EmiRecipe> getSort(EmiRecipeCategory category) {
		EmiRecipeCategoryProperties props = EmiData.categoryPriorities.get(category.getId().toString());
		if (props != null && props.sort != null) {
			return props.sort;
		}
		return category.getSort();
	}
}
