package dev.emi.emi.jemi.impl;

import java.util.List;

import org.jetbrains.annotations.Unmodifiable;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;

public class JemiRecipeSlotDrawablesView implements IRecipeSlotDrawablesView {

	@Override
	public @Unmodifiable List<IRecipeSlotDrawable> getSlots() {
		return List.of();
	}
}
