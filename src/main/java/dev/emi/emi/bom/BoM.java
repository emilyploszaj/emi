package dev.emi.emi.bom;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiRecipes;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.data.RecipeDefault;
import net.minecraft.client.MinecraftClient;

public class BoM {
	private static List<RecipeDefault> defaults = List.of();
	public static MaterialTree tree;
	public static Map<EmiStack, EmiRecipe> defaultRecipes = Maps.newHashMap();
	public static Map<EmiStack, EmiRecipe> addedRecipes = Maps.newHashMap();
	public static Set<EmiRecipe> disabledRecipes = Sets.newHashSet();

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

	public static boolean isRecipeEnabled(EmiRecipe recipe) {
		return !disabledRecipes.contains(recipe) && (defaultRecipes.values().contains(recipe) || addedRecipes.values().contains(recipe));
	}

	public static EmiRecipe getRecipe(EmiIngredient stack) {
		EmiRecipe recipe = addedRecipes.get(stack);
		if (recipe == null) {
			recipe = defaultRecipes.get(stack);
		}
		if (recipe != null && disabledRecipes.contains(recipe)) {
			return null;
		}
		return recipe;
	}

	public static void setGoal(EmiRecipe recipe) {
		tree = new MaterialTree(recipe);
	}

	public static void addResolution(EmiIngredient ingredient, EmiRecipe recipe) {
		tree.addResolution(ingredient, recipe);
	}

	public static void addRecipe(EmiRecipe recipe, EmiStack stack) {
		disabledRecipes.remove(recipe);
		addedRecipes.put(stack, recipe);
		recalculate();
	}

	public static void removeRecipe(EmiRecipe recipe) {
		disabledRecipes.add(recipe);
		recalculate();
	}

	private static void recalculate() {
		if (tree != null) {
			tree.recalculate();
		}
	}
}
