package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.AnimatedTextureWidget;
import dev.emi.emi.api.widget.FillingArrowWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.text.TranslatableText;
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
	public void addWidgets(List<Widget> widgets, int x, int y) {
		widgets.add(new FillingArrowWidget(x + 24, y + 5, 50 * recipe.getCookTime()).tooltip(() -> {
			return List.of(TooltipComponent.of(new TranslatableText("emi.cooking.time", recipe.getCookTime() / 20f).asOrderedText()));
		}));
		if (infiniBurn) {
			widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 1, y + 24, 14, 14, 68, 14));
		} else {
			widgets.add(new TextureWidget(EmiRenderHelper.WIDGETS, x + 1, y + 24, 14, 14, 68, 0));
			widgets.add(new AnimatedTextureWidget(EmiRenderHelper.WIDGETS, x + 1, y + 24, 14, 14, 68, 14, 4000 / fuelMultiplier,
				false, true, true));
		}
		widgets.add(new TextWidget(new TranslatableText("emi.cooking.experience",
			recipe.getExperience()).asOrderedText(), x + 26, y + 28, true));
		widgets.add(new SlotWidget(input, x, y + 4));
		widgets.add(new SlotWidget(output, x + 56, y).output(true).recipeContext(this));
	}
}
