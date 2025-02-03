package dev.emi.emi.registry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.EmiRecipeCategoryProperties;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.runtime.dev.EmiDev;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

public class EmiRecipes {
	public static volatile Worker activeWorker = null;
	public static EmiRecipeManager manager = Manager.EMPTY;
	public static List<Consumer<Consumer<EmiRecipe>>> lateRecipes = Lists.newArrayList();
	public static List<Predicate<EmiRecipe>> invalidators = Lists.newArrayList();

	public static List<EmiRecipeCategory> categories = Lists.newArrayList();
	private static Map<EmiRecipeCategory, List<EmiIngredient>> workstations = Maps.newHashMap();
	private static List<EmiRecipe> recipes = Lists.newArrayList();

	public static Map<EmiStack, List<EmiRecipe>> byWorkstation = Maps.newHashMap();
	public static List<EmiRecipeDecorator> decorators = Lists.newArrayList();
	
	public static void clear() {
		setWorker(null);
		lateRecipes.clear();
		invalidators.clear();
		categories.clear();
		workstations.clear();
		recipes.clear();
		byWorkstation.clear();
		decorators.clear();
		manager = Manager.EMPTY;
	}

	public static void bake() {
		long start = System.currentTimeMillis();
		recipes.addAll(EmiData.recipes.stream().map(r -> r.get()).toList());
		categories.sort((a, b) -> EmiRecipeCategoryProperties.getOrder(a) - EmiRecipeCategoryProperties.getOrder(b));
		invalidators.addAll(EmiData.recipeFilters);

		invalidators.add(r -> {
			for (EmiIngredient i : Iterables.concat(r.getInputs(), r.getOutputs(), r.getCatalysts())) {
				if (EmiHidden.isDisabled(i)) {
					return true;
				}
			}
			return false;
		});

		List<EmiRecipe> filtered = recipes.stream().filter(r -> {
			for (Predicate<EmiRecipe> predicate : invalidators) {
				if (predicate.test(r)) {
					return false;
				}
			}
			return true;
		}).toList();
		Map<EmiRecipeCategory, List<EmiIngredient>> filteredWorkstations = Maps.newHashMap();
		for (Map.Entry<EmiRecipeCategory, List<EmiIngredient>> entry : workstations.entrySet()) {
			List<EmiIngredient> w = entry.getValue().stream().filter(s -> !EmiHidden.isDisabled(s)).toList();
			if (!w.isEmpty()) {
				filteredWorkstations.put(entry.getKey(), w);
			}
		}
		manager = new Manager(categories, filteredWorkstations, filtered, false);
		setWorker(new Worker(categories, filteredWorkstations, filtered));
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

	private static synchronized void setWorker(Worker worker) {
		activeWorker = worker;
		if (worker != null) {
			new Thread(activeWorker).start();
		}
	}

	private static class Manager implements EmiRecipeManager {
		public static final EmiRecipeManager EMPTY = new Manager();
		private final List<EmiRecipeCategory> categories;
		private final Map<EmiRecipeCategory, List<EmiIngredient>> workstations;
		private final List<EmiRecipe> recipes;
		private Map<EmiStack, List<EmiRecipe>> byInput = new Object2ObjectOpenCustomHashMap<>(new EmiStackList.ComparisonHashStrategy());
		private Map<EmiStack, List<EmiRecipe>> byOutput = new Object2ObjectOpenCustomHashMap<>(new EmiStackList.ComparisonHashStrategy());
		private Map<EmiRecipeCategory, List<EmiRecipe>> byCategory = Maps.newHashMap();
		private Map<Identifier, EmiRecipe> byId = Maps.newHashMap();

		private Manager() {
			this.categories = List.of();
			this.workstations = Map.of();
			this.recipes = List.of();
		}

		public Manager(List<EmiRecipeCategory> categories, Map<EmiRecipeCategory, List<EmiIngredient>> workstations, List<EmiRecipe> recipes, boolean doSort) {
			this.categories = categories.stream().distinct().toList();
			this.workstations = workstations;
			this.recipes = List.copyOf(recipes);
	
			Object2IntMap<Identifier> duplicateIds = new Object2IntOpenHashMap<>();
			Set<Identifier> incorrectIds = new ObjectArraySet<>();
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
					} else {
						byId.put(id, recipe);
					}

					MinecraftClient client = MinecraftClient.getInstance();
					if (!id.getPath().startsWith("/") && client.world != null && client.world.getRecipeManager().get(id).isEmpty()) {
						incorrectIds.add(id);
					}
				}
			}
	
			if (EmiConfig.devMode) {
				for (Identifier id : duplicateIds.keySet()) {
					EmiReloadLog.warn(duplicateIds.getInt(id) + " recipes loaded with the same id: " + id);
				}
				for (Identifier id : incorrectIds) {
					EmiReloadLog.warn("Recipe " + id + " not present in recipe manager. Consider prefixing its path with '/' if it is synthetic.");
				}
			}
	
			Map<EmiStack, Set<EmiRecipe>> byInput = new Object2ObjectOpenCustomHashMap<>(new EmiStackList.ComparisonHashStrategy());
			Map<EmiStack, Set<EmiRecipe>> byOutput = new Object2ObjectOpenCustomHashMap<>(new EmiStackList.ComparisonHashStrategy());

			for (EmiRecipeCategory category : byCategory.keySet()) {
				String key = EmiUtil.translateId("emi.category.", category.getId());
				if (category.getName().equals(EmiPort.translatable(key)) && !I18n.hasTranslation(key)) {
					EmiReloadLog.warn("Untranslated recipe category " + category.getId());
				}
				List<EmiRecipe> cRecipes = byCategory.get(category);
				Comparator<EmiRecipe> sort = EmiRecipeCategoryProperties.getSort(category);
				if (doSort && sort != EmiRecipeSorting.none()) {
					cRecipes = cRecipes.stream().sorted(sort).collect(Collectors.toList());
					EmiRecipeSorter.clear();
				}
				byCategory.put(category, cRecipes);
				for (EmiRecipe recipe : cRecipes) {
					recipe.getInputs().stream().flatMap(i -> i.getEmiStacks().stream()).forEach(i -> {
						byInput.computeIfAbsent(i.copy(), b -> Sets.newLinkedHashSet()).add(recipe);
					});
					recipe.getCatalysts().stream().flatMap(i -> i.getEmiStacks().stream()).forEach(i -> {
						byInput.computeIfAbsent(i.copy(), b -> Sets.newLinkedHashSet()).add(recipe);
					});
					recipe.getOutputs().stream().forEach(i -> {
						byOutput.computeIfAbsent(i.copy(), b -> Sets.newLinkedHashSet()).add(recipe);
					});
				}
			}
			for (EmiStack key : byInput.keySet()) {
				Set<EmiRecipe> r = byInput.getOrDefault(key, null);
				if (r != null) {
					this.byInput.put(key, r.stream().toList());
				} else {
					EmiReloadLog.warn("Stack illegally self-mutated during recipe bake, causing recipe loss: " + key);
				}
			}
			for (EmiStack key : byOutput.keySet()) {
				Set<EmiRecipe> r = byOutput.getOrDefault(key, null);
				if (r != null) {
					this.byOutput.put(key, r.stream().toList());
				} else {
					EmiReloadLog.warn("Stack illegally self-mutated during recipe bake, causing recipe loss: " + key);
				}
			}
			for (EmiRecipeCategory category : workstations.keySet()) {
				List<EmiIngredient> w = workstations.getOrDefault(category, null);
				if (w != null) {
					workstations.put(category, w.stream().distinct().toList());
				} else {
					EmiReloadLog.warn("Recipe category illegally self-mutated during recipe bake, causing recipe loss: " + category);
				}
			}
			for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : byCategory.entrySet()) {
				for (EmiIngredient ingredient : workstations.getOrDefault(entry.getKey(), List.of())) {
					for (EmiStack stack : ingredient.getEmiStacks()) {
						byWorkstation.computeIfAbsent(stack, (s) -> Lists.newArrayList()).addAll(entry.getValue());
					}
				}
			}

			if (EmiConfig.devMode) {
				EmiDev.duplicateRecipeIds = duplicateIds.keySet();
				EmiDev.incorrectRecipeIds = incorrectIds;
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

	private static class Worker implements Runnable {
		private List<EmiRecipeCategory> categories;
		private Map<EmiRecipeCategory, List<EmiIngredient>> workstations;
		private List<EmiRecipe> recipes;

		public Worker(List<EmiRecipeCategory> categories, Map<EmiRecipeCategory, List<EmiIngredient>> workstations, List<EmiRecipe> recipes) {
			this.categories = categories;
			this.workstations = workstations;
			this.recipes = recipes;
		}

		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			Manager manager = new Manager(categories, workstations, recipes, true);
			if (activeWorker == this) {
				long endTime = System.currentTimeMillis();
				EmiLog.info("Baked recipes after reload in " + (endTime - startTime) + "ms");
				EmiRecipes.manager = manager;
			}
			setWorker(null);
		}
	}
}
