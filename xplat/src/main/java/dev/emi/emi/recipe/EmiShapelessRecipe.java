package dev.emi.emi.recipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.recipe.ShapelessRecipe;

public class EmiShapelessRecipe extends EmiCraftingRecipe {
	
	public EmiShapelessRecipe(ShapelessRecipe recipe) {
		super(recipe.getIngredients().stream().map(i -> EmiIngredient.of(i)).toList(), EmiStack.of(EmiPort.getOutput(recipe)),
			recipe.getId());
		EmiShapedRecipe.setRemainders(input, recipe);
	}

	@Override
	public boolean canFit(int width, int height) {
		return input.size() <= width * height;
	}
}
