package dev.emi.emi.mixin.jei.accessor;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import mezz.jei.common.ingredients.IngredientManager;
import mezz.jei.common.ingredients.RegisteredIngredients;


/**
 * Accessor used to get RegisteredIngredients object that is under private final.
 */
@Pseudo
@Mixin(IngredientManager.class)
public interface IngredientManagerAccessor
{
    @Accessor(remap = false)
    RegisteredIngredients getRegisteredIngredients();
}
