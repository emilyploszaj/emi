package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.FluidUnit;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;

@SuppressWarnings({"rawtypes", "unchecked"})
public class JemiIngredientConsumer implements IIngredientConsumer {
	public List<EmiIngredient> stacks = Lists.newArrayList();

	@Override
	public <I> IIngredientConsumer addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		for (I i : ingredients) {
			addIngredient(ingredientType, i);
		}
		return this;
	}

	@Override
	public <I> IIngredientConsumer addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		stacks.add(JemiUtil.getStack(ingredientType, ingredient));
		return this;
	}

	@Override
	public IIngredientConsumer addIngredientsUnsafe(List<?> ingredients) {
		for (Object o : ingredients) {
			stacks.add(JemiUtil.getStack(o));
		}
		return this;
	}

	@Override
	public IIngredientConsumer addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		for (ITypedIngredient i : ingredients) {
			addIngredient(i.getType(), i.getIngredient());
		}
		return this;
	}

	@Override
	public IIngredientConsumer addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		for (Optional<ITypedIngredient<?>> i : ingredients) {
			if (i.isPresent()) {
				ITypedIngredient t = i.get();
				addIngredient(t.getType(), t.getIngredient());
			}
		}
		return this;
	}

	@Override
	public IIngredientConsumer addFluidStack(Fluid fluid) {
		return addFluidStack(fluid, FluidUnit.BUCKET);
	}

	@Override
	public IIngredientConsumer addFluidStack(Fluid fluid, long amount) {
		return addFluidStack(fluid, amount, ComponentChanges.EMPTY);
	}

	@Override
	public IIngredientConsumer addFluidStack(Fluid fluid, long amount, ComponentChanges component) {
		stacks.add(EmiStack.of(fluid, component, amount));
		return this;
	}
}
