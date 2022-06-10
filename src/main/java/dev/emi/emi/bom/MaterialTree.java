package dev.emi.emi.bom;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public class MaterialTree {
	public MaterialNode goal;
	public List<FractionalMaterialCost> fractionalCosts = Lists.newArrayList();
	public List<FlatMaterialCost> costs = Lists.newArrayList();
	public Map<EmiStack, FlatMaterialCost> remainders = Maps.newHashMap();
	public Map<EmiIngredient, EmiRecipe> resolutions = Maps.newHashMap();

	public MaterialTree(EmiRecipe recipe) {
		EmiStack output = recipe.getOutputs().get(0);
		goal = new MaterialNode(output);
		goal.defineRecipe(recipe);
		recalculate();
	}

	public EmiRecipe getRecipe(EmiIngredient stack) {
		EmiRecipe recipe = resolutions.get(stack);
		if (recipe == null) {
			recipe = BoM.getRecipe(stack);
		}
		return recipe;
	}

	public void addResolution(EmiIngredient ingredient, EmiRecipe recipe) {
		resolutions.put(ingredient, recipe);
		recalculate();
	}

	public void recalculate() {
		goal.recalculate(this);
	}

	public void calculateCost(boolean fractional) {
		if (fractional) {
			fractionalCosts.clear();
			calculateFractionalCost(fractionalCosts, goal.amount, goal);
		} else {
			costs.clear();
			remainders.clear();
			calculateFlatCost(costs, remainders, goal.amount, goal);
		}
	}

	public static boolean isCatalyst(EmiIngredient ing) {
		if (ing.getEmiStacks().size() == 1) {
			EmiStack stack = ing.getEmiStacks().get(0);
			if (stack.equals(stack.getRemainder())) {
				return true;
			}
		}
		return false;
	}

	private void addRemainder(Map<EmiStack, FlatMaterialCost> remainders, EmiStack stack, long amount) {
		if (amount > 0) {
			if (remainders.containsKey(stack)) {
				remainders.get(stack).amount += amount;
			} else {
				remainders.put(stack, new FlatMaterialCost(stack, amount));
			}
		}
	}

	private long getRemainder(Map<EmiStack, FlatMaterialCost> remainders, EmiStack stack, long desired, boolean catalyst) {
		if (remainders.containsKey(stack)) {
			FlatMaterialCost remainder = remainders.get(stack);
			if (remainder.amount >= desired) {
				remainder.amount -= desired;
				if (remainder.amount == 0 && !catalyst) {
					remainders.remove(stack);
				}
				return desired;
			} else {
				if (!catalyst) {
					remainders.remove(stack);
				}
				return remainder.amount;
			}
		}
		return 0;
	}

	// TODO clean up the whole remainder thing, I feel like it's gonna break
	private void calculateFlatCost(List<FlatMaterialCost> costs, Map<EmiStack, FlatMaterialCost> remainders, long amount, MaterialNode node) {
		EmiRecipe recipe = node.recipe;
		if (recipe instanceof EmiResolutionRecipe) {
			calculateFlatCost(costs, remainders, amount, node.children.get(0));
			return;
		}
		boolean catalyst = isCatalyst(node.ingredient);
		if (catalyst) {
			amount = node.amount;
		}
		amount -= getRemainder(remainders, node.ingredient.getEmiStacks().get(0), amount, catalyst);
		if (amount == 0) {
			return;
		}

		if (recipe == null && node.ingredient.getEmiStacks().size() == 1) {
			EmiStack r = node.ingredient.getEmiStacks().get(0).getRemainder();
			if (!r.isEmpty()) {
				addRemainder(remainders, r, amount);
			}
		}
		
		if (recipe != null && node.state != FoldState.COLLAPSED) {
			long minBatches = (int) Math.ceil(amount / (float) node.divisor);
			long remainder = minBatches * node.divisor;
			if (!catalyst) {
				remainder -= amount;
			}
			EmiStack stack = node.ingredient.getEmiStacks().get(0);
			addRemainder(remainders, stack, remainder);

			for (MaterialNode n : node.children) {
				calculateFlatCost(costs, remainders, minBatches * n.amount, n);
			}
			for (EmiStack es : recipe.getOutputs()) {
				if (!stack.equals(es)) {
					addRemainder(remainders, es, minBatches * es.getAmount());
				}
			}
		} else {
			for (FlatMaterialCost cost : costs) {
				if (EmiIngredient.areEqual(cost.ingredient, node.ingredient)) {
					cost.amount += amount;
					return;
				}
			}
			costs.add(new FlatMaterialCost(node.ingredient, amount));
		}
	}

	private void calculateFractionalCost(List<FractionalMaterialCost> costs, float amount, MaterialNode node) {
		EmiRecipe recipe = node.recipe;
		if (recipe != null) {
			float newAmount = amount / node.divisor;

			for (MaterialNode n : node.children) {
				calculateFractionalCost(costs, newAmount * n.amount, n);
			}
		} else {
			for (FractionalMaterialCost cost : costs) {
				if (EmiIngredient.areEqual(cost.ingredient, node.ingredient)) {
					cost.amount += amount;
					return;
				}
			}
			costs.add(new FractionalMaterialCost(node.ingredient, amount));
		}
	}
}
