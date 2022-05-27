package dev.emi.emi.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiHistory;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.recipe.EmiSyntheticIngredientRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;

public class EmiApi {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static boolean isCheatMode() {
		return EmiConfig.cheatMode;
	}

	/**
	 * Gets the currently hovered EmiIngredient at the privded screen coordinates,
	 * or {@link EmiStack#EMPTY} if none.
	 * @param includeStandard Whether to include the EmiIngredient representation of
	 * 	standard stacks in slots or otherwise provided to EMI.
	 */
	public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean includeStandard) {
		return EmiScreenManager.getHoveredStack(mouseX, mouseY, includeStandard);
	}

	public static HandledScreen<?> getHandledScreen() {
		Screen s = client.currentScreen;
		if (s instanceof HandledScreen<?> hs) {
			return hs;
		} else if (s instanceof RecipeScreen rs) {
			return rs.old;
		} else if (s instanceof BoMScreen bs) {
			return bs.old;
		}
		return null;
	}

	public static void displayAllRecipes() {
		setPages(EmiRecipes.byCategory);
	}

	public static void displayRecipeCategory(EmiRecipeCategory category) {
		setPages(Map.of(category, EmiRecipes.byCategory.get(category)));
	}

	public static void displayRecipe(EmiRecipe recipe) {
		setPages(Map.of(recipe.getCategory(), List.of(recipe)));
	}
	
	public static void displayRecipes(EmiIngredient stack) {
		if (stack instanceof TagEmiIngredient tag) {
			for (EmiRecipe recipe : EmiRecipes.byCategory.get(VanillaPlugin.TAG)) {
				if (recipe instanceof EmiTagRecipe tr && tr.key.equals(tag.key)) {
					setPages(Map.of(VanillaPlugin.TAG, List.of(recipe)));
					break;
				}
			}
		} else if (stack instanceof ListEmiIngredient list) {
			setPages(Map.of(VanillaPlugin.INGREDIENT, List.of(new EmiSyntheticIngredientRecipe(stack))));
		} else if (stack.getEmiStacks().size() == 1) {
			setPages(pruneSources(EmiRecipes.byOutput.getOrDefault(stack.getEmiStacks().get(0).getKey(), Map.of()),
				stack.getEmiStacks().get(0)));
		}
	}

	public static void displayUses(EmiIngredient stack) {
		if (!stack.isEmpty()) {
			EmiStack zero = stack.getEmiStacks().get(0);
			Map<EmiRecipeCategory, List<EmiRecipe>> map = Maps.newHashMap();
			for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry
					: pruneUses(EmiRecipes.byInput.getOrDefault(zero.getKey(), Map.of()), stack).entrySet()) {
				List<EmiRecipe> list = Lists.newArrayList();
				list.addAll(entry.getValue());
				map.put(entry.getKey(), list);
			}
			for (EmiRecipe recipe : EmiRecipes.byWorkstation.getOrDefault(zero, List.of())) {
				map.computeIfAbsent(recipe.getCategory(), (r) -> Lists.newArrayList()).add(recipe);
			}
			setPages(map);
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

	public static void performFill(EmiRecipe recipe, EmiFillAction action, boolean all) {
		HandledScreen<?> hs;
		if (client.currentScreen instanceof RecipeScreen rs) {
			hs = rs.old;
		} else if (client.currentScreen instanceof HandledScreen<?> s) {
			hs = s;
		} else {
			return;
		}
		List<ItemStack> stacks = recipe.getFill(hs, all);
		if (stacks != null) {
			client.setScreen(hs);
			EmiClient.sendFillRecipe(hs.getScreenHandler().syncId, action.id, stacks);
		}
	}

	private static void push() {
		if (client.currentScreen instanceof RecipeScreen rs) {
			EmiHistory.push(rs);
		} else if (client.currentScreen instanceof BoMScreen bs) {
			EmiHistory.push(bs);
		} else {
			EmiHistory.clear();
		}
	}

	private static Map<EmiRecipeCategory, List<EmiRecipe>> pruneSources(Map<EmiRecipeCategory, List<EmiRecipe>> map,
			EmiStack context) {
		return map.entrySet().stream().map(e -> {
			return Maps.immutableEntry(e.getKey(), e.getValue().stream().filter(r -> 
				r.getOutputs().stream().anyMatch(i -> i.isEqual(context))).toList());
		}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	private static Map<EmiRecipeCategory, List<EmiRecipe>> pruneUses(Map<EmiRecipeCategory, List<EmiRecipe>> map,
			EmiIngredient context) {
		return map.entrySet().stream().map(e -> {
			return Maps.immutableEntry(e.getKey(), e.getValue().stream().filter(r -> 
				r.getInputs().stream().anyMatch(i -> containsAll(i, context))).toList());
		}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
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

	private static void setPages(Map<EmiRecipeCategory, List<EmiRecipe>> recipes) {
		recipes = recipes.entrySet().stream().filter(e -> !e.getValue().isEmpty())
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		if (!recipes.isEmpty()) {
			if (client.currentScreen == null) {
				client.setScreen(new InventoryScreen(client.player));
			}
			if (client.currentScreen instanceof HandledScreen<?> hs) {
				push();
				client.setScreen(new RecipeScreen(hs, recipes));
			} else if (client.currentScreen instanceof BoMScreen bs) {
				push();
				client.setScreen(new RecipeScreen(bs.old, recipes));
			} else if (client.currentScreen instanceof RecipeScreen rs) {
				push();
				RecipeScreen n = new RecipeScreen(rs.old, recipes);
				client.setScreen(n);
				n.focusCategory(rs.getFocusedCategory());
			}
		}
	}
}
