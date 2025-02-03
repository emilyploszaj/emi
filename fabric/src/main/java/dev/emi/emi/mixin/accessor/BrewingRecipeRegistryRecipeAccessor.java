package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;

@Mixin(BrewingRecipeRegistry.Recipe.class)
public interface BrewingRecipeRegistryRecipeAccessor {

	@Accessor("input")
	Object getInput();

	@Accessor("ingredient")
	Ingredient getIngredient();

	@Accessor("output")
	Object getOutput();
}
