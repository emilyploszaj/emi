package dev.emi.emi.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.recipe.EmiSyntheticIngredientRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public class EmiApi {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static List<EmiStack> getIndexStacks() {
		return EmiStackList.stacks;
	}

	public static EmiRecipeManager getRecipeManager() {
		return EmiRecipes.manager;
	}

	public static boolean isCheatMode() {
		return EmiConfig.cheatMode;
	}

	/**
	 * @return Current search text
	 */
	public static String getSearchText() {
		return EmiScreenManager.search.getText();
	}

	/**
	 * Sets the current search to the provided query
	 */
	public static void setSearchText(String text) {
		EmiScreenManager.search.setText(text);
	}

	public static boolean isSearchFocused() {
		return EmiScreenManager.search.isFocused();
	}

	/**
	 * Gets the currently hovered EmiIngredient at the provided screen coordinates,
	 * or {@link EmiStack#EMPTY} if none.
	 * @param includeStandard Whether to include the EmiIngredient representation of
	 * 	standard stacks in slots or otherwise provided to EMI.
	 */
	public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean includeStandard) {
		return EmiScreenManager.getHoveredStack(mouseX, mouseY, includeStandard);
	}

	/**
	 * Gets the currently hovered EmiIngredient at the mouse or {@link EmiStack#EMPTY} if none.
	 * @param includeStandard Whether to include the EmiIngredient representation of
	 * 	standard stacks in slots or otherwise provided to EMI.
	 */
	public static EmiStackInteraction getHoveredStack(boolean includeStandard) {
		return EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, includeStandard);
	}

	/**
	 * @return Recipe context associated with specific ingredient implementations.
	 *  This could be favorites, craftables, or something else.
	 */
	public static @Nullable EmiRecipe getRecipeContext(EmiIngredient stack) {
		if (stack instanceof EmiFavorite fav) {
			return fav.getRecipe();
		}
		return null;
	}

	public static @Nullable HandledScreen<?> getHandledScreen() {
		Screen s = client.currentScreen;
		if (s instanceof HandledScreen<?> hs) {
			return hs;
		} else if (s instanceof RecipeScreen rs && rs.old instanceof HandledScreen<?> hs) {
			return hs;
		} else if (s instanceof BoMScreen bs && bs.old instanceof HandledScreen<?> hs) {
			return hs;
		}
		return null;
	}

	public static void displayAllRecipes() {
		EmiRecipeManager manager = EmiApi.getRecipeManager();
		setPages(manager.getCategories().stream().collect(Collectors.toMap(c -> c, c -> manager.getRecipes(c))), EmiStack.EMPTY);
	}

	public static void displayRecipeCategory(EmiRecipeCategory category) {
		setPages(Map.of(category, getRecipeManager().getRecipes(category)), EmiStack.EMPTY);
	}

	public static void displayRecipe(EmiRecipe recipe) {
		setPages(Map.of(recipe.getCategory(), List.of(recipe)), EmiStack.EMPTY);
	}

	public static void displayRecipes(EmiIngredient stack) {
		if (stack instanceof EmiFavorite fav) {
			stack = fav.getStack();
		}
		if (stack instanceof TagEmiIngredient tag) {
			for (EmiRecipe recipe : getRecipeManager().getRecipes(VanillaPlugin.TAG)) {
				if (recipe instanceof EmiTagRecipe tr && tr.key.equals(tag.key)) {
					setPages(Map.of(VanillaPlugin.TAG, List.of(recipe)), stack);
					break;
				}
			}
		} else if (stack instanceof ListEmiIngredient list) {
			setPages(Map.of(VanillaPlugin.INGREDIENT, List.of(new EmiSyntheticIngredientRecipe(stack))), stack);
		} else if (stack.getEmiStacks().size() == 1) {
			EmiStack es = stack.getEmiStacks().get(0);
			setPages(mapRecipes(pruneSources(EmiApi.getRecipeManager().getRecipesByOutput(es), es)), stack);
			focusRecipe(BoM.getRecipe(es));
		}
	}

	public static void displayUses(EmiIngredient stack) {
		if (!stack.isEmpty()) {
			EmiStack zero = stack.getEmiStacks().get(0);
			Map<EmiRecipeCategory, List<EmiRecipe>> map
				= mapRecipes(Stream.concat(
						pruneUses(getRecipeManager().getRecipesByInput(zero), stack).stream(),
						EmiRecipes.byWorkstation.getOrDefault(zero, List.of()).stream()).distinct().toList());
			setPages(map, stack);
		}
	}

	public static void viewRecipeTree() {
		if (client.currentScreen == null) {
			client.setScreen(new InventoryScreen(client.player));
		}
		Screen s = client.currentScreen;
		if (s instanceof HandledScreen<?> hs) {
			push();
			client.setScreen(new BoMScreen(hs));
		} else if (s instanceof RecipeScreen rs) {
			push();
			client.setScreen(new BoMScreen(rs.old));
		}
	}

	public static void focusRecipe(EmiRecipe recipe) {
		if (client.currentScreen instanceof RecipeScreen rs) {
			rs.focusRecipe(recipe);
		}
	}

	private static void push() {
		if (client.currentScreen instanceof RecipeScreen rs) {
			EmiHistory.push(rs);
		} else if (client.currentScreen instanceof BoMScreen bs) {
			EmiHistory.push(bs);
		} else {
			EmiHistory.clear();
			EmiHistory.push(client.currentScreen);
		}
	}

	private static List<EmiRecipe> pruneSources(List<EmiRecipe> list, EmiStack context) {
		return list.stream().filter(r -> {
			return r.getOutputs().stream().anyMatch(i -> i.isEqual(context));
		}).toList();
	}

	private static List<EmiRecipe> pruneUses(List<EmiRecipe> list, EmiIngredient context) {
		return list.stream().filter(r -> {
			return r.getInputs().stream().anyMatch(i -> containsAll(i, context))
				|| r.getCatalysts().stream().anyMatch(i -> containsAll(i, context));
		}).sorted((a, b) -> getSmallestPresence(a, context) - getSmallestPresence(b, context)).toList();
	}

	private static int getSmallestPresence(EmiRecipe recipe, EmiIngredient context) {
		int ideal = context.getEmiStacks().size();
		int smallestPresence = Integer.MAX_VALUE;
		for (EmiIngredient i : recipe.getInputs()) {
			if (containsAll(i, context)) {
				smallestPresence = Math.min(smallestPresence, i.getEmiStacks().size());
				if (smallestPresence <= ideal) {
					break;
				}
			}
		}
		return smallestPresence;
	}

	private static Map<EmiRecipeCategory, List<EmiRecipe>> mapRecipes(List<EmiRecipe> list) {
		Map<EmiRecipeCategory, List<EmiRecipe>> map = Maps.newHashMap();
		for (EmiRecipe recipe : list) {
			map.computeIfAbsent(recipe.getCategory(), k -> Lists.newArrayList()).add(recipe);
		}
		return map;
	}

	private static boolean containsAll(EmiIngredient collection, EmiIngredient ingredient) {
		outer:
		for (EmiStack ing : ingredient.getEmiStacks()) {
			for (EmiStack col : collection.getEmiStacks()) {
				if (col.isEqual(ing)) {
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

	private static void setPages(Map<EmiRecipeCategory, List<EmiRecipe>> recipes, EmiIngredient stack) {
		recipes = recipes.entrySet().stream().filter(e -> !e.getValue().isEmpty())
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		if (!recipes.isEmpty()) {
			EmiSidebars.lookup(stack);
			if (client.currentScreen instanceof BoMScreen bs) {
				push();
				client.setScreen(new RecipeScreen(bs.old, recipes));
			} else if (client.currentScreen instanceof RecipeScreen rs) {
				push();
				RecipeScreen n = new RecipeScreen(rs.old, recipes);
				client.setScreen(n);
				n.focusCategory(rs.getFocusedCategory());
			} else {
				push();
				client.setScreen(new RecipeScreen(client.currentScreen, recipes));
			}
		}
	}
}
