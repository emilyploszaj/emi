package dev.emi.emi.api.stack;

import dev.emi.emi.api.recipe.EmiRecipe;

public class EmiStackInteraction {
	public static final EmiStackInteraction EMPTY = new EmiStackInteraction(EmiStack.EMPTY, null, false);
	private final EmiIngredient stack;
	private final EmiRecipe recipe;
	private final boolean clickable;

	public EmiStackInteraction(EmiIngredient stack) {
		this(stack, null, true);
	}

	/**
	 * @param stack The ingredient being interacted with
	 * @param recipe The recipe associated with this ingredient, like from the output of a recipe, or a favorited recipe
	 * @param clickable Whether this stack can be interacted with using a mouse for EMI functions.
	 * 	For example, stacks in the sidebar can, but stacks in the inventory cannot.
	 */
	public EmiStackInteraction(EmiIngredient stack, EmiRecipe recipe, boolean clickable) {
		this.stack = stack;
		this.recipe = recipe;
		this.clickable = clickable;
	}

	public EmiIngredient getStack() {
		return stack;
	}

	public EmiRecipe getRecipeContext() {
		return recipe;
	}

	public boolean isClickable() {
		return clickable;
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}
}
