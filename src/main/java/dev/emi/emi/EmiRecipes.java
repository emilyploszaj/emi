package dev.emi.emi;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.EmiRecipeCategoryProperties;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

public class EmiRecipes {
	public static List<Consumer<Consumer<EmiRecipe>>> lateRecipes = Lists.newArrayList();
	public static List<Predicate<EmiRecipe>> invalidators = Lists.newArrayList();

	public static List<EmiRecipeCategory> categories = Lists.newArrayList();
	public static Map<EmiRecipeCategory, List<EmiIngredient>> workstations = Maps.newHashMap();
	public static List<EmiRecipe> recipes = Lists.newArrayList();

	public static Map<Object, List<EmiRecipe>> byInput = Maps.newHashMap();
	public static Map<Object, List<EmiRecipe>> byOutput = Maps.newHashMap();
	public static Map<EmiRecipeCategory, List<EmiRecipe>> byCategory = Maps.newHashMap();
	public static Map<EmiStack, List<EmiRecipe>> byWorkstation = Maps.newHashMap();
	public static Map<Identifier, EmiRecipe> byId = Maps.newHashMap();
	
	public static void clear() {
		invalidators.clear();
		categories.clear();
		workstations.clear();
		recipes.clear();
		byInput.clear();
		byOutput.clear();
		byCategory.clear();
		byWorkstation.clear();
		byId.clear();
	}

	public static void bake() {
		long start = System.currentTimeMillis();
		Map<Object, Set<EmiRecipe>> byInput = Maps.newHashMap();
		Map<Object, Set<EmiRecipe>> byOutput = Maps.newHashMap();
		recipes.addAll(EmiData.recipes);

		categories.sort((a, b) -> EmiRecipeCategoryProperties.getOrder(a) - EmiRecipeCategoryProperties.getOrder(b));

		invalidators.addAll(EmiData.recipeFilters);
		outer:
		for (EmiRecipe recipe : recipes) {
			for (Predicate<EmiRecipe> predicate : invalidators) {
				if (predicate.test(recipe)) {
					continue outer;
				}
			}
			Identifier id = recipe.getId();
			EmiRecipeCategory category = recipe.getCategory();
			if (!categories.contains(category)) {
				EmiReloadLog.warn("Recipe " + id + " loaded with unregistered category: " + category.getId());
			}
			if (EmiConfig.logNonTagIngredients && recipe.supportsRecipeTree()) {
				Set<EmiIngredient> seen = new ObjectArraySet<>(0);
				for (EmiIngredient ingredient : recipe.getInputs()) {
					if (ingredient instanceof ListEmiIngredient && !seen.contains(ingredient)) {
						EmiReloadLog.warn("Recipe " + recipe.getId() + " uses non-tag ingredient: " + ingredient);
						seen.add(ingredient);
					}
				}
			}
			byCategory.computeIfAbsent(category, a -> Lists.newArrayList()).add(recipe);
			if (id != null) {
				if (byId.containsKey(id)) {
					EmiReloadLog.warn("Recipe loaded with duplicate id: " + id);
				}
				byId.put(id, recipe);
			}
		}
		for (EmiRecipeCategory category : byCategory.keySet()) {
			String key = EmiUtil.translateId("emi.category.", category.getId());
			if (category.getName().equals(EmiPort.translatable(key)) && !I18n.hasTranslation(key)) {
				EmiReloadLog.warn("Untranslated recipe category " + category.getId());
			}
			List<EmiRecipe> cRecipes = byCategory.get(category);
			Comparator<EmiRecipe> sort = EmiRecipeCategoryProperties.getSort(category);
			if (sort != EmiRecipeSorting.none()) {
				cRecipes = cRecipes.stream().sorted(sort).collect(Collectors.toList());
			}
			byCategory.put(category, cRecipes);
			for (EmiRecipe recipe : cRecipes) {
				getKeys(recipe.getInputs()).stream().forEach(i -> byInput
					.computeIfAbsent(i, b -> Sets.newLinkedHashSet()).add(recipe));
				getKeys(recipe.getCatalysts()).stream().forEach(i -> byInput
					.computeIfAbsent(i, b -> Sets.newLinkedHashSet()).add(recipe));
				getKeys(recipe.getOutputs()).stream().forEach(i -> byOutput
					.computeIfAbsent(i, b -> Sets.newLinkedHashSet()).add(recipe));
			}
		}
		EmiRecipes.byInput = byInput.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), m -> {
			return m.getValue().stream().toList();
		}));
		EmiRecipes.byOutput = byOutput.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), m -> {
			return m.getValue().stream().toList();
		}));
		for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : byCategory.entrySet()) {
			for (EmiIngredient ingredient : workstations.getOrDefault(entry.getKey(), List.of())) {
				for (EmiStack stack : ingredient.getEmiStacks()) {
					byWorkstation.computeIfAbsent(stack, (s) -> Lists.newArrayList()).addAll(entry.getValue());
				}
			}
		}
		EmiLog.info("Baked " + recipes.size() + " recipes in " + (System.currentTimeMillis() - start) + "ms");
	}

	public static void addCategory(EmiRecipeCategory category) {
		categories.add(category);
	}

	public static void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation) {
		workstations.computeIfAbsent(category, k -> Lists.newArrayList()).add(workstation);
	}

	public static void addRecipe(EmiRecipe recipe) {
		recipes.add(recipe);
	}

	private static Set<Object> getKeys(List<? extends EmiIngredient> list) {
		Set<Object> set = Sets.newHashSet();
		for (EmiIngredient stackSet : list) {
			for (EmiStack stack : stackSet.getEmiStacks()) {
				if (!stack.isEmpty()) {
					set.add(stack.getKey());
				}
			}
		}
		return set;
	}
}
