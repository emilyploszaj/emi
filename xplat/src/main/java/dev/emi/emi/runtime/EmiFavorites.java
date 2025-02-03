package dev.emi.emi.runtime;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.ChanceMaterialCost;
import dev.emi.emi.bom.FlatMaterialCost;
import dev.emi.emi.bom.MaterialNode;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class EmiFavorites {
	public static List<EmiFavorite> favorites = Lists.newArrayList();
	public static List<EmiFavorite.Synthetic> syntheticFavorites = Lists.newArrayList();
	public static List<EmiFavorite> favoriteSidebar = new CompoundList<>(favorites, syntheticFavorites);

	public static JsonArray save() {
		JsonArray arr = new JsonArray();
		for (EmiFavorite fav : favorites) {
			JsonElement stack = EmiIngredientSerializer.getSerialized(fav.getStack());
			if (stack != null) {
				JsonObject obj = new JsonObject();
				obj.add("stack", stack);
				if (fav.getRecipe() != null && fav.getRecipe().getId() != null) {
					obj.addProperty("recipe", fav.getRecipe().getId().toString());
				}
				arr.add(obj);
			}
		}
		return arr;
	}

	public static void load(JsonArray arr) {
		favorites.clear();
		for (JsonElement el : arr) {
			if (el.isJsonObject()) {
				JsonObject json = el.getAsJsonObject();
				EmiRecipe recipe = null;
				if (JsonHelper.hasString(json, "recipe")) {
					recipe = EmiApi.getRecipeManager().getRecipe(EmiPort.id(JsonHelper.getString(json, "recipe")));
				}
				if (JsonHelper.hasElement(json, "stack")) {
					EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(json.get("stack"));
					if (ingredient.isEmpty()) {
						continue;
					}
					if (ingredient instanceof EmiStack es) {
						ingredient = es.copy();
					}
					favorites.add(new EmiFavorite(ingredient, recipe));
				}
			}
		}
	}

	public static boolean canFavorite(EmiIngredient stack, EmiRecipe recipe) {
		stack = EmiIngredientSerializer.getDeserialized(EmiIngredientSerializer.getSerialized(stack));
		if (stack.isEmpty()) {
			return false;
		}
		if (recipe != null) {
			return recipe.getId() != null;
		}
		return true;
	}

	private static int indexOf(EmiIngredient stack) {
		for (int i = 0; i < favorites.size(); i++) {
			if (favorites.get(i).strictEquals(stack) && favorites.get(i).getRecipe() == EmiApi.getRecipeContext(stack)) {
				return i;
			}
		}
		return -1;
	}

	public static boolean removeFavorite(EmiIngredient stack) {
		int index = indexOf(stack);
		if (index != -1) {
			favorites.remove(index);
			return true;
		}
		return false;
	}

	public static void addFavorite(EmiIngredient stack) {
		addFavorite(stack, null);
	}

	public static void addFavoriteAt(EmiIngredient stack, int offset) {
		if (stack instanceof EmiFavorite.Synthetic) {
			return;
		}
		if (stack instanceof EmiFavorite.Craftable craftable) {
			stack = craftable.stack;
		}
		EmiFavorite favorite;
		if (stack instanceof EmiFavorite fav) {
			int original = indexOf(stack);
			if (original != -1) {
				if (original < offset) {
					offset--;
				}
				favorites.remove(original);
			}
			favorite = fav;
		} else {
			stack = EmiIngredientSerializer.getDeserialized(EmiIngredientSerializer.getSerialized(stack));
			if (stack.isEmpty()) {
				return;
			}
			for (int i = 0; i < favorites.size(); i++) {
				EmiFavorite fav = favorites.get(i);
				if (fav.getRecipe() == null && fav.strictEquals(stack)) {
					favorites.remove(i--);
				}
			}
			favorite = new EmiFavorite(stack, null);
		}
		if (offset < 0) {
			offset = 0;
		}
		if (offset >= favorites.size()) {
			favorites.add(favorite);
		} else {
			favorites.add(offset, favorite);
		}
		EmiPersistentData.save();
	}

	public static void addFavorite(EmiIngredient stack, EmiRecipe context) {
		if (stack instanceof EmiFavorite.Synthetic) {
			return;
		}
		if (stack instanceof EmiFavorite.Craftable craftable) {
			stack = craftable.stack;
		}
		if (stack instanceof EmiFavorite f) {
			if (!removeFavorite(f)) {
				favorites.add(f);
			}
		} else {
			stack = EmiIngredientSerializer.getDeserialized(EmiIngredientSerializer.getSerialized(stack));
			if (stack instanceof EmiStack es && context != null && context.getId() != null) {
				es = es.copy();
				if (es instanceof ItemEmiStack ies) {
					ies.getItemStack().setCount(1);
				}
				if (!es.isEmpty()) {
					for (int i = 0; i < favorites.size(); i++) {
						EmiFavorite fav = favorites.get(i);
						if (fav.getRecipe() == context && fav.strictEquals(es)) {
							return;
						}
					}
					favorites.add(new EmiFavorite(es, context));
				}
			} else {
				if (stack.isEmpty()) {
					return;
				}
				for (int i = 0; i < favorites.size(); i++) {
					EmiFavorite fav = favorites.get(i);
					if (fav.getRecipe() == null && fav.strictEquals(stack)) {
						return;
					}
				}
				favorites.add(new EmiFavorite(stack, null));
			}
		}
		EmiPersistentData.save();
	}

	public static void updateSynthetic(EmiPlayerInventory inv) {
		syntheticFavorites.clear();
		if (BoM.tree != null && BoM.craftingMode) {
			BoM.tree.calculateCost();
			Map<EmiIngredient, ChanceMaterialCost> chancedCosts = Maps.newHashMap(BoM.tree.cost.chanceCosts);
			BoM.tree.calculateProgress(inv);
			Object2LongMap<EmiRecipe> batches = new Object2LongLinkedOpenHashMap<>();
			Object2LongMap<EmiRecipe> amounts = new Object2LongLinkedOpenHashMap<>();
			countRecipes(batches, amounts, BoM.tree.goal);
			boolean hasSomething = false;
			for (Object2LongMap.Entry<EmiRecipe> entry : batches.object2LongEntrySet()) {
				EmiRecipe recipe = entry.getKey();
				long amount = amounts.getOrDefault(recipe, 0);
				long batch = entry.getLongValue();
				if (amount == 0) {
					continue;
				}
				hasSomething = true;
				int state = 0;
				if (inv.canCraft(recipe, batch)) {
					state = 2;
				} else if (inv.canCraft(recipe)) {
					state = 1;
				}
				syntheticFavorites.add(new EmiFavorite.Synthetic(recipe, batch, amount, state));
			}
			if (!hasSomething) {
				BoM.craftingMode = false;
			} else {
				for (FlatMaterialCost cost : BoM.tree.cost.costs.values()) {
					if (cost.amount > 0) {
						syntheticFavorites.add(new EmiFavorite.Synthetic(cost.ingredient, cost.amount, cost.amount));
					}
				}
				for (ChanceMaterialCost cost : BoM.tree.cost.chanceCosts.values()) {
					if (cost.getEffectiveAmount() > 0) {
						long needed = cost.getEffectiveAmount();
						if (chancedCosts.containsKey(cost.ingredient)) {
							ChanceMaterialCost original = chancedCosts.get(cost.ingredient);
							long done = (long) Math.ceil(original.amount * original.chance - cost.amount * cost.chance);
							needed = original.getEffectiveAmount() - done;
						}
						if (needed > 0) {
							syntheticFavorites.add(new EmiFavorite.Synthetic(cost.ingredient, needed, needed));
						}
					}
				}
			}
		}
	}

	public static void countRecipes(Object2LongMap<EmiRecipe> batches, Object2LongMap<EmiRecipe> amounts, MaterialNode node) {
		if (node.recipe instanceof EmiResolutionRecipe recipe) {
			countRecipes(batches, amounts, node.children.get(0));
			return;
		}
		// Include empty costs for proper sorting
		if (node.recipe != null) {
			long amount = node.neededBatches;
			if (batches.containsKey(node.recipe)) {
				// Remove?
				amount += batches.getLong(node.recipe);
				batches.removeLong(node.recipe);
			}
			batches.put(node.recipe, amount);
			amount = node.totalNeeded;
			if (amounts.containsKey(node.recipe)) {
				// Remove?
				amount += amounts.getLong(node.recipe);
				amounts.removeLong(node.recipe);
			}
			amounts.put(node.recipe, amount);
			for (MaterialNode child : node.children) {
				countRecipes(batches, amounts, child);
			}
		}
	}

	private static class CompoundList<T> extends AbstractList<T> {
		private List<? extends T> a, b;

		public CompoundList(List<? extends T> a, List<? extends T> b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public T get(int index) {
			if (index >= a.size()) {
				return b.get(index - a.size());
			}
			return a.get(index);
		}

		@Override
		public int size() {
			return a.size() + b.size();
		}
	}
}
