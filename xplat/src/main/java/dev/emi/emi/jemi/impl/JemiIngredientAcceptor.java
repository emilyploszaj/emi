package dev.emi.emi.jemi.impl;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;

public class JemiIngredientAcceptor implements IIngredientAcceptor<JemiIngredientAcceptor> {
	public final RecipeIngredientRole role;
	public final List<EmiStack> stacks = Lists.newArrayList();

	public JemiIngredientAcceptor(RecipeIngredientRole role) {
		this.role = role;
	}

	public EmiIngredient build() {
		return EmiIngredient.of(stacks);
	}

	@Override
	public <I> JemiIngredientAcceptor addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		for (I i : ingredients) {
			addIngredient(ingredientType, i);
		}
		return this;
	}

	@Override
	public <I> JemiIngredientAcceptor addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		stacks.add(JemiUtil.getStack(ingredientType, ingredient));
		return this;
	}

	@Override
	public JemiIngredientAcceptor addIngredientsUnsafe(List<?> ingredients) {
		stacks.addAll(ingredients.stream().map(JemiUtil::getStack).filter(s -> !s.isEmpty()).toList());
		return this;
	}

	@Override
	public JemiIngredientAcceptor addFluidStack(Fluid fluid, long amount) {
		stacks.add(EmiStack.of(fluid, amount));
		return this;
	}

	@Override
	public JemiIngredientAcceptor addFluidStack(Fluid fluid, long amount, NbtCompound tag) {
		stacks.add(EmiStack.of(fluid, tag, amount));
		return this;
	}
}
