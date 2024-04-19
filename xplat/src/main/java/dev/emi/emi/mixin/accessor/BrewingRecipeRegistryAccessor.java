package dev.emi.emi.mixin.accessor;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BrewingRecipeRegistry.class)
public interface BrewingRecipeRegistryAccessor {
    @Accessor("potionTypes")
    List<Ingredient> getPotionTypes();

    @Accessor("potionRecipes")
    List<BrewingRecipeRegistry.Recipe<Potion>> getPotionRecipes();

    @Accessor("itemRecipes")
    List<BrewingRecipeRegistry.Recipe<Item>> getItemRecipes();
}
