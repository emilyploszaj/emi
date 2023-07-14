package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class EmiRecipeSorter {
	private static Map<EmiRecipe, IntList> inputCache = Maps.newIdentityHashMap();
	private static Map<EmiRecipe, IntList> outputCache = Maps.newIdentityHashMap();

	public static IntList getInput(EmiRecipe recipe) {
		IntList list = inputCache.get(recipe);
		if (list == null) {
			list = bakedList(recipe.getInputs());
			inputCache.put(recipe, list);
		}
		return list;
	}

	public static IntList getOutput(EmiRecipe recipe) {
		IntList list = outputCache.get(recipe);
		if (list == null) {
			list = bakedList(recipe.getOutputs());
			outputCache.put(recipe, list);
		}
		return list;
	}

	public static void clear() {
		inputCache.clear();
		outputCache.clear();
	}

	private static IntList bakedList(List<? extends EmiIngredient> stacks) {
		IntList list = new IntArrayList(stacks.size());
		for (EmiIngredient stack : stacks) {
			if (stack.isEmpty()) {
				continue;
			}
			int value = EmiStackList.indices.getOrDefault(stack.getEmiStacks().get(0), Integer.MAX_VALUE);
			list.add(value);
		}
		return list;
	}
}
