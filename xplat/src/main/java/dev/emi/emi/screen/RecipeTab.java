package dev.emi.emi.screen;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.registry.EmiRecipes;

public class RecipeTab {
	private static final int RECIPE_PADDING = 10;
	private final List<RecipeDisplay> displays;
	private final int width;
	private List<List<RecipeDisplay>> pages = Lists.newArrayList();
	public final EmiRecipeCategory category;

	public RecipeTab(EmiRecipeCategory category, List<EmiRecipe> recipes) {
		this.category = category;
		displays = recipes.stream().map(r -> {
			try {
				return new RecipeDisplay(r);
			} catch (Throwable t) {
				return new RecipeDisplay(t);
			}
		}).toList();
		width = displays.stream().map(RecipeDisplay::getWidth).max(Integer::compareTo).orElse(0);
	}

	public List<WidgetGroup> constructWidgets(int page, int x, int y, int backgroundWidth, int backgroundHeight) {
		List<WidgetGroup> groups = Lists.newArrayList();
		int width = backgroundWidth - 16;
		int height = getVerticalRecipeSpace(backgroundHeight);
		int off = 0;
		for (RecipeDisplay display : pages.get(page)) {
			int wx = x + 8;
			int wy = y + 37 + off;
			groups.add(display.getWidgets(wx, wy, width, height));
			off += display.getHeight() + RECIPE_PADDING;
		}
		return groups;
	}

	private int getVerticalRecipeSpace(int backgroundHeight) {
		int height = backgroundHeight - 46;
		if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
			if (!EmiRecipes.workstations.getOrDefault(category, List.of()).isEmpty() || RecipeScreen.resolve != null) {
				height -= 23;
			}
		}
		return height;
	}

	public void bakePages(int height) {
		height = getVerticalRecipeSpace(height);
		pages.clear();
		List<RecipeDisplay> current = Lists.newArrayList();
		int h = 0;
		for (RecipeDisplay recipe : displays) {
			int rh = recipe.getHeight();
			if (!current.isEmpty() && h + rh > height) {
				pages.add(current);
				current = Lists.newArrayList();
				h = 0;
			}
			h += rh + RECIPE_PADDING;
			current.add(recipe);
		}
		if (!current.isEmpty()) {
			pages.add(current);
		}
	}

	public int getWidth() {
		return width;
	}

	public int getPageCount() {
		return pages.size();
	}

	public List<RecipeDisplay> getPage(int page) {
		if (page >= 0 && page < getPageCount()) {
			return pages.get(page);
		}
		return List.of();
	}
}
