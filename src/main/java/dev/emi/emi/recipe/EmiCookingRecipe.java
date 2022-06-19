package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.util.Identifier;

public class EmiCookingRecipe implements EmiRecipe {
	private final Identifier id;
	private final EmiRecipeCategory category;
	private final EmiIngredient input;
	private final EmiStack output;
	private final AbstractCookingRecipe recipe;
	private final int fuelMultiplier;
	private final boolean infiniBurn;
	
	public EmiCookingRecipe(AbstractCookingRecipe recipe, EmiRecipeCategory category, int fuelMultiplier, boolean infiniBurn) {
		this.id = recipe.getId();
		this.category = category;
		input = EmiIngredient.of(recipe.getIngredients().get(0));
		output = EmiStack.of(recipe.getOutput());
		this.recipe = recipe;
		this.fuelMultiplier = fuelMultiplier;
		this.infiniBurn = infiniBurn;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return category;
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
		return 82;
	}

	@Override
	public int getDisplayHeight() {
		return 38;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addFillingArrow(24, 5, 50 * recipe.getCookTime()).tooltip((mx, my) -> {
			return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.cooking.time", recipe.getCookTime() / 20f))));
		});
		if (infiniBurn) {
			widgets.addTexture(EmiTexture.FULL_FLAME, 1, 24);
		} else {
			widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 24);
			widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 1, 24, 4000 / fuelMultiplier, false, true, true);
		}
		widgets.addText(EmiPort.ordered(EmiPort.translatable("emi.cooking.experience", recipe.getExperience())), 26, 28, -1, true);
		widgets.addSlot(input, 0, 4);
		widgets.addSlot(output, 56, 0).output(true).recipeContext(this);
	}
}
