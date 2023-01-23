package dev.emi.emi.sidebar;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiFavorite;
import dev.emi.emi.EmiFavorites;
import dev.emi.emi.EmiPersistentData;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.chess.EmiChess;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class EmiSidebars {
	public static List<EmiIngredient> craftables = List.of();
	public static List<EmiIngredient> craftHistory = Lists.newArrayList();
	public static Set<SidebarType> dirty = Sets.newHashSet();

	public static List<? extends EmiIngredient> getStacks(SidebarType type) {
		return switch (type) {
			case INDEX -> EmiStackList.stacks;
			case CRAFTABLES -> craftables;
			case FAVORITES -> EmiFavorites.favoriteSidebar;
			case CRAFT_HISTORY -> craftHistory;
			case CHESS -> EmiChess.SIDEBAR;
			default -> List.of();
		};
	}

	public static void craft(EmiRecipe recipe) {
		if (!recipe.getOutputs().isEmpty()) {
			if (craftHistory.size() >= 1 && EmiApi.getRecipeContext(craftHistory.get(0)).equals(recipe)) {
				return;
			}
			EmiIngredient stack = new EmiFavorite.Craftable(recipe);
			craftHistory.removeIf(i -> i instanceof EmiFavorite.Craftable c && c.getRecipe().equals(recipe));
			craftHistory.add(0, stack);
			EmiPersistentData.save();
			EmiScreenManager.forPanel(SidebarType.CRAFT_HISTORY, p -> p.batcher.repopulate());
		}
	}

	public static void save(JsonObject json) {
		JsonArray arr = new JsonArray();
		for (int i = 0; i < 1024; i++) {
			if (i >= craftHistory.size()) {
				break;
			}
			EmiIngredient stack = craftHistory.get(i);
			EmiRecipe recipe = EmiApi.getRecipeContext(stack);
			if (recipe != null && recipe.getId() != null) {
				arr.add(recipe.getId().toString());
			}
		}
		System.out.println(arr);
		json.add("craft_history", arr);
	}

	public static void load(JsonObject json) {
		craftHistory.clear();
		if (JsonHelper.hasArray(json, "craft_history")) {
			for (JsonElement el : JsonHelper.getArray(json, "craft_history")) {
				if (JsonHelper.isString(el)) {
					String s = el.getAsString();
					if (Identifier.isValid(s)) {
						Identifier id = new Identifier(s);
						EmiRecipe recipe = EmiRecipes.byId.get(id);
						if (recipe != null) {
							craftHistory.add(new EmiFavorite.Craftable(recipe));
						}
					}
				}
			}
		}
	}
}
