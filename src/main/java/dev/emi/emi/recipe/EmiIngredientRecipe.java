package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.util.Identifier;

public class EmiIngredientRecipe implements EmiRecipe {
	private final EmiIngredient ingredient;

	public EmiIngredientRecipe(EmiIngredient ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.INGREDIENT;
	}

	@Override
	public Identifier getId() {
		return null;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(ingredient);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of();
	}

	@Override
	public int getDisplayHeight() {
		return Math.min((ingredient.getEmiStacks().size() - 1) / 8 + 1, 7) * 18 + 24;
	}

	@Override
	public int getDisplayWidth() {
		return 144;
	}

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new SlotWidget(ingredient, x + 63, y));
		List<EmiStack> stacks = ingredient.getEmiStacks();
		for (int i = 0; i < stacks.size() && i < 7 * 8; i++) {
			widgets.add(new SlotWidget(stacks.get(i), x + i % 8 * 18, y + i / 8 * 18 + 24));
		}
	}
}
