package dev.emi.emi.bom;

import java.util.Map;

import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public class MaterialTree {
	public MaterialNode goal;
	public TreeCost cost = new TreeCost();
	public Map<EmiIngredient, EmiRecipe> resolutions = Maps.newHashMap();
	public long batches = 1;

	public MaterialTree(EmiRecipe recipe) {
		EmiStack output = recipe.getOutputs().get(0);
		goal = new MaterialNode(output);
		goal.defineRecipe(recipe);
		recalculate();
	}

	public EmiRecipe getRecipe(EmiIngredient stack) {
		EmiRecipe recipe = resolutions.get(stack);
		if (recipe == null && !resolutions.containsKey(stack)) {
			recipe = BoM.getRecipe(stack);
		}
		return recipe;
	}

	public void addResolution(EmiIngredient ingredient, EmiRecipe recipe) {
		resolutions.put(ingredient, recipe);
		if (ingredient.equals(goal.ingredient)) {
			goal.defineRecipe(recipe);
			goal.amount = ingredient.getAmount();
		}
		recalculate();
	}

	public void recalculate() {
		goal.recalculate(this);
	}

	public void calculateProgress(EmiPlayerInventory inventory) {
		cost.calculateProgress(goal, batches, inventory);
	}

	public void calculateCost() {
		cost.calculate(goal, batches);
	}
}
