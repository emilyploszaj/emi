package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.mixin.accessor.LegacySmithingRecipeAccessor;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

public class EmiSmithingRecipe implements EmiRecipe {
	private final Identifier id;
	private final EmiIngredient input;
	private final EmiIngredient addition;
	private final EmiStack output;
	
	public EmiSmithingRecipe(SmithingRecipe recipe) {
		this.id = recipe.getId();
		input = EmiIngredient.of(((LegacySmithingRecipeAccessor) recipe).getBase());
		addition = EmiIngredient.of(((LegacySmithingRecipeAccessor) recipe).getAddition());
		output = EmiStack.of(EmiPort.getOutput(recipe));
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.SMITHING;
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
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addSlot(input, 0, 0);
		widgets.addSlot(addition, 49, 0);
		widgets.addSlot(output, 107, 0).recipeContext(this);
	}
}
