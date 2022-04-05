package dev.emi.emi.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiRecipes;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiIngredientList;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiTagIngredient;
import dev.emi.emi.recipe.EmiIngredientRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;

public class EmiApi {
	private static final MinecraftClient client = MinecraftClient.getInstance();

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
		if (stack instanceof EmiTagIngredient tag) {
			for (EmiRecipe recipe : EmiRecipes.byCategory.get(VanillaPlugin.TAG)) {
				if (recipe instanceof EmiTagRecipe tr && tr.tag == tag.tag) {
					setPages(Map.of(VanillaPlugin.TAG, List.of(recipe)));
					break;
				}
			}
		} else if (stack instanceof EmiIngredientList list) {
			setPages(Map.of(VanillaPlugin.INGREDIENT, List.of(new EmiIngredientRecipe(stack))));
		} else if (stack.getEmiStacks().size() > 0) {
			setPages(pruneSources(EmiRecipes.byOutput.getOrDefault(stack.getEmiStacks().get(0).getKey(), Map.of()), stack));
		}
	}

	public static void displayUses(EmiIngredient stack) {
		if (!stack.isEmpty()) {
			setPages(pruneUses(EmiRecipes.byInput.getOrDefault(stack.getEmiStacks().get(0).getKey(), Map.of()), stack));
		}
	}

	public static void viewRecipeTree() {
		if (client.currentScreen == null) {
			client.setScreen(new InventoryScreen(client.player));
		}
		Screen s = client.currentScreen;
		if (s instanceof HandledScreen<?> hs) {
			client.setScreen(new BoMScreen(hs));
		} else if (s instanceof RecipeScreen rs) {
			client.setScreen(new BoMScreen(rs.old));
		}
	}

	public static void focusRecipe(EmiRecipe recipe) {
		if (client.currentScreen instanceof RecipeScreen rs) {
			rs.focusRecipe(recipe);
		}
	}

	public static boolean canFill(EmiRecipe recipe) {
		HandledScreen<?> hs;
		if (client.currentScreen instanceof RecipeScreen rs) {
			hs = rs.old;
		} else if (client.currentScreen instanceof HandledScreen<?> s) {
			hs = s;
		} else {
			return false;
		}
		return recipe.canFill(hs);
	}

	public static void performFill(EmiRecipe recipe, boolean all) {
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
			int action = 0;
			if (EmiUtil.isControlDown()) {
				action = 1;
			}
			if (EmiUtil.isShiftDown()) {
				action = 2;
			}
			EmiClient.sendFillRecipe(hs.getScreenHandler().syncId, action, stacks);
		}
	}

	private static Map<EmiRecipeCategory, List<EmiRecipe>> pruneUses(Map<EmiRecipeCategory, List<EmiRecipe>> map,
			EmiIngredient context) {
		return map.entrySet().stream().map(e -> {
			return Maps.immutableEntry(e.getKey(), e.getValue().stream().filter(r -> 
				r.getInputs().stream().anyMatch(i -> containsAll(i, context))
				|| r.getCatalysts().stream().anyMatch(i -> containsAll(i, context))).toList());
		}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	private static Map<EmiRecipeCategory, List<EmiRecipe>> pruneSources(Map<EmiRecipeCategory, List<EmiRecipe>> map,
			EmiIngredient context) {
		return map.entrySet().stream().map(e -> {
			return Maps.immutableEntry(e.getKey(), e.getValue().stream().filter(r -> 
				r.getOutputs().stream().anyMatch(i -> containsAll(i, context))).toList());
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
				client.setScreen(new RecipeScreen(hs));
			} else if (client.currentScreen instanceof BoMScreen bs) {
				client.setScreen(new RecipeScreen(bs.old));
			}
			if (client.currentScreen instanceof RecipeScreen rs) {
				rs.setPages(recipes);
			}
		}
	}
}
