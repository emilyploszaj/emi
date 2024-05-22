package dev.emi.emi.api.neoforge;

import dev.emi.emi.api.stack.EmiIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.Arrays;

public final class NeoForgeEmiIngredient {

    public static EmiIngredient of(FluidIngredient ingredient) {
        return EmiIngredient.of(Arrays.stream(ingredient.getStacks()).map(NeoForgeEmiStack::of).toList());
    }
}
