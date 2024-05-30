package dev.emi.emi.runtime;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.chess.EmiChess;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class EmiSidebars {
	public static List<EmiIngredient> craftables = List.of();
	public static List<EmiIngredient> lookupHistory = Lists.newArrayList();
	public static List<EmiIngredient> craftHistory = Lists.newArrayList();

	public static List<? extends EmiIngredient> getStacks(SidebarType type) {
		return switch (type) {
			case INDEX -> EmiConfig.editMode ? EmiStackList.stacks : EmiStackList.filteredStacks;
			case CRAFTABLES -> craftables;
			case FAVORITES -> EmiFavorites.favoriteSidebar;
			case LOOKUP_HISTORY -> lookupHistory;
			case CRAFT_HISTORY -> craftHistory;
			case CHESS -> EmiChess.SIDEBAR;
			default -> List.of();
		};
	}

	public static void lookup(EmiIngredient stack) {
		if (!stack.isEmpty()) {
			if (lookupHistory.size() >= 1 && lookupHistory.get(0).equals(stack)) {
				return;
			}
			lookupHistory.remove(stack);
			lookupHistory.add(0, stack);
			EmiPersistentData.save();
			EmiScreenManager.repopulatePanels(SidebarType.LOOKUP_HISTORY);
		}
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
			EmiScreenManager.repopulatePanels(SidebarType.CRAFT_HISTORY);
		}
	}

	public static void save(JsonObject json) {
		JsonArray arr = new JsonArray();
		for (int i = 0; i < 1024; i++) {
			if (i >= lookupHistory.size()) {
				break;
			}
			EmiIngredient stack = lookupHistory.get(i);
			JsonElement el = EmiIngredientSerializer.getSerialized(stack);
			if (el != null && !el.isJsonNull()) {
				arr.add(el);
			}
		}
		json.add("lookup_history", arr);

		arr = new JsonArray();
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
		json.add("craft_history", arr);
	}

	public static void load(JsonObject json) {
		lookupHistory.clear();
		if (JsonHelper.hasArray(json, "lookup_history")) {
			for (JsonElement el : JsonHelper.getArray(json, "lookup_history")) {
				EmiIngredient stack = EmiIngredientSerializer.getDeserialized(el);
				if (!stack.isEmpty()) {
					lookupHistory.add(stack);
				}
			}
		}

		craftHistory.clear();
		if (JsonHelper.hasArray(json, "craft_history")) {
			for (JsonElement el : JsonHelper.getArray(json, "craft_history")) {
				if (JsonHelper.isString(el)) {
					String s = el.getAsString();
					Identifier id = Identifier.tryParse(s);
					if (id != null) {
						EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
						if (recipe != null) {
							craftHistory.add(new EmiFavorite.Craftable(recipe));
						}
					}
				}
			}
		}
	}
}
