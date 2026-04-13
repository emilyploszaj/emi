package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;

import java.util.Optional;

@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor {

	@Accessor("template")
    Optional<Ingredient> getTemplate();

	@Accessor("base")
	Ingredient getBase();

	@Accessor("addition")
    Optional<Ingredient> getAddition();
}
