package dev.emi.emi.bom;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.data.RecipeDefaults;
import dev.emi.emi.runtime.EmiPersistentData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class BoM {
	private static RecipeDefaults defaults = new RecipeDefaults();
	public static List<MaterialTree> trees = Lists.newArrayList();
	public static int treeIndex = -1;
	public static TreeCost combinedCost = new TreeCost();
	public static TreeCost combinedProgress = new TreeCost();
	public static Map<EmiIngredient, EmiRecipe> defaultRecipes = Maps.newHashMap();
	public static Map<EmiIngredient, EmiRecipe> addedRecipes = Maps.newHashMap();
	public static Set<EmiRecipe> disabledRecipes = Sets.newHashSet();
	public static boolean craftingMode = false;

	public static void setDefaults(RecipeDefaults defaults) {
		BoM.defaults = defaults;
		MinecraftClient.getInstance().execute(() -> reload());
	}

	public static JsonObject saveAdded() {
		JsonArray added = new JsonArray();
		JsonObject addedTags = new JsonObject();
		JsonObject resolutions = new JsonObject();
		Set<Identifier> placed = Sets.newHashSet();
		for (Map.Entry<EmiIngredient, EmiRecipe> entry : addedRecipes.entrySet()) {
			EmiRecipe recipe = entry.getValue();
			if (recipe instanceof EmiResolutionRecipe err) {
				if (err.ingredient instanceof TagEmiIngredient tei) {
					JsonElement el = EmiIngredientSerializer.getSerialized(tei.copy().setAmount(1).setChance(1));
					JsonElement val = EmiIngredientSerializer.getSerialized(err.stack);
					if (el != null && JsonHelper.isString(el) && val != null) {
						addedTags.add(el.getAsString(), val);
					}
				}
			} else if (recipe != null && recipe.getId() != null && !placed.contains(recipe.getId())) {
				DefaultStatus status = getRecipeStatus(recipe);
				placed.add(recipe.getId());
				if (status == DefaultStatus.FULL) {
					added.add(recipe.getId().toString());
				} else if (status == DefaultStatus.PARTIAL) {
					JsonArray arr = new JsonArray();
					for (EmiStack stack : recipe.getOutputs()) {
						if (getRecipe(stack) == recipe) {
							JsonElement el = EmiIngredientSerializer.getSerialized(stack);
							if (el != null) {
								arr.add(el);
							}
						}
					}
					if (!arr.isEmpty()) {
						resolutions.add(recipe.getId().toString(), arr);
					}
				}
			}
		}
		JsonArray disabled = new JsonArray();
		for (EmiRecipe recipe : disabledRecipes) {
			if (recipe != null && recipe.getId() != null) {
				disabled.add(recipe.getId().toString());
			}
		}
		JsonObject obj = new JsonObject();
		obj.add("added", added);
		obj.add("tags", addedTags);
		obj.add("resolutions", resolutions);
		obj.add("disabled", disabled);
		return obj;
	}

	public static void loadAdded(JsonObject object) {
		addedRecipes.clear();
		disabledRecipes.clear();
		JsonArray disabled = JsonHelper.getArray(object, "disabled", new JsonArray());
		for (JsonElement el : disabled) {
			Identifier id = EmiPort.id(el.getAsString());
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			disabledRecipes.add(recipe);
		}
		JsonArray added = JsonHelper.getArray(object, "added", new JsonArray());
		for (JsonElement el : added) {
			Identifier id = EmiPort.id(el.getAsString());
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null && !disabledRecipes.contains(recipe)) {
				for (EmiStack output : recipe.getOutputs()) {
					addedRecipes.put(output, recipe);
				}
			}
		}
		JsonObject resolutions = JsonHelper.getObject(object, "resolutions", new JsonObject());
		for (String key : resolutions.keySet()) {
			Identifier id = EmiPort.id(key);
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null && JsonHelper.hasArray(resolutions, key)) {
				JsonArray arr = JsonHelper.getArray(resolutions, key);
				for (JsonElement el : arr) {
					EmiIngredient stack = EmiIngredientSerializer.getDeserialized(el);
					if (!stack.isEmpty()) {
						addedRecipes.put(stack, recipe);
					}
				}
			}
		}
		JsonObject addedTags = JsonHelper.getObject(object, "tags", new JsonObject());
		for (String key : addedTags.keySet()) {
			EmiIngredient tag = EmiIngredientSerializer.getDeserialized(new JsonPrimitive(key));
			EmiIngredient stack = EmiIngredientSerializer.getDeserialized(addedTags.get(key));
			if (!tag.isEmpty() && !stack.isEmpty() && stack.getEmiStacks().size() == 1 && tag.getEmiStacks().containsAll(stack.getEmiStacks())) {
				addedRecipes.put(tag, new EmiResolutionRecipe(tag, stack.getEmiStacks().get(0)));
			}
		}
	}

	public static void reload() {
		defaultRecipes = defaults.bake();
	}

	public static boolean isRecipeEnabled(EmiRecipe recipe) {
		return !disabledRecipes.contains(recipe) && (defaultRecipes.values().contains(recipe) || addedRecipes.values().contains(recipe));
	}

	public static DefaultStatus getRecipeStatus(EmiRecipe recipe) {
		int found = 0;
		for (EmiStack stack : recipe.getOutputs()) {
			if (recipe.equals(getRecipe(stack))) {
				found++;
			}
		}
		if (found == 0) {
			return DefaultStatus.EMPTY;
		} else if (found >= recipe.getOutputs().size()) {
			return DefaultStatus.FULL;
		} else {
			return DefaultStatus.PARTIAL;
		}
	}

	public static EmiRecipe getRecipe(EmiIngredient stack) {
		EmiRecipe recipe = addedRecipes.get(stack);
		if (recipe == null) {
			recipe = defaultRecipes.get(stack);
			if (recipe != null && disabledRecipes.contains(recipe)) {
				return null;
			}
		}
		return recipe;
	}

	public static void setGoal(EmiRecipe recipe) {
		trees.clear();
		trees.add(new MaterialTree(recipe));
		treeIndex = 0;
		craftingMode = false;
		recalculate();
	}

   	public static void addGoal(EmiRecipe recipe) {
		if (trees.isEmpty()) {
			setGoal(recipe);
			return;
		}
		trees.add(new MaterialTree(recipe));
		treeIndex = trees.size() - 1;
		recalculate();
	}

   	public static MaterialTree getTree() {
		if (treeIndex < 0 || treeIndex >= trees.size()) {
			return null;
		}
		return trees.get(treeIndex);
	}

   	public static List<MaterialTree> getTrees() {
		return trees;
	}

   	public static void selectTree(int index) {
		if (trees.isEmpty()) {
			treeIndex = -1;
			return;
		}
		treeIndex = Math.max(0, Math.min(index, trees.size() - 1));
	}

   	public static void cycleTree(int delta) {
		if (trees.isEmpty()) {
			treeIndex = -1;
			return;
		}
		int size = trees.size();
		treeIndex = ((treeIndex + delta) % size + size) % size;
	}

   	public static void removeTree(int index) {
		if (index < 0 || index >= trees.size()) {
			return;
		}
		trees.remove(index);
		if (trees.isEmpty()) {
			treeIndex = -1;
			craftingMode = false;
		} else {
			treeIndex = Math.max(0, Math.min(treeIndex, trees.size() - 1));
		}
		recalculate();
	}

	public static void addResolution(EmiIngredient ingredient, EmiRecipe recipe) {
		MaterialTree tree = getTree();
		if (tree != null) {
			tree.addResolution(ingredient, recipe);
		}
	}

	public static boolean isDefaultRecipe(EmiIngredient stack, EmiRecipe recipe) {
		if (recipe instanceof EmiResolutionRecipe err) {
			if (getRecipe(err.ingredient) instanceof EmiResolutionRecipe res) {
				return stack.equals(res.stack);
			}
		}
		return getRecipe(stack) == recipe;
	}

	public static void addRecipe(EmiRecipe recipe) {
		disabledRecipes.remove(recipe);
		for (EmiStack stack : recipe.getOutputs()) {
			addedRecipes.put(stack, recipe);
		}
		EmiPersistentData.save();
		recalculate();
	}

	public static void addRecipe(EmiIngredient stack, EmiRecipe recipe) {
		if (recipe instanceof EmiResolutionRecipe err) {
			stack = err.ingredient;
		}
		addedRecipes.put(stack, recipe);
		EmiPersistentData.save();
		recalculate();
	}

	public static void removeRecipe(EmiRecipe recipe) {
		for (EmiStack stack : recipe.getOutputs()) {
			addedRecipes.remove(stack, recipe);
		}
		if (getRecipeStatus(recipe) != DefaultStatus.EMPTY) {
			disabledRecipes.add(recipe);
		}
		EmiPersistentData.save();
		recalculate();
	}

	public static void removeRecipe(EmiIngredient stack, EmiRecipe recipe) {
		if (recipe instanceof EmiResolutionRecipe err) {
			if (addedRecipes.get(err.ingredient) instanceof EmiResolutionRecipe res && stack.equals(res.stack)) {
				addedRecipes.remove(err.ingredient);
			}
		} else {
			addedRecipes.remove(stack, recipe);
		}
		if (getRecipeStatus(recipe) != DefaultStatus.EMPTY) {
			disabledRecipes.add(recipe);
		}
		EmiPersistentData.save();
		recalculate();
	}

	public static void calculateCombinedCosts(EmiPlayerInventory inventory) {
		combinedProgress.clear();
		for (MaterialTree tree : trees) {
			tree.calculateProgress(inventory);
			combinedProgress.merge(tree.cost);
		}
		combinedCost.clear();
		for (MaterialTree tree : trees) {
			tree.calculateCost();
			combinedCost.merge(tree.cost);
		}
	}

    private static void recalculate() {
        for (MaterialTree tree : trees) {
            tree.recalculate();
        }
    }

	public static enum DefaultStatus {
		EMPTY,
		PARTIAL,
		FULL
	}
}
