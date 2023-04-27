package dev.emi.emi.registry;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.runtime.EmiReloadLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

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
		if (recipe.getInputs() == null) {
			EmiReloadLog.warn("Recipe " + recipe.getId() + " provides null inputs and cannot be added");
		} else if (recipe.getOutputs() == null) {
			EmiReloadLog.warn("Recipe " + recipe.getId() + " provides null outputs and cannot be added");
		} else {
			EmiRecipes.addRecipe(recipe);
		}
	}

	@Override
	public void removeRecipes(Predicate<EmiRecipe> predicate) {
		EmiRecipes.invalidators.add(predicate);
	}

	@Override
	public void addDeferredRecipes(Consumer<Consumer<EmiRecipe>> consumer) {
		EmiRecipes.lateRecipes.add(consumer);
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
	public <T extends EmiIngredient> void addIngredientSerializer(Class<T> clazz, EmiIngredientSerializer<T> serializer) {
		EmiIngredientSerializers.BY_CLASS.put(clazz, serializer);
		EmiIngredientSerializers.BY_TYPE.put(serializer.getType(), serializer);
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
	public <T extends Screen> void addDragDropHandler(Class<T> clazz, EmiDragDropHandler<T> handler) {
		EmiDragDropHandlers.fromClass.computeIfAbsent(clazz, c -> Lists.newArrayList()).add(handler);
	}

	@Override
	public void addGenericDragDropHandler(EmiDragDropHandler<Screen> handler) {
		EmiDragDropHandlers.generic.add(handler);
	}

	@Override
	public <T extends Screen> void addStackProvider(Class<T> clazz, EmiStackProvider<T> provider) {
		EmiStackProviders.fromClass.computeIfAbsent(clazz, c -> Lists.newArrayList()).add(provider);
	}

	@Override
	public void addGenericStackProvider(EmiStackProvider<Screen> provider) {
		EmiStackProviders.generic.add(provider);
	}
	
	@Override
	public <T extends ScreenHandler> void addRecipeHandler(ScreenHandlerType<T> type, EmiRecipeHandler<T> handler) {
		EmiRecipeFiller.handlers.computeIfAbsent(type, (c) -> Lists.newArrayList()).add(handler);
	}

	@Override
	public void setDefaultComparison(Object key, Function<Comparison, Comparison> comparison) {
		EmiComparisonDefaults.comparisons.put(key, comparison.apply(EmiComparisonDefaults.get(key)));
	}
}
