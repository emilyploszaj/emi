package dev.emi.emi.recipe.world;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.util.Identifier;

public class EmiDualResultWorldRecipe extends EmiCustomWorldRecipe {
	private final boolean isCatalyst;
	private final EmiIngredient input, catalyst;
	private final EmiStack result1, result2;

	public EmiDualResultWorldRecipe(EmiIngredient input, EmiIngredient catalyst, EmiStack result1, EmiStack result2,
			Identifier id, boolean isCatalyst) {
		super(id);
		this.isCatalyst = isCatalyst;
		this.input = input;
		this.catalyst = catalyst;
		this.result1 = result1;
		this.result2 = result2;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		if (isCatalyst) {
			return List.of(input);
		} else {
			return List.of(input, catalyst);
		}
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		if (isCatalyst) {
			return List.of(catalyst);
		} else {
			return List.of();
		}
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(result1, result2);
	}

	@Override
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 22, y + 3, 13, 13, 82, 0));
		widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 61, y + 1, 24, 17, 44, 0));
		widgets.add(new SlotWidget(input, x, y));
		widgets.add(new SlotWidget(catalyst, x + 40, y).catalyst(isCatalyst));
		widgets.add(new SlotWidget(result1, x + 89, y).recipeContext(this));
		widgets.add(new SlotWidget(result2, x + 107, y).recipeContext(this));
	}	
}
