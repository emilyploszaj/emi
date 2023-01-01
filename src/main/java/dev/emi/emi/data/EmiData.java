package dev.emi.emi.data;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiStackSerializer;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class EmiData {
	public static Map<String, EmiRecipeCategoryProperties> categoryPriorities = Map.of();
	public static List<Predicate<EmiRecipe>> recipeFilters = List.of();
	public static List<IndexStackData> stackData;
	
	public static void init() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new RecipeDefaultLoader());
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new EmiTagExclusionsLoader());
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new EmiDataLoader<Map<String, EmiRecipeCategoryProperties>>(
				new Identifier("emi:category_properties"), "category/properties", Maps::newHashMap,
				(map, json) -> {
					for (String k : json.keySet()) {
						if (JsonHelper.hasJsonObject(json, k)) {
							EmiRecipeCategoryProperties props = map.computeIfAbsent(k, s -> new EmiRecipeCategoryProperties());
							JsonObject val = json.getAsJsonObject(k);
							if (JsonHelper.hasNumber(val, "order")) {
								props.order = val.get("order").getAsInt();
							}
							if (JsonHelper.hasString(val, "sort")) {
								switch (JsonHelper.getString(val, "sort")) {
									case "none":
										props.sort = EmiRecipeSorting.none();
										break;
									case "input_then_output":
										props.sort = EmiRecipeSorting.compareInputThenOutput();
										break;
									case "output_then_input":
										props.sort = EmiRecipeSorting.compareOutputThenInput();
										break;
								}
							}
						}
					}
				}, map -> categoryPriorities = map));
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new EmiDataLoader<List<Predicate<EmiRecipe>>>(
				new Identifier("emi:recipe_filters"), "recipe/filters", Lists::newArrayList,
				(list, json) -> {
					JsonArray arr = JsonHelper.getArray(json, "filters", new JsonArray());
					for (JsonElement el : arr) {
						if (el.isJsonObject()) {
							JsonObject obj = el.getAsJsonObject();
							List<Predicate<EmiRecipe>> predicates = Lists.newArrayList();
							if (JsonHelper.hasString(obj, "id")) {
								String id = JsonHelper.getString(obj, "id");
								if (id.startsWith("/") && id.endsWith("/")) {
									Pattern pat = Pattern.compile(id.substring(1, id.length() - 1));
									predicates.add(r -> {
										String rid = r.getId() == null ? "null" : r.getId().toString();
										return pat.matcher(rid).find();
									});
								} else {
									predicates.add(r -> {
										String rid = r.getId() == null ? "null" : r.getId().toString();
										return rid.equals(id);
									});
								}
							}
							if (JsonHelper.hasString(obj, "category")) {
								String id = JsonHelper.getString(obj, "category");
								if (id.startsWith("/") && id.endsWith("/")) {
									Pattern pat = Pattern.compile(id.substring(1, id.length() - 1));
									predicates.add(r -> {
										return pat.matcher(r.getCategory().getId().toString()).find();
									});
								} else {
									predicates.add(r -> {
										return r.getCategory().getId().toString().equals(id);
									});
								}
							}
							if (predicates.size() <= 1) {
								list.addAll(predicates);
							} else {
								list.add(r -> {
									for (Predicate<EmiRecipe> p : predicates) {
										if (!p.test(r)) {
											return false;
										}
									}
									return true;
								});
							}
						}
					}
				}, list -> recipeFilters = list));
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new EmiDataLoader<List<IndexStackData>>(
				new Identifier("emi:index_stacks"), "index/stacks", Lists::newArrayList,
				(list, json) -> {
					List<IndexStackData.Added> added = Lists.newArrayList();
					List<EmiIngredient> removed = Lists.newArrayList();
					if (JsonHelper.hasArray(json, "added")) {
						for (JsonElement el : json.getAsJsonArray("added")) {
							if (el.isJsonObject()) {
								JsonObject obj = el.getAsJsonObject();
								EmiIngredient stack = EmiStackSerializer.deserialize(obj.get("stack"));
								EmiIngredient after = EmiStack.EMPTY;
								if (obj.has("after")) {
									after = EmiStackSerializer.deserialize(obj.get("after"));
								}
								added.add(new IndexStackData.Added(stack, after));
							}
						}
					}
					if (JsonHelper.hasArray(json, "removed")) {
						for (JsonElement el : json.getAsJsonArray("removed")) {
							removed.add(EmiStackSerializer.deserialize(el));
						}
					}
					list.add(new IndexStackData(added, removed));
				}, list -> stackData = list));
	}
}
