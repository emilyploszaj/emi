package dev.emi.emi.recipe.special;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
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
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiRenderHelper.WIDGETS, 60, 18, 24, 17, 44, 0);
		widgets.addTexture(EmiRenderHelper.WIDGETS, 97, 0, 16, 14, 95, 0);
		for (int i = 0; i < 9; i++) {
			widgets.add(getInputWidget(i, widgets.getX() + i % 3 * 18, widgets.getY() + i / 3 * 18));
		}
		widgets.add(getOutputWidget(widgets.getX() + 92, widgets.getY() + 14).output(true).recipeContext(this));
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}
}
