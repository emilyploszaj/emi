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

	/**
	 * @return The vanilla recipe manager, for iterating recipe types.
	 */
	RecipeManager getRecipeManager();
	
	/**
	 * Adds a recipe category.
	 * Recipes are organized based on recipe category.
	 */
	void addCategory(EmiRecipeCategory category);

	/**
	 * Adds a workstation to a recipe category.
	 */
	void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation);

	/**
	 * Adds a recipe to EMI that can be viewed and associated with its components.
	 */
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

	/**
	 * Adds a default compraison method for a stack key.
	 * @param key A stack key such as an item or fluid.
	 * @param comparison A function to mutate the current comprison method.
	 */
	void setDefaultComparison(Object key, Function<Comparison, Comparison> comparison);

	/**
	 * Adds a default compraison method for a stack using its key.
	 * @param stack A stack to derive a key from.
	 * @param comparison A function to mutate the current comprison method.
	 */
	default void setDefaultComparison(EmiStack stack, Function<Comparison, Comparison> comparison) {
		setDefaultComparison(stack.getKey(), comparison);
	}

	void addRecipeHandler(EmiRecipeCategory category, EmiRecipeHandler<?> handler);
}
