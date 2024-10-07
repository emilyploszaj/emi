package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import dev.emi.emi.jemi.impl.JemiRecipeSlot.IngredientRenderer;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.OffsetDrawable;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.TankInfo;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;

public class JemiRecipeSlotBuilder implements IRecipeSlotBuilder {
	public final JemiIngredientAcceptor acceptor;
	public final int x, y;
	public Optional<String> name = Optional.empty();
	public IRecipeSlotTooltipCallback tooltipCallback;
	public OffsetDrawable background, overlay;
	public Map<IIngredientType<?>, IngredientRenderer<?>> renderers; 
	public TankInfo tankInfo;

	public JemiRecipeSlotBuilder(RecipeIngredientRole role, int x, int y) {
		this.acceptor = new JemiIngredientAcceptor(role);
		this.x = x;
		this.y = y;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		acceptor.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		acceptor.addIngredient(ingredientType, ingredient);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		acceptor.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid) {
		acceptor.addFluidStack(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount) {
		acceptor.addFluidStack(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, NbtCompound tag) {
		acceptor.addFluidStack(fluid, amount, tag);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		name = Optional.ofNullable(slotName);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		this.background = new OffsetDrawable(background, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		this.overlay = new OffsetDrawable(overlay, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		this.tankInfo = new TankInfo(width, height, capacity, showCapacity);
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType,
			IIngredientRenderer<T> ingredientRenderer) {
		if (renderers == null) {
			renderers = Maps.newHashMap();
		}
		renderers.put(ingredientType, new IngredientRenderer<T>(ingredientType, ingredientRenderer));
		return this;
	}
}
