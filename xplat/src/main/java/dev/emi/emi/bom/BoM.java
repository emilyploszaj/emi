package dev.emi.emi.bom;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.data.RecipeDefault;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiPersistentData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class BoM {
	private static List<RecipeDefault> defaults = List.of();
	public static MaterialTree tree;
	public static Map<EmiIngredient, EmiRecipe> defaultRecipes = Maps.newHashMap();
	public static Map<EmiIngredient, EmiRecipe> addedRecipes = Maps.newHashMap();
	public static Set<EmiRecipe> disabledRecipes = Sets.newHashSet();
	public static boolean craftingMode = false;

	public static void setDefaults(List<RecipeDefault> defaults) {
		BoM.defaults = defaults;
		MinecraftClient.getInstance().execute(() -> reload());
	}

	public static JsonObject saveAdded() {
		JsonArray added = new JsonArray();
		JsonObject addedTags = new JsonObject();
		Set<Identifier> placed = Sets.newHashSet();
		for (Map.Entry<EmiIngredient, EmiRecipe> entry : addedRecipes.entrySet()) {
			EmiRecipe recipe = entry.getValue();
			if (recipe instanceof EmiResolutionRecipe err) {
				if (err.ingredient instanceof TagEmiIngredient tei) {
					addedTags.addProperty(tei.key.id().toString(), EmiPort.getItemRegistry().getId(err.stack.getItemStack().getItem()).toString());
				}
			} else if (recipe != null && recipe.getId() != null && !placed.contains(recipe.getId())) {
				placed.add(recipe.getId());
				added.add(recipe.getId().toString());
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
		obj.add("added_item_tags", addedTags);
		obj.add("disabled", disabled);
		return obj;
	}

	public static void loadAdded(JsonObject object) {
		addedRecipes.clear();
		disabledRecipes.clear();
		JsonArray disabled = JsonHelper.getArray(object, "disabled", new JsonArray());
		for (JsonElement el : disabled) {
			Identifier id = new Identifier(el.getAsString());
			EmiRecipe recipe = EmiRecipes.byId.get(id);
			disabledRecipes.add(recipe);
		}
		JsonArray added = JsonHelper.getArray(object, "added", new JsonArray());
		for (JsonElement el : added) {
			Identifier id = new Identifier(el.getAsString());
			EmiRecipe recipe = EmiRecipes.byId.get(id);
			if (recipe != null && !disabledRecipes.contains(recipe)) {
				for (EmiStack output : recipe.getOutputs()) {
					addedRecipes.put(output, recipe);
				}
			}
		}
		JsonObject addedTags = JsonHelper.getObject(object, "added_item_tags", new JsonObject());
		for (String key : addedTags.keySet()) {
			Item item = EmiPort.getItemRegistry().get(new Identifier(JsonHelper.getString(addedTags, key, "")));
			Identifier id = new Identifier(key);
			TagKey<Item> tag = TagKey.of(EmiPort.getItemRegistry().getKey(), id);
			List<Item> items = EmiUtil.values(tag).map(RegistryEntry<Item>::value).toList();
			if (item != null && items.contains(item)) {
				EmiIngredient tagStack = EmiIngredient.of(tag);
				addedRecipes.put(tagStack, new EmiResolutionRecipe(tagStack, EmiStack.of(item)));
			}
		}
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
		craftingMode = false;
	}

	public static void addResolution(EmiIngredient ingredient, EmiRecipe recipe) {
		tree.addResolution(ingredient, recipe);
	}

	public static void addRecipe(EmiIngredient stack, EmiRecipe recipe) {
		disabledRecipes.remove(recipe);
		addedRecipes.put(stack, recipe);
		EmiPersistentData.save();
		recalculate();
	}

	public static void removeRecipe(EmiRecipe recipe) {
		disabledRecipes.add(recipe);
		EmiPersistentData.save();
		recalculate();
	}

	private static void recalculate() {
		if (tree != null) {
			tree.recalculate();
		}
	}
}
