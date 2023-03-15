package dev.emi.emi.bom;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public class TreeCost {
	public Map<EmiIngredient, FlatMaterialCost> costs = Maps.newHashMap();
	public Map<EmiIngredient, ChanceMaterialCost> chanceCosts = Maps.newHashMap();
	public Map<EmiStack, FlatMaterialCost> remainders = Maps.newHashMap();
	public Map<EmiStack, ChanceMaterialCost> chanceRemainders = Maps.newHashMap();

	public void calculate(MaterialNode node, long batches) {
		costs.clear();
		chanceCosts.clear();
		remainders.clear();
		chanceRemainders.clear();
		calculateCost(node, batches * node.amount, ChanceState.DEFAULT, false);
	}

	public void calculateProgress(MaterialNode node, long batches, EmiPlayerInventory inventory) {
		costs.clear();
		chanceCosts.clear();
		remainders.clear();
		chanceRemainders.clear();
		for (EmiStack stack : inventory.inventory.values()) {
			stack = stack.copy();
			remainders.put(stack, new FlatMaterialCost(stack, stack.getAmount()));
		}
		calculateCost(node, batches * node.amount, ChanceState.DEFAULT, true);
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

	private void addCost(EmiIngredient stack, long amount, long minBatch, ChanceState chance) {
		if (chance.chanced()) {
			if (chanceCosts.containsKey(stack)) {
				chanceCosts.get(stack).merge(amount, chance.chance());
			} else {
				chanceCosts.put(stack, new ChanceMaterialCost(stack, amount, chance.chance()));
			}
			chanceCosts.get(stack).minBatch(minBatch);
		} else {
			if (costs.containsKey(stack)) {
				costs.get(stack).amount += amount;
			} else {
				costs.put(stack, new FlatMaterialCost(stack, amount));
			}
		}
	}

	private void addRemainder(EmiStack stack, long amount, ChanceState chance) {
		if (amount > 0) {
			stack = stack.copy().setAmount(1);
			chance = chance.consume(stack.getChance());
			if (chance.chanced()) {
				if (chanceRemainders.containsKey(stack)) {
					chanceRemainders.get(stack).merge(amount, chance.chance());
				} else {
					chanceRemainders.put(stack, new ChanceMaterialCost(stack, amount, chance.chance()));
				}
			} else {
				if (remainders.containsKey(stack)) {
					remainders.get(stack).amount += amount;
				} else {
					remainders.put(stack, new FlatMaterialCost(stack, amount));
				}
			}
		}
	}

	private double getChancedRemainder(EmiStack stack, double desired, boolean catalyst, ChanceState chance) {
		double given = 0;
		ChanceMaterialCost chancedRemainder = chanceRemainders.get(stack);
		if (chancedRemainder != null) {
			double remainderEff = chancedRemainder.amount * chancedRemainder.chance;
			if (remainderEff >= desired) {
				if (!catalyst) {
					chancedRemainder.amount = 1;
					chancedRemainder.chance = (float) (remainderEff - desired);
					if (chancedRemainder.chance == 0) {
						chanceRemainders.remove(stack);
					}
				}
				return desired;
			} else {
				given = remainderEff;
				if (!catalyst) {
					double leftover = remainderEff - (given * chance.chance());
					if (leftover == 0) {
						chanceRemainders.remove(stack);
					} else {
						chancedRemainder.amount = 1;
						chancedRemainder.chance = (float) leftover;
					}
				}
			}
		} else {
			FlatMaterialCost remainder = remainders.get(stack);
			if (remainder != null) {
				// TODO add partial chanced remainders
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
					given += remainder.amount;
				}
			}
		}
		return given;
	}

	private long getRemainder(EmiStack stack, long desired, boolean catalyst) {
		FlatMaterialCost remainder = remainders.get(stack);
		if (remainder != null) {
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

	private void calculateCost(MaterialNode node, long amount, ChanceState chance, boolean trackProgress) {
		if (trackProgress) {
			node.progress = ProgressState.UNSTARTED;
			node.neededBatches = 0;
		}
		EmiRecipe recipe = node.recipe;
		if (recipe instanceof EmiResolutionRecipe) {
			calculateCost(node.children.get(0), amount, chance, trackProgress);
			return;
		}
		boolean catalyst = isCatalyst(node.ingredient);
		if (catalyst) {
			amount = node.amount;
		}
		long original = amount;
		List<EmiStack> ingredientStacks = node.ingredient.getEmiStacks();
		for (int i = 0; i < ingredientStacks.size(); i++) {
			if (chance.chanced()) {
				double desired = amount * chance.chance();
				double given = getChancedRemainder(ingredientStacks.get(i), desired, catalyst, chance);
				if (given > 0) {
					double scaled = given / chance.chance();
					amount -= (long) scaled;
					if (amount > 0) {
						chance = new ChanceState((float) ((amount - (scaled % 1)) * chance.chance() / amount), true);
					}
				}
			} else {
				amount -= getRemainder(ingredientStacks.get(i), amount, catalyst);
			}
		}
		if (amount == 0) {
			if (trackProgress) {
				complete(node);
			}
			return;
		}
		if (trackProgress && amount != original) {
			node.progress = ProgressState.PARTIAL;
		}
		
		long effectiveCrafts = amount;
		if (recipe != null) {
			long minBatches = (int) Math.ceil(amount / (float) node.divisor);
			effectiveCrafts = minBatches * node.divisor;
			if (trackProgress) {
				node.neededBatches = minBatches;
			}
			ChanceState produced = chance.produce(node.produceChance);
			for (MaterialNode n : node.children) {
				calculateCost(n, minBatches * n.amount, produced.consume(n.consumeChance), trackProgress);
			}
			EmiStack stack = node.ingredient.getEmiStacks().get(0);
			addRemainder(stack, effectiveCrafts - amount, chance);

			for (EmiStack es : recipe.getOutputs()) {
				if (!stack.equals(es)) {
					addRemainder(es, minBatches * es.getAmount(), chance);
				}
			}
		} else {
			addCost(node.ingredient, amount, node.amount, chance);
		}

		if (node.ingredient.getEmiStacks().size() == 1) {
			EmiStack r = node.ingredient.getEmiStacks().get(0).getRemainder();
			if (!r.isEmpty()) {
				addRemainder(r, effectiveCrafts, chance);
			}
		}
	}

	public long getIdealBatch(MaterialNode node, long total) {
		if (node.divisor > 0) {
			total *= node.divisor / gcd(total, node.divisor);
		}
		if (node.children != null) {
			for (MaterialNode n : node.children) {
				total = getIdealBatch(n, total);
			}
		}
		return total;
	}

	public long gcd(long a, long b) {
		if (b == 0) {
			return a;
		} else {
			return gcd(b, a % b);
		}
	}
}
