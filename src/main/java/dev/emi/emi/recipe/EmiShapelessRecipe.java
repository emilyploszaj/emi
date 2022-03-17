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
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;

public class EmiShapelessRecipe implements EmiRecipe {
	private final Identifier id;
	private final List<EmiIngredient> input;
	private final EmiStack output;
	
	public EmiShapelessRecipe(ShapelessRecipe recipe) {
		this.id = recipe.getId();
		input = recipe.getIngredients().stream().map(i -> EmiIngredient.of(i)).toList();
		output = EmiStack.of(recipe.getOutput());
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.CRAFTING;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return input;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}

	@Override
	public int getDisplayWidth() {
		return 118;
	}

	@Override
	public int getDisplayHeight() {
		return 54;
	}

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 60, y + 18, 24, 17, 44, 0));
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 97, y, 16, 14, 95, 0));
		for (int i = 0; i < 9; i++) {
			if (i < input.size()) {
				widgets.add(new SlotWidget(input.get(i), x + i % 3 * 18, y + i / 3 * 18));
			} else {
				widgets.add(new SlotWidget(EmiStack.of(ItemStack.EMPTY), x + i % 3 * 18, y + i / 3 * 18));
			}
		}
		widgets.add(new SlotWidget(output, x + 92, y + 14).output(true).recipeContext(this));
	}
}
