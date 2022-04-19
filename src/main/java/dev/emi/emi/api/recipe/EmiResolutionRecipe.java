package dev.emi.emi.api.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;

public class EmiResolutionRecipe implements EmiRecipe {
	public final EmiIngredient ingredient;
	public final EmiStack stack;

	public EmiResolutionRecipe(EmiIngredient ingredient, EmiStack stack) {
		this.ingredient = ingredient;
		this.stack = stack;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.RESOLUTION;
	}

	@Override
	public @Nullable Identifier getId() {
		return null;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(stack);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(stack);
	}

	@Override
	public int getDisplayWidth() {
		return 68;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiRenderHelper.WIDGETS, 22, 1, 24, 17, 44, 0);
		widgets.addSlot(stack, 0, 0);
		widgets.addSlot(ingredient, 50, 0);
	}
}
