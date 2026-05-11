package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.jemi.widget.JemiSlotWidget;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;

public class JemiRecipeSlotDrawable implements IRecipeSlotDrawable {
	public JemiSlotWidget widget;
	public List<IIngredientConsumer> overrides = Lists.newArrayList();

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return widget.slot.getAllIngredients();
	}

	@Override
	public @Unmodifiable List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return widget.slot.getAllIngredientsList();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return widget.slot.getDisplayedIngredient();
	}

	@Override
	public RecipeIngredientRole getRole() {
		return widget.slot.getRole();
	}

	@Override
	public void drawHighlight(DrawContext raw, int color) {
		widget.slot.drawHighlight(raw, color);
	}

	@Override
	public Optional<String> getSlotName() {
		return widget.slot.getSlotName();
	}

	@Override
	public void draw(DrawContext raw) {
		// I don't think I will
	}

	@Override
	public void drawHoverOverlays(DrawContext raw) {
		// I don't think I will
	}

	@Override
	public List<Text> getTooltip() {
		// Unimplemented
		// Mutable because who knows
		return Lists.newArrayList();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		// Unimplemented
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return widget.getBounds().contains((int) mouseX, (int) mouseY);
	}

	@Override
	public void setPosition(int x, int y) {
		// Nope
	}

	@Override
	public IIngredientConsumer createDisplayOverrides() {
		// "Implemented" but also just ignored
		JemiIngredientConsumer consumer = new JemiIngredientConsumer();
		overrides.add(consumer);
		return consumer;
	}

	@Override
	public void clearDisplayOverrides() {
		overrides.clear();
	}

	@Override
	public Rect2i getRect() {
		Bounds bounds = widget.getBounds();
		return new Rect2i(bounds.x(), bounds.y(), bounds.width(), bounds.height());
	}

	@Override
	public Rect2i getAreaIncludingBackground() {
		return getRect();
	}
}
