package dev.emi.emi.recipe.special;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.recipe.EmiCraftingRecipe;
import net.minecraft.util.Identifier;

public abstract class EmiPatternCraftingRecipe extends EmiCraftingRecipe {
	protected final int unique = EmiUtil.RANDOM.nextInt();
	
	public EmiPatternCraftingRecipe(List<EmiIngredient> input, EmiStack output, Identifier id) {
		super(input, output, id);
	}

	public abstract SlotWidget getInputWidget(int slot, int x, int y);

	public abstract SlotWidget getOutputWidget(int x, int y);

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 60, y + 18, 24, 17, 44, 0));
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 97, y, 16, 14, 95, 0));
		for (int i = 0; i < 9; i++) {
			widgets.add(getInputWidget(i, x + i % 3 * 18, y + i / 3 * 18));
		}
		widgets.add(getOutputWidget(x + 92, y + 14).output(true).recipeContext(this));
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}
}
