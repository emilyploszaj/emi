package dev.emi.emi.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;

public class EmiWorldRecipe implements EmiRecipe {
	private final boolean isCatalyst;
	private final Identifier id;
	private final EmiIngredient input, catalyst;
	private final EmiStack result;

	public EmiWorldRecipe(EmiIngredient input, EmiIngredient catalyst, EmiStack result, Identifier id) {
		this(input, catalyst, result, id, true);
	}

	public EmiWorldRecipe(EmiIngredient input, EmiIngredient catalyst, EmiStack result, Identifier id, boolean isCatalyst) {
		this.isCatalyst = isCatalyst;
		this.input = input;
		this.catalyst = catalyst;
		this.result = result;
		this.id = id;
		if (isCatalyst) {
			for (EmiStack stack : catalyst.getEmiStacks()) {
				stack.setRemainder(stack);
			}
		}
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.WORLD_INTERACTION;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input, catalyst);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(result);
	}

	@Override
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiRenderHelper.WIDGETS, 27, 3, 13, 13, 82, 0);
		widgets.addTexture(EmiRenderHelper.WIDGETS, 75, 1, 24, 17, 44, 0);
		widgets.addSlot(input, 0, 0);
		widgets.addSlot(catalyst, 49, 0).catalyst(isCatalyst);
		widgets.addSlot(result, 107, 0).recipeContext(this);
	}	
}
