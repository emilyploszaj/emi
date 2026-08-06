package dev.emi.emi.api.neoforge;

import dev.emi.emi.api.stack.EmiIngredient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.util.context.ContextParameterMap;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;

public final class NeoForgeEmiIngredient {

    public static EmiIngredient of(SizedIngredient ingredient) {
        return EmiIngredient.of(ingredient.ingredient(), ingredient.count());
    }

    public static EmiIngredient of(FluidIngredient ingredient) {
        return EmiIngredient.of(ingredient.fluids().stream().map((entry) -> {
            return NeoForgeEmiStack.of(new FluidStack(entry, 1000)); // TODO: review this
        }).toList());
    }

    public static EmiIngredient of(SizedFluidIngredient ingredient) {
        return of(ingredient.ingredient()).setAmount(ingredient.amount());
    }
}
