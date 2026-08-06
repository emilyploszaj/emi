package dev.emi.emi.recipe;

import net.minecraft.recipe.TransmuteRecipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public class EmiTransmuteRecipe extends EmiCraftingRecipe {

    public EmiTransmuteRecipe(TransmuteRecipe recipe) {
        super(recipe.getIngredientPlacement().getIngredients().stream().map(EmiIngredient::of).toList(),
                EmiStack.of(EmiPort.getOutput(recipe)), EmiPort.getId(recipe));
        EmiShapedRecipe.setRemainders(input, recipe);
    }

    @Override
    public boolean canFit(int width, int height) {
        return input.size() <= width * height;
    }
}
