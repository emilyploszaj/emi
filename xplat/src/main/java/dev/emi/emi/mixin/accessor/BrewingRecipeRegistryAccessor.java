package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

@Mixin(PotionBrewing.class)
public interface BrewingRecipeRegistryAccessor {
    @Accessor("containers")
    List<Ingredient> getPotionTypes();

    @Accessor("potionMixes")
    List<PotionBrewing.Mix<Potion>> getPotionRecipes();

    @Accessor("containerMixes")
    List<PotionBrewing.Mix<Item>> getItemRecipes();
}
