package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingRecipe;

@Mixin(SmithingRecipe.class)
public interface SmithingRecipeAccessor {
	
	@Accessor("base")
	Ingredient getBase();

	@Accessor("addition")
	Ingredient getAddition();
}
