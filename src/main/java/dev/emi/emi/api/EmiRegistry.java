package dev.emi.emi.api;

import java.util.function.Function;
import java.util.function.Predicate;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

public interface EmiRegistry {

	RecipeManager getRecipeManager();
	
	void addCategory(EmiRecipeCategory category);

	void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation);

	void addRecipe(EmiRecipe recipe);

	/**
	 * Adds a predicate to run on all current and future recipes to prevent certain ones from being added.
	 */
	void removeRecipes(Predicate<EmiRecipe> predicate);

	/**
	 * Adds a predicate to run on all current and future recipes to prevent certain ones with the given identifier from being added.
	 */
	default void removeRecipes(Identifier id) {
		removeRecipes(r -> id.equals(r.getId()));
	}

	/**
	 * Adds an EmiStack to the sidebar.
	 */
	void addEmiStack(EmiStack stack);

	/**
	 * Adds an EmiStack to the sidebar immediately following another.
	 * If the predicate never succeeds, the provided EmiStack will not be added.
	 */
	void addEmiStackAfter(EmiStack stack, Predicate<EmiStack> predicate);

	/**
	 * Adds an EmiStack to the sidebar immediately following another.
	 * If the predicate never succeeds, the provided EmiStack will not be added.
	 */
	default void addEmiStackAfter(EmiStack stack, EmiStack other) {
		addEmiStackAfter(stack, s -> s.equals(other));
	}

	/**
	 * Adds a predicate to run on all current and future EmiStacks to prevent certain ones from being added to the sidebar.
	 */
	void removeEmiStacks(Predicate<EmiStack> predicate);

	/**
	 * Adds a predicate to run on all current and future EmiStacks to prevent matching ones from being added to the sidebar.
	 */
	default void removeEmiStacks(EmiStack stack) {
		removeEmiStacks(s -> s.equals(stack));
	}

	/**
	 * Adds an EmiExclusionArea to screens of a given class.
	 * Exclusion areas can provide rectangles where EMI will not place EmiStacks.
	 */
	<T extends Screen> void addExclusionArea(Class<T> clazz, EmiExclusionArea<T> area);

	/**
	 * Adds an EmiExclusionArea to every screen.
	 * Exclusion areas can provide rectangles where EMI will not place EmiStacks.
	 */
	void addGenericExclusionArea(EmiExclusionArea<Screen> area);

	void addRecipeHandler(EmiRecipeCategory category, EmiRecipeHandler<?> handler);

	void setDefaultComparison(Object key, Function<Comparison, Comparison> comparison);

	default void setDefaultComparison(EmiStack stack, Function<Comparison, Comparison> comparison) {
		setDefaultComparison(stack.getKey(), comparison);
	}
}
