package dev.emi.emi.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class RecipeDefaults {
	public final List<Identifier> added = Lists.newArrayList();
	public final List<Resolution> resolutions = Lists.newArrayList();
	public final List<Tag> tags = Lists.newArrayList();

	public void add(Identifier id) {
		added.add(id);
	}

	public void add(Identifier id, JsonArray arr) {
		resolutions.add(new Resolution(id, Lists.newArrayList(arr)));
	}

	public void addTag(JsonElement tag, JsonElement stack) {
		tags.add(new Tag(tag, stack));
	}

	public void remove(Identifier id) {
		added.remove(id);
		resolutions.removeIf(r -> r.recipe.equals(id));
	}

	public void clear() {
		added.clear();
		resolutions.clear();
		tags.clear();
	}

	public Map<EmiIngredient, EmiRecipe> bake() {
		Map<EmiIngredient, EmiRecipe> map = Maps.newHashMap();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) {
			return map;
		}
		EmiRecipeManager manager = EmiApi.getRecipeManager();
		for (Identifier id : added) {
			EmiRecipe recipe = manager.getRecipe(id);
			if (recipe != null) {
				for (EmiIngredient stack : recipe.getOutputs()) {
					map.put(stack, recipe);
				}
			}
		}
		for (Resolution r : resolutions) {
			EmiRecipe recipe = manager.getRecipe(r.recipe);
			if (recipe != null) {
				for (JsonElement el : r.stacks) {
					EmiIngredient stack = EmiIngredientSerializer.getDeserialized(el);
					if (!stack.isEmpty()) {
						map.put(stack, recipe);
					}
				}
			}
		}
		for (Tag t : tags) {
			EmiIngredient tag = EmiIngredientSerializer.getDeserialized(t.tag);
			EmiIngredient stack = EmiIngredientSerializer.getDeserialized(t.stack);
			if (!tag.isEmpty() && !stack.isEmpty() && stack.getEmiStacks().size() == 1 && tag.getEmiStacks().containsAll(stack.getEmiStacks())) {
				map.put(tag, new EmiResolutionRecipe(tag, stack.getEmiStacks().get(0)));
			}
		}
		return map;
	}

	public static record Resolution(Identifier recipe, List<JsonElement> stacks) {
	}

	public static record Tag(JsonElement tag, JsonElement stack) {
	}
}
