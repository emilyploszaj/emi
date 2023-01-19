package dev.emi.emi.bom;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
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
	public int batches = 1;

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
		}
		recalculate();
	}

	public void recalculate() {
		goal.recalculate(this);
	}

	public void calculateProgress(EmiPlayerInventory inventory) {
		Map<EmiStack, FlatMaterialCost> remainders = Maps.newHashMap();
		for (EmiStack stack : inventory.inventory.values()) {
			stack = stack.copy();
			remainders.put(stack, new FlatMaterialCost(stack, stack.getAmount()));
		}
		costs.clear();
		calculateFlatCost(costs, remainders, batches * goal.amount, goal, true);
	}

	public void calculateCost(boolean fractional) {
		if (fractional) {
			fractionalCosts.clear();
			Map<EmiStack, FractionalMaterialCost> fractionalRemainders = Maps.newHashMap();
			calculateFractionalCost(fractionalCosts, fractionalRemainders, batches * goal.amount, goal);
			// Calculate twice with the remainders of the last to remove temporary costs per batch
			fractionalCosts.clear();
			calculateFractionalCost(fractionalCosts, fractionalRemainders, batches * goal.amount, goal);
		} else {
			costs.clear();
			remainders.clear();
			calculateFlatCost(costs, remainders, batches * goal.amount, goal, false);
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
		stack = stack.copy().setAmount(1);
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
				if (!catalyst) {
					remainder.amount -= desired;
					if (remainder.amount == 0) {
						remainders.remove(stack);
					}
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

	private void complete(MaterialNode node) {
		node.progress = ProgressState.COMPLETED;
		node.neededBatches = 0;
		if (node.children != null) {
			for (MaterialNode child : node.children) {
				complete(child);
			}
		}
	}

	// TODO clean up the whole remainder thing, I feel like it's gonna break
	private void calculateFlatCost(List<FlatMaterialCost> costs, Map<EmiStack, FlatMaterialCost> remainders, long amount,
			MaterialNode node, boolean progress) {
		if (progress) {
			node.progress = ProgressState.UNSTARTED;
			node.neededBatches = 0;
		}
		EmiRecipe recipe = node.recipe;
		if (recipe instanceof EmiResolutionRecipe) {
			calculateFlatCost(costs, remainders, amount, node.children.get(0), progress);
			return;
		}
		boolean catalyst = isCatalyst(node.ingredient);
		if (catalyst) {
			amount = node.amount;
		}
		long original = amount;
		List<EmiStack> ingredientStacks = node.ingredient.getEmiStacks();
		for (int i = 0; i < ingredientStacks.size(); i++) {
			amount -= getRemainder(remainders, ingredientStacks.get(i), amount, catalyst);
		}
		if (amount == 0) {
			if (progress) {
				complete(node);
			}
			return;
		}
		if (progress && amount != original) {
			node.progress = ProgressState.PARTIAL;
		}
		
		if (recipe != null) {
			long minBatches = (int) Math.ceil(amount / (float) node.divisor);
			if (progress) {
				node.neededBatches = minBatches;
			}
			for (MaterialNode n : node.children) {
				calculateFlatCost(costs, remainders, minBatches * n.amount, n, progress);
			}
			long remainder = minBatches * node.divisor;
			remainder -= amount;
			EmiStack stack = node.ingredient.getEmiStacks().get(0);
			addRemainder(remainders, stack, remainder);

			for (EmiStack es : recipe.getOutputs()) {
				if (!stack.equals(es)) {
					addRemainder(remainders, es, minBatches * es.getAmount());
				}
			}
		} else {
			outer: {
				for (FlatMaterialCost cost : costs) {
					if (EmiIngredient.areEqual(cost.ingredient, node.ingredient)) {
						cost.amount += amount;
						break outer;
					}
				}
				costs.add(new FlatMaterialCost(node.ingredient, amount));
			}
		}

		if (node.ingredient.getEmiStacks().size() == 1) {
			EmiStack r = node.ingredient.getEmiStacks().get(0).getRemainder();
			if (!r.isEmpty()) {
				addRemainder(remainders, r, r.getAmount() * amount);
			}
		}
	}

	private void addFractionalRemainder(Map<EmiStack, FractionalMaterialCost> remainders, EmiStack stack, float amount) {
		stack = stack.copy().setAmount(1);
		if (amount > 0) {
			if (remainders.containsKey(stack)) {
				remainders.get(stack).amount += amount;
			} else {
				remainders.put(stack, new FractionalMaterialCost(stack, amount));
			}
		}
	}

	private float getFractionalRemainder(Map<EmiStack, FractionalMaterialCost> remainders, EmiStack stack, float desired) {
		if (remainders.containsKey(stack)) {
			FractionalMaterialCost remainder = remainders.get(stack);
			if (remainder.amount >= desired) {
				remainder.amount -= desired;
				if (remainder.amount == 0) {
					remainders.remove(stack);
				}
				return desired;
			} else {
				remainders.remove(stack);
				return remainder.amount;
			}
		}
		return 0;
	}

	private void calculateFractionalCost(List<FractionalMaterialCost> costs, Map<EmiStack, FractionalMaterialCost> remainders, float amount, MaterialNode node) {
		EmiRecipe recipe = node.recipe;
		List<EmiStack> ingredientStacks = node.ingredient.getEmiStacks();
		for (int i = 0; i < ingredientStacks.size(); i++) {
			amount -= getFractionalRemainder(remainders, ingredientStacks.get(i), amount);
		}
		if (amount <= 0) {
			return;
		}
		if (recipe != null) {
			amount = amount / node.divisor;

			for (MaterialNode n : node.children) {
				calculateFractionalCost(costs, remainders, amount * n.amount, n);
			}

			EmiStack stack = node.ingredient.getEmiStacks().get(0);
			for (EmiStack es : recipe.getOutputs()) {
				if (!stack.equals(es)) {
					addFractionalRemainder(remainders, es, amount * es.getAmount());
				}
			}
		} else {
			outer : {
				for (FractionalMaterialCost cost : costs) {
					if (EmiIngredient.areEqual(cost.ingredient, node.ingredient)) {
						cost.amount += amount;
						break outer;
					}
				}
				costs.add(new FractionalMaterialCost(node.ingredient, amount));
			}
		}
		if (node.ingredient.getEmiStacks().size() == 1) {
			EmiStack stack = node.ingredient.getEmiStacks().get(0).getRemainder();
			addFractionalRemainder(remainders, stack, stack.getAmount() * amount);
		}
	}
}
