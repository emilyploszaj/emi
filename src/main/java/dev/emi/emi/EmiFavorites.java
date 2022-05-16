package dev.emi.emi;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class EmiFavorites {
	public static List<EmiFavorite> favorites = Lists.newArrayList();

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
					addFavorite(ingredient, recipe);
				}
			}
		}
	}

	public static void addFavorite(EmiIngredient stack) {
		addFavorite(stack, null);
	}

	public static void addFavoriteAt(EmiIngredient stack, int offset) {
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
		} else if (stack instanceof EmiStack es) {
			for (int i = 0; i < favorites.size(); i++) {
				EmiFavorite fav = favorites.get(i);
				if (fav.getRecipe() == null && fav.getStack().isEqual(es)) {
					favorites.remove(i--);
				}
			}
			favorite = new EmiFavorite(es, null);
		} else {
			return;
		}
		if (offset < 0) {
			offset = 0;
		}
		if (offset >= favorites.size()) {
			favorites.add(favorite);
		} else {
			favorites.add(offset, favorite);
		}
	}

	public static void addFavorite(EmiIngredient stack, EmiRecipe context) {
		if (context != null && !EmiRecipeFiller.RECIPE_HANDLERS.containsKey(context.getCategory())) {
			context = null;
		}
		if (stack instanceof EmiFavorite f) {
			if (!favorites.remove(f)) {
				favorites.add(f);
			}
		} else if (stack instanceof EmiStack es) {
			es = es.copy().comparison(c -> c.copy().nbt(true).amount(false).build());
			if (context == null && es instanceof ItemEmiStack ies) {
				ies.getItemStack().setCount(1);
			}
			if (!es.isEmpty()) {
				for (int i = 0; i < favorites.size(); i++) {
					EmiFavorite fav = favorites.get(i);
					if (fav.getRecipe() == context && fav.getStack().isEqual(es)) {
						return;
					}
				}
				favorites.add(new EmiFavorite(es, context));
			}
		}
		EmiPersistentData.save();
	}
}
