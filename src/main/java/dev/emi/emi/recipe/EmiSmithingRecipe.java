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
import dev.emi.emi.mixin.accessor.SmithingRecipeAccessor;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

public class EmiSmithingRecipe implements EmiRecipe {
	private final Identifier id;
	private final EmiIngredient input;
	private final EmiIngredient addition;
	private final EmiStack output;
	
	public EmiSmithingRecipe(SmithingRecipe recipe) {
		this.id = recipe.getId();
		input = EmiIngredient.of(((SmithingRecipeAccessor) recipe).getBase());
		addition = EmiIngredient.of(((SmithingRecipeAccessor) recipe).getAddition());
		output = EmiStack.of(recipe.getOutput());
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.SMITHING;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input, addition);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
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
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 27, y + 3, 13, 13, 82, 0));
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 75, y + 1, 24, 17, 44, 0));
		widgets.add(new SlotWidget(input, x, y));
		widgets.add(new SlotWidget(addition, x + 49, y));
		widgets.add(new SlotWidget(output, x + 107, y).recipeContext(this));
	}
}
