package dev.emi.emi;

import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.recipe.RecipeManager;

public class EmiRegistryImpl implements EmiRegistry {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public RecipeManager getRecipeManager() {
		return client.world.getRecipeManager();
	}

	@Override
	public void addCategory(EmiRecipeCategory category) {
		EmiRecipes.addCategory(category);
	}

	@Override
	public void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation) {
		EmiRecipes.addWorkstation(category, workstation);
	}

	@Override
	public void addRecipe(EmiRecipe recipe) {
		EmiRecipes.addRecipe(recipe);
	}

	@Override
	public void removeRecipes(Predicate<EmiRecipe> predicate) {
		EmiRecipes.invalidators.add(predicate);
	}

	@Override
	public void addEmiStack(EmiStack stack) {
		EmiStackList.stacks.add(stack);
	}

	@Override
	public void addEmiStackAfter(EmiStack stack, Predicate<EmiStack> predicate) {
		for (int i = 0; i < EmiStackList.stacks.size(); i++) {
			if (predicate.test(EmiStackList.stacks.get(i))) {
				EmiStackList.stacks.add(i + 1, stack);
				return;
			}
		}
	}

	@Override
	public void removeEmiStacks(Predicate<EmiStack> predicate) {
		EmiStackList.invalidators.add(predicate);
	}

	@Override
	public <T extends Screen> void addExclusionArea(Class<T> clazz, EmiExclusionArea<T> area) {
		EmiExclusionAreas.fromClass.computeIfAbsent(clazz, c -> Lists.newArrayList()).add(area);
	}

	@Override
	public void addGenericExclusionArea(EmiExclusionArea<Screen> area) {
		EmiExclusionAreas.generic.add(area);
	}
	
	@Override
	public void addRecipeHandler(EmiRecipeCategory category, EmiRecipeHandler<?> handler) {
		EmiRecipeFiller.RECIPE_HANDLERS.computeIfAbsent(category, (c) -> Sets.newHashSet()).add(handler);
	}

	@Override
	public void setDefaultComparison(Object key, Function<Comparison, Comparison> comparison) {
		EmiComparisonDefaults.comparisons.put(key, comparison.apply(EmiComparisonDefaults.get(key)));
	}
}
