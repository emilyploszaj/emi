package dev.emi.emi.registry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.EmiRecipeCategoryProperties;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

public class EmiRecipes {
	public static EmiRecipeManager manager = Manager.EMPTY;
	public static List<Consumer<Consumer<EmiRecipe>>> lateRecipes = Lists.newArrayList();
	public static List<Predicate<EmiRecipe>> invalidators = Lists.newArrayList();

	public static List<EmiRecipeCategory> categories = Lists.newArrayList();
	private static Map<EmiRecipeCategory, List<EmiIngredient>> workstations = Maps.newHashMap();
	private static List<EmiRecipe> recipes = Lists.newArrayList();

	public static Map<EmiStack, List<EmiRecipe>> byWorkstation = Maps.newHashMap();
	
	public static void clear() {
		lateRecipes.clear();
		invalidators.clear();
		categories.clear();
		workstations.clear();
		recipes.clear();
		byWorkstation.clear();
		manager = Manager.EMPTY;
	}

	public static void bake() {
		long start = System.currentTimeMillis();
		recipes.addAll(EmiData.recipes.stream().map(Supplier::get).toList());
		categories.sort((a, b) -> EmiRecipeCategoryProperties.getOrder(a) - EmiRecipeCategoryProperties.getOrder(b));
		invalidators.addAll(EmiData.recipeFilters);
		List<EmiRecipe> filtered = recipes.stream().filter(r -> {
			for (Predicate<EmiRecipe> predicate : invalidators) {
				if (predicate.test(r)) {
					return false;
				}
			}
			return true;
		}).toList();
		manager = new Manager(categories, workstations, filtered);
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

	private static class Manager implements EmiRecipeManager {
		public static final EmiRecipeManager EMPTY = new Manager();
		private final List<EmiRecipeCategory> categories;
		private final Map<EmiRecipeCategory, List<EmiIngredient>> workstations;
		private final List<EmiRecipe> recipes;
		private Map<EmiStack, List<EmiRecipe>> byInput = Maps.newHashMap();
		private Map<EmiStack, List<EmiRecipe>> byOutput = Maps.newHashMap();
		private Map<EmiRecipeCategory, List<EmiRecipe>> byCategory = Maps.newHashMap();
		private Map<Identifier, EmiRecipe> byId = Maps.newHashMap();

		private Manager() {
			this.categories = List.of();
			this.workstations = Map.of();
			this.recipes = List.of();
		}

		public Manager(List<EmiRecipeCategory> categories, Map<EmiRecipeCategory, List<EmiIngredient>> workstations, List<EmiRecipe> recipes) {
			this.categories = List.copyOf(categories);
			this.workstations = workstations;
			this.recipes = List.copyOf(recipes);

			Map<EmiStack, Set<EmiRecipe>> byInput = Maps.newHashMap();
			Map<EmiStack, Set<EmiRecipe>> byOutput = Maps.newHashMap();
	
			Object2IntMap<Identifier> duplicateIds = new Object2IntOpenHashMap<>();
			for (EmiRecipe recipe : recipes) {
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
						duplicateIds.put(id, duplicateIds.getOrDefault(id, 1) + 1);
					}
					byId.put(id, recipe);
				}
			}
	
			for (Identifier id : duplicateIds.keySet()) {
				EmiReloadLog.warn(duplicateIds.getInt(id) + " recipes loaded with the same id: " + id);
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
					recipe.getInputs().stream().flatMap(i -> i.getEmiStacks().stream()).forEach(i -> byInput
						.computeIfAbsent(i, b -> Sets.newLinkedHashSet()).add(recipe));
					recipe.getCatalysts().stream().flatMap(i -> i.getEmiStacks().stream()).forEach(i -> byInput
						.computeIfAbsent(i, b -> Sets.newLinkedHashSet()).add(recipe));
					recipe.getOutputs().stream().forEach(i -> byOutput
						.computeIfAbsent(i, b -> Sets.newLinkedHashSet()).add(recipe));
				}
			}
			this.byInput = byInput.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), m -> {
				return m.getValue().stream().toList();
			}));
			this.byOutput = byOutput.entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), m -> {
				return m.getValue().stream().toList();
			}));
			for (EmiRecipeCategory category : workstations.keySet()) {
				workstations.put(category, workstations.get(category).stream().distinct().toList());
			}
			for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : byCategory.entrySet()) {
				for (EmiIngredient ingredient : workstations.getOrDefault(entry.getKey(), List.of())) {
					for (EmiStack stack : ingredient.getEmiStacks()) {
						byWorkstation.computeIfAbsent(stack, (s) -> Lists.newArrayList()).addAll(entry.getValue());
					}
				}
			}
		}

		@Override
		public List<EmiRecipeCategory> getCategories() {
			return categories;
		}

		@Override
		public List<EmiIngredient> getWorkstations(EmiRecipeCategory category) {
			return workstations.getOrDefault(category, List.of());
		}

		@Override
		public List<EmiRecipe> getRecipes() {
			return recipes;
		}

		@Override
		public List<EmiRecipe> getRecipes(EmiRecipeCategory category) {
			return byCategory.getOrDefault(category, List.of());
		}

		@Override
		public @Nullable EmiRecipe getRecipe(Identifier id) {
			return byId.getOrDefault(id, null);
		}

		@Override
		public List<EmiRecipe> getRecipesByInput(EmiStack stack) {
			return byInput.getOrDefault(stack, List.of());
		}

		@Override
		public List<EmiRecipe> getRecipesByOutput(EmiStack stack) {
			return byOutput.getOrDefault(stack, List.of());
		}
	}
}
