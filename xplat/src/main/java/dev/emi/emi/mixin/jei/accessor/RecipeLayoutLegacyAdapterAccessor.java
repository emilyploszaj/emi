package dev.emi.emi.mixin.jei.accessor;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import mezz.jei.common.deprecated.gui.recipes.layout.RecipeLayoutLegacyAdapter;
import mezz.jei.common.gui.recipes.layout.IRecipeLayoutInternal;


/**
 * Accessor to get access to IRecipeLayoutInternal that is under private final.
 */
@Pseudo
@Mixin(RecipeLayoutLegacyAdapter.class)
public interface RecipeLayoutLegacyAdapterAccessor
{
    @Accessor(remap = false)
    IRecipeLayoutInternal getRecipeLayout();
}
