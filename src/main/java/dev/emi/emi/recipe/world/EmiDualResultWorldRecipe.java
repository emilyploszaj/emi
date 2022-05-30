package dev.emi.emi.recipe.world;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
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
		if (isCatalyst) {
			for (EmiStack stack : catalyst.getEmiStacks()) {
				stack.setRemainder(stack);
			}
		}
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
			return super.getCatalysts();
		}
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(result1, result2);
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiRenderHelper.WIDGETS, 22, 3, 13, 13, 82, 0);
		widgets.addTexture(EmiRenderHelper.WIDGETS, 61, 1, 24, 17, 44, 0);
		widgets.addSlot(input, 0, 0);
		widgets.addSlot(catalyst, 40, 0).catalyst(isCatalyst);
		widgets.addSlot(result1, 89, 0).recipeContext(this);
		widgets.addSlot(result2, 107, 0).recipeContext(this);
	}	
}
