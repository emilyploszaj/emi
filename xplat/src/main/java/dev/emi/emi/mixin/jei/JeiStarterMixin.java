package dev.emi.emi.mixin.jei;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.emi.emi.jemi.runtime.JemiIngredientFilter;
import dev.emi.emi.jemi.runtime.JemiRecipesGui;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.runtime.*;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.recipes.RecipesGui;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.common.ingredients.IngredientFilterApi;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.recipes.RecipeManager;
import mezz.jei.common.recipes.RecipeTransferManager;
import mezz.jei.common.startup.JeiStarter;
import mezz.jei.core.config.IClientConfig;


@Pseudo
@Mixin(value = JeiStarter.class, remap = false)
public abstract class JeiStarterMixin {
    /**
     * Replaces JEI RecipesGUI with JemiRecipesGui.
     */
    @Redirect(method = "start",
        at = @At(value = "NEW",
            target = "(Lmezz/jei/common/recipes/RecipeManager;Lmezz/jei/common/recipes/RecipeTransferManager;Lmezz/jei/common/ingredients/RegisteredIngredients;Lmezz/jei/api/helpers/IModIdHelper;Lmezz/jei/core/config/IClientConfig;Lmezz/jei/common/gui/textures/Textures;Lmezz/jei/api/runtime/IIngredientVisibility;Lmezz/jei/common/input/IKeyBindings;)Lmezz/jei/common/gui/recipes/RecipesGui;"
        )
    )
    private RecipesGui injectJemiRecipesGui(RecipeManager recipeManager,
        RecipeTransferManager recipeTransferManager,
        RegisteredIngredients registeredIngredients,
        IModIdHelper modIdHelper,
        IClientConfig clientConfig,
        Textures textures,
        IIngredientVisibility ingredientVisibility,
        IKeyBindings keyBindings) {

        return new JemiRecipesGui(recipeManager,
            recipeTransferManager,
            registeredIngredients,
            modIdHelper,
            clientConfig,
            textures,
            ingredientVisibility,
            keyBindings);
    }


    /**
     * Replaces JEI IngredientFilterApi with JemiIngredientFilter.
     */
    @Redirect(method = "start",
        at = @At(value = "NEW",
            target = "(Lmezz/jei/common/ingredients/IngredientFilter;Lmezz/jei/common/filter/IFilterTextSource;)Lmezz/jei/common/ingredients/IngredientFilterApi;"
        )
    )
    private IngredientFilterApi injectJemiIngredientFilter(IngredientFilter ingredientFilter, IFilterTextSource filterTextSource) {
        return new JemiIngredientFilter(ingredientFilter, filterTextSource);
    }
}
