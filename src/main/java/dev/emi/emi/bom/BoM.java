package dev.emi.emi.bom;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiRecipes;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.data.RecipeDefault;
import net.minecraft.client.MinecraftClient;

public class BoM {
	private static List<RecipeDefault> defaults = List.of();
	public static MaterialNode goal;
	public static List<MaterialCost> costs = Lists.newArrayList();
	public static Map<EmiRecipe, MaterialCost> remainders = Maps.newHashMap();
	public static Map<EmiStack, EmiRecipe> defaultRecipes = Maps.newHashMap();
	public static Map<EmiStack, EmiRecipe> addedRecipes = Maps.newHashMap();

	public static void setDefaults(List<RecipeDefault> defaults) {
		BoM.defaults = defaults;
		MinecraftClient.getInstance().execute(() -> reload());
	}

	public static void reload() {
		for (RecipeDefault def : defaults) {
			EmiRecipe recipe = EmiRecipes.byId.get(def.id());
			if (recipe != null) {
				for (EmiStack out : recipe.getOutputs()) {
					if (def.matchAll() || out.isEqual(def.stack())) {
						defaultRecipes.put(out, recipe);
					}
				}
			}
		}
	}

	public static EmiRecipe getRecipe(EmiIngredient stack) {
		EmiRecipe recipe = addedRecipes.get(stack);
		if (recipe == null) {
			recipe = defaultRecipes.get(stack);
		}
		return recipe;
	}

	public static void setGoal(EmiRecipe recipe) {
		EmiStack output = recipe.getOutputs().get(0);
		goal = new MaterialNode(output);
		goal.defineRecipe(recipe);
		goal.recalculate();
	}

	public static void addRecipe(EmiRecipe recipe, EmiStack stack) {
		addedRecipes.put(stack, recipe);
		goal.recalculate();
	}

	public static void calculateCost() {
		costs.clear();
		remainders.clear();
		calculateCost(costs, remainders, goal.amount, goal);
	}

	private static void calculateCost(List<MaterialCost> costs, Map<EmiRecipe, MaterialCost> remainders, int amount, MaterialNode node) {
		EmiRecipe recipe = node.recipe;
		if (remainders.containsKey(recipe)) {
			MaterialCost remainder = remainders.get(recipe);
			if (remainder.amount >= amount) {
				remainder.amount -= amount;
				if (remainder.amount == 0) {
					remainders.remove(recipe);
				}
				return;
			} else {
				amount -= remainder.amount;
				remainders.remove(recipe);
			}
		}
		
		if (recipe != null) {
			int minBatches = (int) Math.ceil(amount / (float) node.divisor);
			int remainder = minBatches * node.divisor - amount;
			if (remainder > 0) {
				if (remainders.containsKey(recipe)) {
					remainders.get(recipe).amount += remainder;
				} else {
					remainders.put(recipe, new MaterialCost(node.ingredient, remainder));
				}
			}

			for (MaterialNode n : node.children) {
				calculateCost(costs, remainders, minBatches * n.amount, n);
			}
		} else {
			for (MaterialCost cost : costs) {
				if (EmiIngredient.areEqual(cost.ingredient, node.ingredient)) {
					cost.amount += amount;
					return;
				}
			}
			costs.add(new MaterialCost(node.ingredient, amount));
		}
	}
}
