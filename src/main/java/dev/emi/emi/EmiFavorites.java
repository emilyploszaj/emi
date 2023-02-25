package dev.emi.emi;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
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
	public static List<EmiFavorite> syntheticFavorites = Lists.newArrayList();
	public static List<EmiFavorite> favoriteSidebar = new CompoundList<>(favorites, syntheticFavorites);

	public static JsonArray save() {
		JsonArray arr = new JsonArray();
		for (EmiFavorite fav : favorites) {
			JsonObject stack = EmiStackSerializer.serialize(fav.getStack());
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
					recipe = EmiRecipes.byId.get(new Identifier(JsonHelper.getString(json, "recipe")));
				}
				if (JsonHelper.hasJsonObject(json, "stack")) {
					EmiIngredient ingredient = EmiStackSerializer.deserialize(JsonHelper.getObject(json, "stack"));
					if (ingredient.isEmpty()) {
						continue;
					}
					if (ingredient instanceof EmiStack es) {
						ingredient = es.copy().comparison(c -> c.copy().nbt(true).amount(false).build());
					}
					favorites.add(new EmiFavorite(ingredient, recipe));
				}
			}
		}
	}

	public static void addFavorite(EmiIngredient stack) {
		addFavorite(stack, null);
	}

	public static void addFavoriteAt(EmiIngredient stack, int offset) {
		if (stack instanceof EmiFavorite.Synthetic) {
			return;
		}
		EmiFavorite favorite;
		if (stack instanceof EmiFavorite fav) {
			int original = favorites.indexOf(fav);
			if (original != -1) {
				if (original < offset) {
					offset--;
				}
				favorites.remove(original);
			}
			favorite = fav;
		} else {
			stack = EmiStackSerializer.deserialize(EmiStackSerializer.serialize(stack));
			if (stack.isEmpty()) {
				return;
			}
			for (int i = 0; i < favorites.size(); i++) {
				EmiFavorite fav = favorites.get(i);
				if (fav.getRecipe() == null && fav.getStack().equals(stack)) {
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
		if (stack instanceof EmiFavorite f) {
			if (!favorites.remove(f)) {
				favorites.add(f);
			}
		} else {
			stack = EmiStackSerializer.deserialize(EmiStackSerializer.serialize(stack));
			if (stack instanceof EmiStack es) {
				es = es.copy().comparison(c -> c.copy().nbt(true).amount(false).build());
				if (context == null && es instanceof ItemEmiStack ies) {
					ies.getItemStack().setCount(1);
				}
				if (!es.isEmpty()) {
					for (int i = 0; i < favorites.size(); i++) {
						EmiFavorite fav = favorites.get(i);
						if (fav.getRecipe() == context && fav.getStack().equals(es)) {
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
					if (fav.getRecipe() == null && fav.getStack().equals(stack)) {
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
			countRecipes(batches, BoM.tree.goal);
			boolean hasSomething = false;
			for (Object2LongMap.Entry<EmiRecipe> entry : batches.object2LongEntrySet()) {
				EmiRecipe recipe = entry.getKey();
				long amount = entry.getLongValue();
				if (amount == 0) {
					continue;
				}
				hasSomething = true;
				int state = 0;
				if (inv.canCraft(recipe, amount)) {
					state = 2;
				} else if (inv.canCraft(recipe)) {
					state = 1;
				}
				syntheticFavorites.add(new EmiFavorite.Synthetic(recipe, amount, state));
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

	public static void countRecipes(Object2LongMap<EmiRecipe> batches, MaterialNode node) {
		if (node.recipe instanceof EmiResolutionRecipe recipe) {
			countRecipes(batches, node.children.get(0));
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
			for (MaterialNode child : node.children) {
				countRecipes(batches, child);
			}
		}
	}

	private static class CompoundList<T> extends AbstractList<T> {
		private List<T> a, b;

		public CompoundList(List<T> a, List<T> b) {
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
