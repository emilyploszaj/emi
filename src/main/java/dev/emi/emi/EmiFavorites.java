package dev.emi.emi;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;

public class EmiFavorites {
	public static List<EmiFavorite> favorites = Lists.newArrayList();

	public static void addFavorite(EmiIngredient stack) {
		addFavorite(stack, null);
	}

	public static void addFavorite(EmiIngredient stack, EmiRecipe context) {
		if (context != null && !EmiRecipeFiller.RECIPE_HANDLERS.containsKey(context.getCategory())) {
			context = null;
		}
		if (stack instanceof EmiFavorite) {
			favorites.remove(stack);
		} else if (stack instanceof EmiStack es) {
			es = es.copy().comparison(c -> c.nbt(true).amount(true));
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
	}
}
