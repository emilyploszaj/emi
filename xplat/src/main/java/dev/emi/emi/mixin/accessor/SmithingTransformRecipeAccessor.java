package dev.emi.emi.mixin.accessor;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor {

	@Accessor("template")
	Ingredient getTemplate();

	@Accessor("base")
	Ingredient getBase();

	@Accessor("addition")
	Ingredient getAddition();
}
