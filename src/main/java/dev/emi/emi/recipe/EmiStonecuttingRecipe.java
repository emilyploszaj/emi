package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.Identifier;

public class EmiStonecuttingRecipe implements EmiRecipe {
	private final Identifier id;
	private final EmiIngredient input;
	private final EmiStack output;
	
	public EmiStonecuttingRecipe(StonecuttingRecipe recipe) {
		this.id = recipe.getId();
		input = EmiIngredient.of(recipe.getIngredients().get(0));
		output = EmiStack.of(recipe.getOutput());
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.STONECUTTING;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}

	@Override
	public int getDisplayWidth() {
		return 76;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 26, y + 1, 24, 17, 44, 0));
		widgets.add(new SlotWidget(input, x, y));
		widgets.add(new SlotWidget(output, x + 58, y).recipeContext(this));
	}
}
