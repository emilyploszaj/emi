package dev.emi.emi.api.neoforge;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;


public final class NeoForgeEmiIngredient {

    public static EmiIngredient of(SizedIngredient ingredient) {
        return EmiIngredient.of(ingredient.ingredient(), ingredient.count());
    }

    public static EmiIngredient of(FluidIngredient ingredient) {
        return EmiIngredient.of(ingredient.fluids().stream().map(f -> (EmiIngredient) EmiStack.of(f.value())).toList());
    }

    public static EmiIngredient of(SizedFluidIngredient ingredient) {
        return of(ingredient.ingredient()).setAmount(ingredient.amount());
    }
}
